package com.microservice.orderservice.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSuccessEvent {

    private Long paymentId;
    private Long orderId;
    private String orderNumber;

    private Long userId;
    private String userEmail;

    private BigDecimal amount;
    private String currency;

    private String paymentMethod;
    private String paymentReference;
    private String gatewayTransactionId;

    private LocalDateTime confirmedAt;
    private String status;
}