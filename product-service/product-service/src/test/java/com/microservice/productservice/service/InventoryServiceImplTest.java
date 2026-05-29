package com.microservice.productservice.service;

import com.microservice.productservice.dto.InventoryDtos.*;
import com.microservice.productservice.entity.*;
import com.microservice.productservice.event.ProductEventPublisher;
import com.microservice.productservice.exception.InsufficientStockException;
import com.microservice.productservice.exception.ProductNotFoundException;
import com.microservice.productservice.mapper.InventoryTransactionMapper;
import com.microservice.productservice.repository.InventoryTransactionRepository;
import com.microservice.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock private ProductRepository              productRepository;
    @Mock private InventoryTransactionRepository transactionRepository;
    @Mock private InventoryTransactionMapper     transactionMapper;
    @Mock private ProductEventPublisher          eventPublisher;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L).name("Test Product").sku("SKU-001")
                .price(new BigDecimal("99.99")).status(ProductStatus.ACTIVE)
                .stockQuantity(50).reservedQuantity(5)
                .lowStockThreshold(10).trackInventory(true)
                .allowBackorder(false).createdBy(1L)
                .build();
    }

    @Test
    @DisplayName("adjustStock() STOCK_IN - should increment stock")
    void adjustStock_stockIn_success() {
        AdjustStockRequest req = new AdjustStockRequest(10, TransactionType.STOCK_IN, "Restock", null);
        InventoryTransaction savedTx = InventoryTransaction.builder()
                .id(1L).product(testProduct).type(TransactionType.STOCK_IN)
                .quantityChange(10).quantityBefore(50).quantityAfter(60).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepository).incrementStock(1L, 10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(transactionRepository.save(any())).thenReturn(savedTx);
        when(transactionMapper.toDto(savedTx)).thenReturn(new InventoryTransactionDto());
        doNothing().when(eventPublisher).publishInventoryUpdated(any());

        assertThatCode(() -> inventoryService.adjustStock(1L, req, 1L))
                .doesNotThrowAnyException();
        verify(productRepository).incrementStock(1L, 10);
    }

    @Test
    @DisplayName("adjustStock() STOCK_OUT - should throw when insufficient stock")
    void adjustStock_stockOut_insufficientStock_throws() {
        // Request more than available (50 - 5 reserved = 45 available)
        AdjustStockRequest req = new AdjustStockRequest(100, TransactionType.STOCK_OUT, "Sale", null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> inventoryService.adjustStock(1L, req, 1L))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("adjustStock() RESERVATION - should throw when insufficient available quantity")
    void adjustStock_reservation_insufficient_throws() {
        AdjustStockRequest req = new AdjustStockRequest(50, TransactionType.RESERVATION, "Cart", null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> inventoryService.adjustStock(1L, req, 1L))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @DisplayName("getInventory() - should return inventory dto")
    void getInventory_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        InventoryDto result = inventoryService.getInventory(1L);

        assertThat(result).isNotNull();
        assertThat(result.getStockQuantity()).isEqualTo(50);
        assertThat(result.getAvailableQuantity()).isEqualTo(45);
    }

    @Test
    @DisplayName("getInventory() - should throw when product not found")
    void getInventory_notFound_throws() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getInventory(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("getLowStockProducts() - should return list of low-stock inventory")
    void getLowStockProducts_returnsList() {
        // Set stock to 5 (below threshold of 10)
        testProduct.setStockQuantity(5);
        when(productRepository.findLowStockProducts()).thenReturn(java.util.List.of(testProduct));

        var result = inventoryService.getLowStockProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isLowStock()).isTrue();
    }
}
