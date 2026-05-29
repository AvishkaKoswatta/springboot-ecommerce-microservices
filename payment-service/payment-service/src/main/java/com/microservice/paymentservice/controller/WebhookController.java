package com.microservice.paymentservice.controller;

import com.microservice.paymentservice.response.ApiResponse;
import com.microservice.paymentservice.webhook.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Receives inbound webhook notifications.
 *
 * This endpoint is open (no JWT) — security is handled by
 * verifying the X-Webhook-Signature HMAC header.
 *
 * In this mock setup, payment-service sends webhooks to order-service
 * and also exposes this endpoint to receive them (for testing simulation).
 */
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookService webhookService;

    /**
     * Inbound webhook from mock gateway (or payment-service itself in sandbox mode).
     * order-service posts to this endpoint after receiving payment webhooks.
     */
    @PostMapping("/payment")
    public ResponseEntity<ApiResponse<String>> receivePaymentWebhook(
            @RequestHeader(value = "X-Webhook-Event",    required = false) String eventType,
            @RequestHeader(value = "X-Webhook-Event-Id", required = false) String eventId,
            @RequestHeader(value = "X-Webhook-Signature",required = false) String signature,
            @RequestBody String payload) {

        log.info("Inbound webhook received: eventId={}, type={}", eventId, eventType);

        if (eventId == null || eventType == null || signature == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Missing required webhook headers", 400));
        }

        boolean valid = webhookService.handleInboundWebhook(eventId, eventType, signature, payload);

        if (!valid) {
            log.warn("Webhook signature verification failed: eventId={}", eventId);
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid webhook signature", 401));
        }

        return ResponseEntity.ok(ApiResponse.success("Webhook acknowledged", eventId));
    }

    /**
     * Verify a webhook signature manually (useful for debugging).
     */
    @PostMapping("/verify-signature")
    public ResponseEntity<ApiResponse<Boolean>> verifySignature(
            @RequestHeader("X-Webhook-Signature") String signature,
            @RequestBody String payload) {

        boolean valid = webhookService.verifySignature(payload, signature);
        return ResponseEntity.ok(ApiResponse.success(
                valid ? "Signature valid" : "Signature invalid", valid));
    }
}
