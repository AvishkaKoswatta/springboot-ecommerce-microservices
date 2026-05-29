package com.microservice.orderservice.dto;

import com.microservice.orderservice.entity.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/* ─────────────────────────────────────────────────────────────────
   REQUEST DTOs
───────────────────────────────────────────────────────────────── */

public class OrderDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ShippingAddressRequest {
        @NotBlank private String recipientName;
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number")
        private String phone;
        @NotBlank private String addressLine1;
        private String addressLine2;
        @NotBlank private String city;
        private String state;
        @NotBlank private String postalCode;
        @NotBlank private String country;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CartItemRequest {
        @NotNull(message = "Product ID required")
        private Long productId;
        @NotNull @Min(1) @Max(100)
        private Integer quantity;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PlaceOrderRequest {
        @NotNull @Valid
        private List<CartItemRequest> items;
        @NotNull @Valid
        private ShippingAddressRequest shippingAddress;

        @Size(max = 500)
        private String customerNotes;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CancelOrderRequest {
        @NotBlank @Size(max = 500)
        private String reason;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateOrderStatusRequest {
        @NotNull private OrderStatus status;
        @Size(max = 500) private String note;
        private String trackingNumber;
    }

    /* ─────────────────────────────────────────────────────────────────
       RESPONSE DTOs
    ───────────────────────────────────────────────────────────────── */

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ShippingAddressDto {
        private String recipientName;
        private String phone;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderItemDto {
        private Long id;
        private Long productId;
        private String productName;
        private String productSku;
        private String productImageUrl;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private OrderItemStatus itemStatus;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderStatusHistoryDto {
        private OrderStatus fromStatus;
        private OrderStatus toStatus;
        private String note;
        private String changedByRole;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderDto {
        private Long id;
        private String orderNumber;
        private Long userId;
        private String userEmail;
        private String userName;
        private OrderStatus status;
        private String paymentStatus;

        private BigDecimal subtotal;
        private BigDecimal shippingFee;
        private BigDecimal discountAmount;
        private BigDecimal totalAmount;
        private String paymentReference;
        private ShippingAddressDto shippingAddress;
        private String customerNotes;
        private String adminNotes;
        private String trackingNumber;
        private LocalDateTime shippedAt;
        private LocalDateTime deliveredAt;
        private LocalDateTime cancelledAt;
        private String cancellationReason;
        private int totalItemCount;
        private List<OrderItemDto> items;
        private List<OrderStatusHistoryDto> statusHistory;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /* Summary for list endpoints (no items/history) */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderSummaryDto {
        private Long id;
        private String orderNumber;
        private OrderStatus status;
        private BigDecimal totalAmount;
        private int totalItemCount;
        private String trackingNumber;
        private LocalDateTime createdAt;
    }
}
