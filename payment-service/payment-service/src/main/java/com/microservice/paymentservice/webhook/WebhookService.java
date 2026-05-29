package com.microservice.paymentservice.webhook;

import com.microservice.paymentservice.dto.PaymentDtos.WebhookEventDto;
import com.microservice.paymentservice.entity.WebhookDeliveryStatus;
import com.microservice.paymentservice.mapper.PaymentMapper;
import com.microservice.paymentservice.repository.WebhookEventRepository;
import com.microservice.paymentservice.response.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final WebhookEventRepository webhookRepository;
    private final PaymentMapper          mapper;

    @Value("${application.mock-gateway.webhook-secret}")
    private String webhookSecret;

    /**
     * Verifies and acknowledges an inbound webhook.
     * Returns true if the signature is valid and the event is not a duplicate.
     */
    @Transactional
    public boolean handleInboundWebhook(String eventId, String eventType,
                                         String signature, String payload) {
        // Idempotency — skip if already processed
        if (webhookRepository.existsByEventId(eventId)) {
            log.info("Duplicate webhook event received: eventId={} — skipping", eventId);
            return true; // return 200 so sender doesn't retry
        }

        // Verify signature
        if (!verifySignature(payload, signature)) {
            log.warn("Invalid webhook signature for eventId={}", eventId);
            return false;
        }

        log.info("Webhook received and verified: eventId={}, type={}", eventId, eventType);
        return true;
    }

    public boolean verifySignature(String payload, String receivedSignature) {
        try {
            String expected = computeSignature(payload);
            return expected.equals(receivedSignature);
        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }

    @Transactional(readOnly = true)
    public PagedResponse<WebhookEventDto> getWebhooksByPayment(Long paymentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<WebhookEventDto> result = webhookRepository
                .findAllByPaymentId(paymentId, pageable)
                .map(mapper::toWebhookDto);
        return PagedResponse.from(result);
    }

    @Transactional(readOnly = true)
    public PagedResponse<WebhookEventDto> getWebhooksByOrder(Long orderId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<WebhookEventDto> result = webhookRepository
                .findAllByOrderId(orderId, pageable)
                .map(mapper::toWebhookDto);
        return PagedResponse.from(result);
    }

    @Transactional(readOnly = true)
    public PagedResponse<WebhookEventDto> getAllWebhooks(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<WebhookEventDto> result = (status != null
                ? webhookRepository.findAllByDeliveryStatus(
                        WebhookDeliveryStatus.valueOf(status), pageable)
                : webhookRepository.findAll(pageable))
                .map(mapper::toWebhookDto);
        return PagedResponse.from(result);
    }

    private String computeSignature(String payload) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(new SecretKeySpec(
                webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return "sha256=" + Base64.getEncoder().encodeToString(hash);
    }
}
