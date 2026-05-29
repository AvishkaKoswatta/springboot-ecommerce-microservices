package com.microservice.paymentservice.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.paymentservice.entity.*;
import com.microservice.paymentservice.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Manages async webhook delivery and retry logic.
 *
 * Flow:
 *   1. Payment event occurs (success/failure/refund)
 *   2. WebhookEvent record is persisted (PENDING)
 *   3. @Async dispatch attempts delivery to order-service webhook endpoint
 *   4. On success → mark DELIVERED
 *   5. On failure → mark RETRYING, scheduler retries up to 3 times
 *
 * Webhook signature: HMAC-SHA256(payload, webhookSecret) in X-Webhook-Signature header
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookDispatchService {

    private final WebhookEventRepository webhookRepository;
    private final ObjectMapper           objectMapper;

    @Value("${application.mock-gateway.webhook-secret}")
    private String webhookSecret;

    @Value("${application.mock-gateway.webhook-delay-ms:1200}")
    private long webhookDelayMs;

    @Value("${services.order-service-url:http://order-service}")
    private String orderServiceUrl;

    // ─────────────────────────────────────────────────────────────────────────
    // Dispatch a webhook event asynchronously
    // ─────────────────────────────────────────────────────────────────────────

    @Async("asyncTaskExecutor")
    @Transactional
    public void dispatchAsync(WebhookEventType eventType, Payment payment, String payload) {
        // Create and persist event record first
        WebhookEvent event = WebhookEvent.builder()
                .eventId(generateEventId())
                .eventType(eventType)
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .orderNumber(payment.getOrderNumber())
                .payload(payload)
                .deliveryStatus(WebhookDeliveryStatus.PENDING)
                .build();

        WebhookEvent saved = webhookRepository.save(event);

        // Simulate delay (real gateway fires webhook asynchronously)
        simulateDelay();

        // Attempt delivery
        deliver(saved, payload);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scheduled retry — runs every 30 seconds
    // ─────────────────────────────────────────────────────────────────────────

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void retryFailedWebhooks() {
        List<WebhookEvent> pending = webhookRepository.findPendingRetries();
        if (pending.isEmpty()) return;

        log.info("Retrying {} pending webhook(s)", pending.size());

        pending.forEach(event -> {
            event.setDeliveryStatus(WebhookDeliveryStatus.RETRYING);
            event.setRetryCount(event.getRetryCount() + 1);
            event.setLastAttemptedAt(LocalDateTime.now());
            webhookRepository.save(event);

            deliver(event, event.getPayload());
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void deliver(WebhookEvent event, String payload) {
        String targetUrl = orderServiceUrl + "/api/v1/webhooks/payment";

        try {
            // Build HMAC signature
            String signature = computeSignature(payload);

            // Use Java's built-in HttpClient (no extra deps)
            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("X-Webhook-Event", event.getEventType().name())
                    .header("X-Webhook-Event-Id", event.getEventId())
                    .header("X-Webhook-Signature", signature)
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload))
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();

            var client   = java.net.http.HttpClient.newHttpClient();
            var response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                event.setDeliveryStatus(WebhookDeliveryStatus.DELIVERED);
                event.setDeliveredAt(LocalDateTime.now());
                event.setResponseStatusCode(response.statusCode());
                event.setResponseBody(truncate(response.body(), 500));
                log.info("Webhook delivered: eventId={}, status={}",
                        event.getEventId(), response.statusCode());
            } else {
                markFailed(event, "HTTP " + response.statusCode()
                        + ": " + truncate(response.body(), 200));
                log.warn("Webhook delivery failed with HTTP {}: eventId={}",
                        response.statusCode(), event.getEventId());
            }

        } catch (Exception ex) {
            markFailed(event, ex.getMessage());
            log.error("Webhook delivery error for eventId={}: {}",
                    event.getEventId(), ex.getMessage());
        }

        webhookRepository.save(event);
    }

    private void markFailed(WebhookEvent event, String reason) {
        if (event.getRetryCount() >= 3) {
            event.setDeliveryStatus(WebhookDeliveryStatus.FAILED);
            log.warn("Webhook permanently failed after {} retries: eventId={}",
                    event.getRetryCount(), event.getEventId());
        } else {
            event.setDeliveryStatus(WebhookDeliveryStatus.RETRYING);
        }
        event.setFailureReason(truncate(reason, 300));
        event.setLastAttemptedAt(LocalDateTime.now());
    }

    private String computeSignature(String payload) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return "sha256=" + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Failed to compute webhook signature: {}", e.getMessage());
            return "sha256=error";
        }
    }

    private String generateEventId() {
        return "evt_" + UUID.randomUUID().toString().replace("-", "");
    }

    private void simulateDelay() {
        try {
            Thread.sleep(webhookDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) : s;
    }
}
