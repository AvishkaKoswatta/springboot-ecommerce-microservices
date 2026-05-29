package com.microservice.productservice.service;

import com.microservice.productservice.dto.InventoryDtos.*;
import com.microservice.productservice.response.PagedResponse;

import java.util.List;

public interface InventoryService {
    InventoryDto getInventory(Long productId);
    InventoryTransactionDto adjustStock(Long productId, AdjustStockRequest request, Long performedBy);
    PagedResponse<InventoryTransactionDto> getTransactionHistory(Long productId, int page, int size);
    List<InventoryDto> getLowStockProducts();
    void checkAndPublishLowStockAlerts();
}
