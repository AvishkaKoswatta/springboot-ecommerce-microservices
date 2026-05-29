package com.microservice.paymentservice.dto;

import com.microservice.paymentservice.entity.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentDtos {

    // ─── Requests ─────────────────────────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class InitiatePaymentRequest {
        @NotNull  private Long orderId;
        @NotBlank private String orderNumber;
        @NotNull  @DecimalMin("0.01") private BigDecimal amount;
        @NotBlank private String currency;
        @NotNull  private PaymentMethod paymentMethod;
       // @NotNull  private Long userId;
        @NotBlank private String userEmail;
        // Mock card details — validated but never stored
        private MockCardDetails cardDetails;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MockCardDetails {
        @NotBlank private String cardNumber;   // e.g. 4242424242424242 = success
        @NotBlank private String expiryMonth;
        @NotBlank private String expiryYear;
        @NotBlank private String cvv;
        @NotBlank private String cardholderName;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProcessPaymentRequest {

        @NotBlank
        private String paymentReference;

        @jakarta.validation.Valid
        private MockCardDetails cardDetails;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RefundRequest {
        @NotBlank private String paymentReference;
        @NotNull  @DecimalMin("0.01") private BigDecimal amount;
        @Size(max = 300) private String reason;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CancelPaymentRequest {
        @NotBlank private String paymentReference;
        @Size(max = 200) private String reason;
    }

    // ─── Response DTOs ────────────────────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PaymentDto {
        private Long id;
        private Long orderId;
        private String orderNumber;
        private Long userId;
        private String userEmail;
        private String paymentReference;
        private String gatewayTransactionId;
        private BigDecimal amount;
        private String currency;
        private PaymentMethod paymentMethod;
        private PaymentStatus status;
        private String gatewayResponseCode;
        private String gatewayResponseMessage;
        private LocalDateTime initiatedAt;
        private LocalDateTime confirmedAt;
        private LocalDateTime failedAt;
        private LocalDateTime expiresAt;
        private BigDecimal refundedAmount;
        private BigDecimal refundableAmount;
        private Boolean webhookDelivered;
        private List<RefundDto> refunds;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RefundDto {
        private Long id;
        private Long paymentId;
        private String paymentReference;
        private String refundReference;
        private String gatewayRefundId;
        private BigDecimal amount;
        private RefundStatus status;
        private String reason;
        private Long requestedBy;
        private String gatewayResponseCode;
        private String gatewayResponseMessage;
        private LocalDateTime processedAt;
        private LocalDateTime failedAt;
        private String failureReason;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class WebhookEventDto {
        private Long id;
        private String eventId;
        private WebhookEventType eventType;
        private Long paymentId;
        private Long orderId;
        private String orderNumber;
        private String payload;
        private WebhookDeliveryStatus deliveryStatus;
        private Integer retryCount;
        private LocalDateTime lastAttemptedAt;
        private LocalDateTime deliveredAt;
        private Integer responseStatusCode;
        private String failureReason;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PaymentSummaryDto {
        private Long id;
        private String orderNumber;
        private BigDecimal amount;
        private String currency;
        private PaymentMethod paymentMethod;
        private PaymentStatus status;
        private String paymentReference;
        private LocalDateTime createdAt;
    }
}
