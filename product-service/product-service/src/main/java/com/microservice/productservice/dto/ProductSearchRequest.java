package com.microservice.productservice.dto;

import com.microservice.productservice.entity.ProductStatus;
import lombok.*;

import java.util.List;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchRequest {

    private String query;

    private Long categoryId;

    private List<Long> categoryIds;

    private String brand;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private ProductStatus status;

    private Boolean featured;

    private Boolean inStock;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDir = "desc";
}
