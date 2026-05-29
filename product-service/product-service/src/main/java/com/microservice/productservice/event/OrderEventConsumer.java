package com.microservice.productservice.event;

import com.microservice.productservice.dto.InventoryDtos.AdjustStockRequest;
import com.microservice.productservice.entity.TransactionType;
import com.microservice.productservice.repository.ProductRepository;
import com.microservice.productservice.service.InventoryService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Listens to order events published by order-service and adjusts inventory.
 *
 * Stock management strategy:
 * ─────────────────────────────────────────────────────────────────────
 *  ORDER PLACED (COD)     → order-service Feign → STOCK_OUT immediately
 *  ORDER PLACED (Online)  → order-service Feign → RESERVATION
 *  PAYMENT CONFIRMED      → order-service Feign → RESERVATION_CANCEL + STOCK_OUT
 *  ORDER CANCELLED        → this consumer       → STOCK_IN (return stock)
 * ─────────────────────────────────────────────────────────────────────
 *
 * This consumer handles ONLY the cancellation stock return.
 * The onOrderPlaced listener has been intentionally removed because
 * order-service handles stock deduction synchronously via Feign.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final InventoryService  inventoryService;
    private final ProductRepository productRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // CONSUME: order.cancelled
    //
    // Triggered when:
    //   1. Customer cancels an order manually
    //   2. System auto-cancels due to payment timeout
    //   3. Admin cancels an order
    //   4. Product deleted → order auto-cancelled (product may no longer exist)
    //
    // Action: STOCK_IN for each item to restore stockQuantity.
    //
    // For online orders that had a RESERVATION:
    //   The RESERVATION was already cancelled by order-service in
    //   expireTimedOutOrders() — no RESERVATION_CANCEL needed here.
    //   We only do STOCK_IN to return the actual stock.
    //
    // For COD or confirmed online orders:
    //   STOCK_OUT was already done — we reverse it with STOCK_IN.
    // ─────────────────────────────────────────────────────────────────────────

    @RabbitListener(queues = "${application.rabbitmq.queues.order-cancelled}")
    @Transactional
    public void onOrderCancelled(
            OrderCancelledMessage message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag
    ) throws IOException {
        try {
            log.info("Received OrderCancelledEvent: order={}, reason={}, items={}",
                    message.orderNumber(),
                    message.reason(),
                    message.items() == null ? 0 : message.items().size());

            if (message.items() == null || message.items().isEmpty()) {
                log.info("No items in cancelled order {} — nothing to return.",
                        message.orderNumber());
                channel.basicAck(tag, false);
                return;
            }

            returnStock(message);

            channel.basicAck(tag, false);

        } catch (Exception ex) {
            log.error("Error processing OrderCancelledEvent for order {}: {}",
                    message.orderNumber(), ex.getMessage(), ex);
            // requeue = false → sends to dead-letter queue if configured
            // prevents infinite retry loop on a poison message
            channel.basicNack(tag, false, false);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void returnStock(OrderCancelledMessage message) {
        for (OrderItemInfo item : message.items()) {

            // Guard: product may have been deleted (e.g. product-deleted event
            // caused this cancellation). In that case there is no inventory
            // record to update — skip gracefully rather than throwing.
            if (!productRepository.existsById(item.productId())) {
                log.warn("Product {} no longer exists — skipping stock return for order {}",
                        item.productId(), message.orderNumber());
                continue;
            }

            try {
                inventoryService.adjustStock(
                        item.productId(),
                        AdjustStockRequest.builder()
                                .quantity(item.quantity())
                                .type(TransactionType.STOCK_IN)   // restore stockQuantity
                                .reason("Order cancelled: " + message.orderNumber())
                                .referenceId(message.orderNumber())
                                .build(),
                        null   // system action — no human performedBy
                );

                log.info("Stock returned — product: {}, qty: {}, order: {}",
                        item.productId(), item.quantity(), message.orderNumber());

            } catch (Exception ex) {
                // Log per-item failure but continue processing the rest.
                // A single item failure must not block stock return for others.
                log.error("Failed to return stock for product {} in order {}: {}",
                        item.productId(), message.orderNumber(), ex.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inbound message shapes
    //
    // These records must match exactly what order-service publishes in
    // OrderEvents.OrderCancelledEvent — field names are used by Jackson
    // for JSON deserialization.
    // ─────────────────────────────────────────────────────────────────────────

    public record OrderCancelledMessage(
            Long    orderId,
            String  orderNumber,
            Long    userId,
            String  userEmail,
            String  reason,
            List<OrderItemInfo> items,
            LocalDateTime cancelledAt
    ) {}

    public record OrderItemInfo(
            Long       productId,
            String     productSku,
            Integer    quantity,
            BigDecimal unitPrice
    ) {}

}