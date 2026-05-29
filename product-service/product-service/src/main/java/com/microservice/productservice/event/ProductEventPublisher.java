package com.microservice.productservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${application.rabbitmq.exchange}")
    private String exchange;

    @Value("${application.rabbitmq.routing-keys.product-created}")
    private String productCreatedKey;

    @Value("${application.rabbitmq.routing-keys.product-updated}")
    private String productUpdatedKey;

    @Value("${application.rabbitmq.routing-keys.product-deleted}")
    private String productDeletedKey;

    @Value("${application.rabbitmq.routing-keys.inventory-updated}")
    private String inventoryUpdatedKey;

    @Value("${application.rabbitmq.routing-keys.low-stock-alert}")
    private String lowStockAlertKey;

    @Async("asyncTaskExecutor")
    public void publishProductCreated(ProductEvents.ProductCreatedEvent event) {
        publish(productCreatedKey, event);
        log.info("Published ProductCreatedEvent for product ID: {}", event.getProductId());
    }

    @Async("asyncTaskExecutor")
    public void publishProductUpdated(ProductEvents.ProductUpdatedEvent event) {
        publish(productUpdatedKey, event);
        log.info("Published ProductUpdatedEvent for product ID: {}", event.getProductId());
    }

    @Async("asyncTaskExecutor")
    public void publishProductDeleted(ProductEvents.ProductDeletedEvent event) {
        publish(productDeletedKey, event);
        log.info("Published ProductDeletedEvent for product ID: {}", event.getProductId());
    }

    @Async("asyncTaskExecutor")
    public void publishInventoryUpdated(ProductEvents.InventoryUpdatedEvent event) {
        publish(inventoryUpdatedKey, event);
        log.info("Published InventoryUpdatedEvent for product ID: {}", event.getProductId());
    }

    @Async("asyncTaskExecutor")
    public void publishLowStockAlert(ProductEvents.LowStockAlertEvent event) {
        publish(lowStockAlertKey, event);
        log.warn("Published LowStockAlertEvent for product ID: {} (stock: {})",
                event.getProductId(), event.getCurrentStock());
    }

    private void publish(String routingKey, Object payload) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, payload);
        } catch (Exception e) {
            log.error("Failed to publish event to RabbitMQ with key {}: {}", routingKey, e.getMessage());
        }
    }
}
