package com.microservice.orderservice.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderEvents {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderPlacedEvent {
        private Long orderId;
        private String orderNumber;
        private Long userId;
        private String userEmail;
        private String userName;
        private BigDecimal totalAmount;
        private List<OrderItemInfo> items;
        private LocalDateTime placedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderConfirmedEvent {
        private Long orderId;
        private String orderNumber;
        private List<OrderItemInfo> items;
        private Long userId;
        private String userEmail;
        private String userName;
        private BigDecimal totalAmount;
        private LocalDateTime confirmedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderCancelledEvent {
        private Long orderId;
        private String orderNumber;
        private Long userId;
        private String userEmail;
        private String reason;
        private List<OrderItemInfo> items;   // to release reservations
        private LocalDateTime cancelledAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderCompletedEvent {
        private Long orderId;
        private String orderNumber;
        private Long userId;
        private BigDecimal totalAmount;
        private LocalDateTime completedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RefundRequestedEvent {
        private Long orderId;
        private String orderNumber;
        private Long userId;
        private String userEmail;
        private BigDecimal requestedAmount;
        private String reasonType;
        private String reasonDetail;
        private LocalDateTime requestedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderItemInfo {
        private Long productId;
        private String productSku;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}
