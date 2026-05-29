package com.microservice.orderservice.event;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Consumes inventory events published by product-service.
 * Useful for: auto-cancelling pending orders if a product goes out of stock,
 * or logging alerts for admin awareness.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventConsumer {

    @RabbitListener(queues = "${application.rabbitmq.queues.inventory-updated}")
    public void onInventoryUpdated(InventoryUpdatedMessage message,
                                   Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            log.info("Inventory updated — product: {}, newStock: {}, type: {}",
                    message.productId(), message.newStockLevel(), message.transactionType());

            // If stock hits zero, we could query for PENDING orders containing this product
            // and notify them. Logic can be extended here.
            if (message.newStockLevel() != null && message.newStockLevel() <= 0) {
                log.warn("Product {} is now OUT OF STOCK. Review pending orders.", message.productId());
            }

            channel.basicAck(tag, false);
        } catch (Exception ex) {
            log.error("Error processing inventory update for product {}: {}", message.productId(), ex.getMessage());
            channel.basicNack(tag, false, true); // requeue
        }
    }

    @RabbitListener(queues = "${application.rabbitmq.queues.low-stock-alert}")
    public void onLowStockAlert(LowStockAlertMessage message,
                                Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            log.warn("LOW STOCK ALERT — product: {} ({}), current stock: {}, threshold: {}",
                    message.productName(), message.sku(), message.currentStock(), message.threshold());
            channel.basicAck(tag, false);
        } catch (Exception ex) {
            log.error("Error processing low-stock alert: {}", ex.getMessage());
            channel.basicNack(tag, false, true);
        }
    }

    // ── Inbound message shapes (match what product-service publishes) ──

    public record InventoryUpdatedMessage(
            Long productId,
            String sku,
            String transactionType,
            Integer quantityChange,
            Integer newStockLevel,
            String reason,
            String updatedAt
    ) {}

    public record LowStockAlertMessage(
            Long productId,
            String productName,
            String sku,
            Integer currentStock,
            Integer threshold,
            String alertedAt
    ) {}
}
