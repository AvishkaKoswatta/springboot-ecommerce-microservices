package com.microservice.productservice.dto;

import com.microservice.productservice.entity.TransactionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

public class InventoryDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AdjustStockRequest {
        @NotNull(message = "Quantity is required")
        private Integer quantity;

        @NotNull(message = "Transaction type is required")
        private TransactionType type;

        @Size(max = 200)
        private String reason;

        @Size(max = 100)
        private String referenceId;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class InventoryDto {
        private Long productId;
        private String productName;
        private String sku;
        private Integer stockQuantity;
        private Integer reservedQuantity;
        private Integer availableQuantity;
        private Integer lowStockThreshold;
        private Boolean trackInventory;
        private Boolean allowBackorder;
        private boolean inStock;
        private boolean lowStock;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class InventoryTransactionDto {
        private Long id;
        private Long productId;
        private TransactionType type;
        private Integer quantityChange;
        private Integer quantityBefore;
        private Integer quantityAfter;
        private String reason;
        private String referenceId;
        private Long performedBy;
        private LocalDateTime createdAt;
    }
}
