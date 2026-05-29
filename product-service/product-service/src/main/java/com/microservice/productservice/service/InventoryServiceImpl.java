package com.microservice.productservice.service;

import com.microservice.productservice.dto.InventoryDtos.*;
import com.microservice.productservice.entity.*;
import com.microservice.productservice.event.ProductEventPublisher;
import com.microservice.productservice.event.ProductEvents.*;
import com.microservice.productservice.exception.InsufficientStockException;
import com.microservice.productservice.exception.ProductNotFoundException;
import com.microservice.productservice.mapper.InventoryTransactionMapper;
import com.microservice.productservice.repository.InventoryTransactionRepository;
import com.microservice.productservice.repository.ProductRepository;
import com.microservice.productservice.response.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository              productRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final InventoryTransactionMapper     transactionMapper;
    private final ProductEventPublisher          eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public InventoryDto getInventory(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return toInventoryDto(product);
    }

    @Override
    @Transactional
    public InventoryTransactionDto adjustStock(Long productId, AdjustStockRequest req, Long performedBy) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        int quantityBefore = product.getStockQuantity();
        int quantityAfter;

        switch (req.getType()) {
            case STOCK_IN, RETURN, INITIAL_STOCK -> {
                productRepository.incrementStock(productId, req.getQuantity());
                quantityAfter = quantityBefore + req.getQuantity();
            }
            case STOCK_OUT, DAMAGED -> {
                if (product.getAvailableQuantity() < req.getQuantity() && !product.getAllowBackorder()) {
                    throw new InsufficientStockException(
                            "Insufficient stock. Available: " + product.getAvailableQuantity()
                            + ", requested: " + req.getQuantity());
                }
                int updated = productRepository.decrementStock(productId, req.getQuantity());
                if (updated == 0) {
                    throw new InsufficientStockException("Stock update failed — concurrent modification detected");
                }
                quantityAfter = quantityBefore - req.getQuantity();
            }
            case RESERVATION -> {
                if (product.getAvailableQuantity() < req.getQuantity()) {
                    throw new InsufficientStockException(
                            "Cannot reserve " + req.getQuantity() + " units. Available: "
                            + product.getAvailableQuantity());
                }
                productRepository.incrementReserved(productId, req.getQuantity());
                quantityAfter = quantityBefore; // stock doesn't change on reservation
            }
            case RESERVATION_CANCEL -> {
                productRepository.decrementReserved(productId, req.getQuantity());
                quantityAfter = quantityBefore;
            }
            case ADJUSTMENT -> {
                // Absolute adjustment: quantity represents the new total
                int delta = req.getQuantity() - quantityBefore;
                if (delta > 0) {
                    productRepository.incrementStock(productId, delta);
                } else if (delta < 0) {
                    productRepository.decrementStock(productId, Math.abs(delta));
                }
                quantityAfter = req.getQuantity();
            }
            default -> throw new IllegalArgumentException("Unsupported transaction type: " + req.getType());
        }

        // Reload fresh state for the audit record
        product = productRepository.findById(productId).orElseThrow();

        // Persist audit record
        InventoryTransaction tx = InventoryTransaction.builder()
                .product(product)
                .type(req.getType())
                .quantityChange(req.getQuantity())
                .quantityBefore(quantityBefore)
                .quantityAfter(quantityAfter)
                .reason(req.getReason())
                .referenceId(req.getReferenceId())
                .performedBy(performedBy)
                .build();
        InventoryTransaction saved = transactionRepository.save(tx);

        // Publish inventory event
        eventPublisher.publishInventoryUpdated(InventoryUpdatedEvent.builder()
                .productId(productId)
                .sku(product.getSku())
                .transactionType(req.getType().name())
                .quantityChange(req.getQuantity())
                .newStockLevel(quantityAfter)
                .reason(req.getReason())
                .updatedAt(LocalDateTime.now())
                .build());

        // Check and emit low-stock alert
        if (product.isLowStock()) {
            eventPublisher.publishLowStockAlert(LowStockAlertEvent.builder()
                    .productId(productId)
                    .productName(product.getName())
                    .sku(product.getSku())
                    .currentStock(product.getAvailableQuantity())
                    .threshold(product.getLowStockThreshold())
                    .alertedAt(LocalDateTime.now())
                    .build());
        }

        log.info("Stock adjusted — product: {}, type: {}, qty: {}, after: {}",
                productId, req.getType(), req.getQuantity(), quantityAfter);
        return transactionMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InventoryTransactionDto> getTransactionHistory(Long productId, int page, int size) {
        productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
        Pageable pageable = PageRequest.of(page, size);
        Page<InventoryTransactionDto> result = transactionRepository
                .findAllByProductIdOrderByCreatedAtDesc(productId, pageable)
                .map(transactionMapper::toDto);
        return PagedResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto> getLowStockProducts() {
        return productRepository.findLowStockProducts()
                .stream().map(this::toInventoryDto).collect(Collectors.toList());
    }

    @Override
    @Scheduled(cron = "0 0 8 * * *") // Every day at 8 AM
    @Transactional(readOnly = true)
    public void checkAndPublishLowStockAlerts() {
        log.info("Running scheduled low-stock check at {}", LocalDateTime.now());
        List<Product> lowStock = productRepository.findLowStockProducts();
        lowStock.forEach(p -> eventPublisher.publishLowStockAlert(
                LowStockAlertEvent.builder()
                        .productId(p.getId())
                        .productName(p.getName())
                        .sku(p.getSku())
                        .currentStock(p.getAvailableQuantity())
                        .threshold(p.getLowStockThreshold())
                        .alertedAt(LocalDateTime.now())
                        .build()));
        log.info("Low-stock check complete. {} product(s) below threshold.", lowStock.size());
    }

    // ─────────────────────────────────────────────────────────────────────────

    private InventoryDto toInventoryDto(Product p) {
        return InventoryDto.builder()
                .productId(p.getId())
                .productName(p.getName())
                .sku(p.getSku())
                .stockQuantity(p.getStockQuantity())
                .reservedQuantity(p.getReservedQuantity())
                .availableQuantity(p.getAvailableQuantity())
                .lowStockThreshold(p.getLowStockThreshold())
                .trackInventory(p.getTrackInventory())
                .allowBackorder(p.getAllowBackorder())
                .inStock(p.isInStock())
                .lowStock(p.isLowStock())
                .build();
    }
}
