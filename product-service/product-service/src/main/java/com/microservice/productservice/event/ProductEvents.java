package com.microservice.productservice.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductEvents {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProductCreatedEvent {
        private Long productId;
        private String name;
        private String sku;
        private BigDecimal price;
        private Long categoryId;
        private String categoryName;
        private Long createdBy;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProductUpdatedEvent {
        private Long productId;
        private String name;
        private String sku;
        private BigDecimal price;
        private String status;
        private Long categoryId;
        private LocalDateTime updatedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProductDeletedEvent {
        private Long productId;
        private String sku;
        private Long deletedBy;
        private String deletedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class InventoryUpdatedEvent {
        private Long productId;
        private String sku;
        private String transactionType;
        private Integer quantityChange;
        private Integer newStockLevel;
        private String reason;
        private LocalDateTime updatedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LowStockAlertEvent {
        private Long productId;
        private String productName;
        private String sku;
        private Integer currentStock;
        private Integer threshold;
        private LocalDateTime alertedAt;
    }
}
