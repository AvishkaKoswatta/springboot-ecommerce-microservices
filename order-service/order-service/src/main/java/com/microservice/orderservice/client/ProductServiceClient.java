package com.microservice.orderservice.client;

import com.microservice.orderservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@FeignClient(
        name = "product-service",
        //url = "${services.product-service-url:}",
        fallback = ProductServiceClientFallback.class
)
public interface ProductServiceClient {

    @GetMapping("/products/{id}")
    ApiResponse<ProductDto> getProductById(@PathVariable Long id);

    @PostMapping("/inventory/{productId}/adjust")
    ApiResponse<Void> adjustStock(@PathVariable Long productId,
                                  @RequestBody StockAdjustRequest request,
                                  @RequestHeader("Authorization") String token);

    /* Minimal DTOs needed by order-service */
    record ProductDto(
            Long id,
            String name,
            String sku,
            String status,
            BigDecimal price,
            Integer availableQuantity,
            Boolean trackInventory,
            Boolean allowBackorder,
            String primaryImageUrl
    ) {}

    record StockAdjustRequest(Integer quantity, String type, String reason, String referenceId) {}
}
