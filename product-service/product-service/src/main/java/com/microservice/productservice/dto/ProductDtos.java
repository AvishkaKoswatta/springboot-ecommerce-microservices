package com.microservice.productservice.dto;

import com.microservice.productservice.entity.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ProductDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProductImageRequest {
        @NotBlank(message = "Image URL is required")
        private String url;

        private String altText;

        @Min(0)
        private Integer displayOrder;

        private Boolean primary;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateProductRequest {

        @NotBlank(message = "Product name is required")
        @Size(max = 200)
        private String name;

        @Size(max = 100)
        private String sku;

        private String description;

        @Size(max = 500)
        private String shortDescription;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 10, fraction = 2)
        private BigDecimal price;

        @DecimalMin(value = "0.00")
        @Digits(integer = 10, fraction = 2)
        private BigDecimal compareAtPrice;

        @DecimalMin(value = "0.00")
        @Digits(integer = 10, fraction = 2)
        private BigDecimal costPrice;

        private ProductStatus status;

        private Long categoryId;

        @Size(max = 100)
        private String brand;

        @Min(0)
        private Integer weightGrams;

        @Size(max = 500)
        private String tags;

        private Boolean featured;

        @Min(0)
        private Integer stockQuantity;

        @Min(0)
        private Integer lowStockThreshold;

        private Boolean trackInventory;

        private Boolean allowBackorder;

        @Valid
        private List<ProductImageRequest> images;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateProductRequest {

        @Size(max = 200)
        private String name;

        private String description;

        @Size(max = 500)
        private String shortDescription;

        @DecimalMin(value = "0.01")
        @Digits(integer = 10, fraction = 2)
        private BigDecimal price;

        @DecimalMin(value = "0.00")
        @Digits(integer = 10, fraction = 2)
        private BigDecimal compareAtPrice;

        @DecimalMin(value = "0.00")
        @Digits(integer = 10, fraction = 2)
        private BigDecimal costPrice;

        private ProductStatus status;

        private Long categoryId;

        @Size(max = 100)
        private String brand;

        @Min(0)
        private Integer weightGrams;

        @Size(max = 500)
        private String tags;

        private Boolean featured;

        @Min(0)
        private Integer lowStockThreshold;

        private Boolean trackInventory;

        private Boolean allowBackorder;

        @Valid
        private List<ProductImageRequest> images;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProductImageDto {
        private Long id;
        private String url;
        private String altText;
        private Integer displayOrder;
        private Boolean primary;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProductDto {
        private Long id;
        private String name;
        private String slug;
        private String sku;
        private String description;
        private String shortDescription;
        private BigDecimal price;
        private BigDecimal compareAtPrice;
        private BigDecimal costPrice;
        private ProductStatus status;
        private Long categoryId;
        private String categoryName;
        private String brand;
        private Integer weightGrams;
        private String tags;
        private Boolean featured;
        private Long createdBy;
        private Integer stockQuantity;
        private Integer reservedQuantity;
        private Integer availableQuantity;
        private Integer lowStockThreshold;
        private Boolean trackInventory;
        private Boolean allowBackorder;
        private Boolean inStock;
        private Boolean lowStock;
        private List<ProductImageDto> images;
        private BigDecimal averageRating;
        private Integer reviewCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
