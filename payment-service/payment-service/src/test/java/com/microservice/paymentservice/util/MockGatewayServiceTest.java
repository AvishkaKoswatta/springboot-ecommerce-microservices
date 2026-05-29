package com.microservice.paymentservice.util;

import com.microservice.paymentservice.dto.PaymentDtos.MockCardDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class MockGatewayServiceTest {

    private MockGatewayService gateway;

    @BeforeEach
    void setUp() {
        gateway = new MockGatewayService();
        ReflectionTestUtils.setField(gateway, "successRate", 0.85);
        ReflectionTestUtils.setField(gateway, "processingDelayMs", 0L); // no delay in tests
        ReflectionTestUtils.setField(gateway, "refundDelayMs", 0L);
    }

    @Test
    @DisplayName("processPayment() — card 4242424242424242 always succeeds")
    void alwaysSuccessCard() {
        MockCardDetails card = new MockCardDetails(
                "4242424242424242", "12", "2030", "123", "Test User");

        var result = gateway.processPayment(card, new BigDecimal("50.00"), "USD");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResponseCode()).isEqualTo("00");
    }

    @Test
    @DisplayName("processPayment() — card 4000000000000002 always declines")
    void alwaysDeclinedCard() {
        MockCardDetails card = new MockCardDetails(
                "4000000000000002", "12", "2030", "123", "Test User");

        var result = gateway.processPayment(card, new BigDecimal("50.00"), "USD");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getResponseCode()).isEqualTo("05");
    }

    @Test
    @DisplayName("processPayment() — card 4000000000009995 returns insufficient funds")
    void insufficientFundsCard() {
        MockCardDetails card = new MockCardDetails(
                "4000000000009995", "12", "2030", "123", "Test User");

        var result = gateway.processPayment(card, new BigDecimal("50.00"), "USD");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getResponseCode()).isEqualTo("51");
    }

    @Test
    @DisplayName("processPayment() — CVV 000 always fails")
    void cvv000AlwaysFails() {
        MockCardDetails card = new MockCardDetails(
                "4242424242424242", "12", "2030", "000", "Test User");

        var result = gateway.processPayment(card, new BigDecimal("50.00"), "USD");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getResponseCode()).isEqualTo("82");
    }

    @Test
    @DisplayName("processPayment() — transaction ID is always generated")
    void transactionIdAlwaysGenerated() {
        MockCardDetails card = new MockCardDetails(
                "4242424242424242", "12", "2030", "123", "Test User");

        var result = gateway.processPayment(card, new BigDecimal("50.00"), "USD");

        assertThat(result.getTransactionId()).isNotBlank().startsWith("TXN-");
    }

    @RepeatedTest(10)
    @DisplayName("processPayment() — random card uses configured success rate")
    void randomCardSuccessRate() {
        // Run 200 transactions and check success rate is approximately right
        List<Boolean> outcomes = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            MockCardDetails card = new MockCardDetails(
                    "1234567890123456", "12", "2030", "123", "Random");
            outcomes.add(gateway.processPayment(
                    card, new BigDecimal("10.00"), "USD").isSuccess());
        }
        long successCount = outcomes.stream().filter(b -> b).count();
        double rate = (double) successCount / outcomes.size();
        // With 85% success rate, expect between 70% and 99% in 200 trials
        assertThat(rate).isBetween(0.70, 0.99);
    }

    @Test
    @DisplayName("processRefund() — generates refund transaction ID with REF- prefix")
    void processRefund_generatesRefundId() {
        var result = gateway.processRefund("TXN-ORIG-123",
                new BigDecimal("25.00"), "Customer return");

        assertThat(result.getTransactionId()).startsWith("REF-TXN-");
    }

    @Test
    @DisplayName("GatewayResponse.toJson() — produces valid JSON string")
    void gatewayResponseToJson() {
        var result = MockGatewayService.GatewayResponse.success("TXN-001", "00", "Approved");
        String json = result.toJson();

        assertThat(json).contains("\"success\":true");
        assertThat(json).contains("TXN-001");
        assertThat(json).contains("\"responseCode\":\"00\"");
    }
}
