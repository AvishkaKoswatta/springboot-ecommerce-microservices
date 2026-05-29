package com.microservice.productservice.controller;

import com.microservice.productservice.dto.InventoryDtos.*;
import com.microservice.productservice.response.ApiResponse;
import com.microservice.productservice.response.PagedResponse;
import com.microservice.productservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<InventoryDto>> getInventory(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success("Inventory retrieved",
                inventoryService.getInventory(productId)));
    }

    @PostMapping("/{productId}/adjust")
    public ResponseEntity<ApiResponse<InventoryTransactionDto>> adjustStock(
            @PathVariable Long productId,
            @Valid @RequestBody AdjustStockRequest request) {
        // In production extract performedBy from JWT claim
        InventoryTransactionDto tx = inventoryService.adjustStock(productId, request, 0L);
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted", tx));
    }

    @GetMapping("/{productId}/transactions")
    public ResponseEntity<ApiResponse<PagedResponse<InventoryTransactionDto>>> getTransactionHistory(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Transaction history retrieved",
                inventoryService.getTransactionHistory(productId, page, size)));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<InventoryDto>>> getLowStockProducts() {
        return ResponseEntity.ok(ApiResponse.success("Low-stock products retrieved",
                inventoryService.getLowStockProducts()));
    }
}
