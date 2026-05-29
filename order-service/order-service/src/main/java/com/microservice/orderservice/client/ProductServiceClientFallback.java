package com.microservice.orderservice.client;

import com.microservice.orderservice.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component @Slf4j
public class ProductServiceClientFallback implements ProductServiceClient {

    @Override
    public ApiResponse<ProductDto> getProductById(Long id) {
        log.warn("product-service unavailable for product id: {}", id);
        return ApiResponse.<ProductDto>builder().success(false)
                .message("product-service unavailable").build();
    }

    @Override
    public ApiResponse<Void> adjustStock(Long productId, StockAdjustRequest request, String token) {
        log.warn("product-service unavailable — stock reservation skipped for product: {}", productId);
        return ApiResponse.<Void>builder().success(false)
                .message("product-service unavailable").build();
    }
}
