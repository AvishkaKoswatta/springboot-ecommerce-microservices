package com.microservice.paymentservice.util;

import com.microservice.paymentservice.dto.PaymentDtos.MockCardDetails;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Simulates a payment gateway.
 *
 * Card number rules (like Stripe test cards):
 *   4242424242424242 → always succeeds
 *   4000000000000002 → always declined
 *   4000000000009995 → insufficient funds
 *   any other        → random outcome based on successRate
 *
 * CVV "000" → always fails (CVV check failure)
 */
@Service
@Slf4j
public class MockGatewayService {

    @Value("${application.mock-gateway.success-rate:0.85}")
    private double successRate;

    @Value("${application.mock-gateway.processing-delay-ms:800}")
    private long processingDelayMs;

    @Value("${application.mock-gateway.refund-delay-ms:500}")
    private long refundDelayMs;

    private static final Map<String, GatewayDecision> CARD_RULES = Map.of(
            "4242424242424242", new GatewayDecision(true,  "00", "Approved"),
            "4000000000000002", new GatewayDecision(false, "05", "Do not honour"),
            "4000000000009995", new GatewayDecision(false, "51", "Insufficient funds"),
            "4000000000000069", new GatewayDecision(false, "54", "Expired card"),
            "4000000000000127", new GatewayDecision(false, "82", "CVV check failed")
    );

    private final Random random = new Random();

    public GatewayResponse processPayment(MockCardDetails card, BigDecimal amount, String currency) {
        simulateNetworkDelay(processingDelayMs);

        // CVV 000 always fails
        if ("000".equals(card.getCvv())) {
            return GatewayResponse.failure(
                    generateTransactionId(),
                    "82",
                    "CVV check failed",
                    "Card security code is invalid");
        }

        // Strip spaces from card number
        String cardNum = card.getCardNumber().replaceAll("\\s+", "");

        // Check fixed test card rules
        if (CARD_RULES.containsKey(cardNum)) {
            GatewayDecision decision = CARD_RULES.get(cardNum);
            return decision.success
                    ? GatewayResponse.success(generateTransactionId(), decision.code, decision.message)
                    : GatewayResponse.failure(generateTransactionId(), decision.code, decision.message,
                      "Payment declined: " + decision.message);
        }

        // Random outcome for any other card
        boolean approved = random.nextDouble() < successRate;
        if (approved) {
            return GatewayResponse.success(generateTransactionId(), "00", "Approved");
        } else {
            return GatewayResponse.failure(generateTransactionId(), "05",
                    "Do not honour", "Payment declined by issuing bank");
        }
    }

    public GatewayResponse processRefund(String originalTransactionId,
                                          BigDecimal amount,
                                          String reason) {
        simulateNetworkDelay(refundDelayMs);

        // 95% refund success rate (refunds are more reliable than payments)
        boolean success = random.nextDouble() < 0.95;

        if (success) {
            return GatewayResponse.success(
                    "REF-" + generateTransactionId(),
                    "00",
                    "Refund approved");
        } else {
            return GatewayResponse.failure(
                    "REF-" + generateTransactionId(),
                    "99",
                    "Refund failed",
                    "Gateway unable to process refund at this time");
        }
    }

    private void simulateNetworkDelay(long ms) {
        try {
            // Add ±20% jitter to make it feel realistic
            long jitter = (long) (ms * 0.2);
            long delay  = ms - jitter + (long) (random.nextDouble() * jitter * 2);
            Thread.sleep(Math.max(0, delay));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    // ─── Inner types ──────────────────────────────────────────────────────────

    @Getter
    @AllArgsConstructor
    public static class GatewayResponse {
        private final boolean success;
        private final String  transactionId;
        private final String  responseCode;
        private final String  responseMessage;
        private final String  failureDetail;
        private final LocalDateTime processedAt;

        public static GatewayResponse success(String txnId, String code, String msg) {
            return new GatewayResponse(true, txnId, code, msg, null, LocalDateTime.now());
        }

        public static GatewayResponse failure(String txnId, String code, String msg, String detail) {
            return new GatewayResponse(false, txnId, code, msg, detail, LocalDateTime.now());
        }

        public String toJson() {
            return """
                    {"success":%b,"transactionId":"%s","responseCode":"%s",
                    "responseMessage":"%s","processedAt":"%s"}
                    """.formatted(success, transactionId, responseCode, responseMessage, processedAt);
        }
    }

    @AllArgsConstructor
    private static class GatewayDecision {
        boolean success;
        String  code;
        String  message;
    }
}
