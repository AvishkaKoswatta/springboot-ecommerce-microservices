package com.microservice.paymentservice.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentEvents {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PaymentSuccessEvent {
        private Long    paymentId;
        private Long    orderId;
        private String  orderNumber;
        private Long    userId;
        private String  userEmail;
        private BigDecimal amount;
        private String  currency;
        private String  paymentMethod;
        private String  paymentReference;
        private String  gatewayTransactionId;
        private LocalDateTime confirmedAt;

        // PaymentEvents.java — PaymentSuccessEvent
        private String status;   // add this field

    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PaymentFailedEvent {
        private Long    paymentId;
        private Long    orderId;
        private String  orderNumber;
        private Long    userId;
        private String  userEmail;
        private BigDecimal amount;
        private String  paymentReference;
        private String  gatewayResponseCode;
        private String  failureReason;
        private LocalDateTime failedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RefundProcessedEvent {
        private Long    paymentId;
        private Long    orderId;
        private String  orderNumber;
        private Long    userId;
        private String  userEmail;
        private String  refundReference;
        private BigDecimal refundAmount;
        private BigDecimal totalRefunded;
        private String  reason;
        private LocalDateTime processedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RefundFailedEvent {
        private Long    paymentId;
        private Long    orderId;
        private String  orderNumber;
        private String  refundReference;
        private BigDecimal refundAmount;
        private String  failureReason;
        private LocalDateTime failedAt;
    }
}
