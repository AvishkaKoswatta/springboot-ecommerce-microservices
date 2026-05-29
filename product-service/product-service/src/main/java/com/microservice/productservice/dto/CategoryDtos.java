package com.microservice.productservice.dto;

import com.microservice.productservice.entity.ProductStatus;
import com.microservice.productservice.entity.ReviewStatus;
import com.microservice.productservice.entity.TransactionType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// ─────────────────────────────────────────────────
// CATEGORY DTOs
// ─────────────────────────────────────────────────

public class CategoryDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateCategoryRequest {
        @NotBlank(message = "Category name is required")
        @Size(max = 100)
        private String name;

        @Size(max = 500)
        private String description;

        private String imageUrl;

        private Long parentId;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateCategoryRequest {
        @Size(max = 100)
        private String name;

        @Size(max = 500)
        private String description;

        private String imageUrl;

        private Long parentId;

        private Boolean active;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CategoryDto {
        private Long id;
        private String name;
        private String slug;
        private String description;
        private String imageUrl;
        private Boolean active;
        private Long parentId;
        private String parentName;
        private List<CategoryDto> children;
        private int productCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
