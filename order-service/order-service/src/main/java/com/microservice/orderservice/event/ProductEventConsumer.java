package com.microservice.orderservice.event;

import com.microservice.orderservice.entity.*;
import com.microservice.orderservice.event.OrderEvents.*;
import com.microservice.orderservice.repository.OrderRepository;
import com.microservice.orderservice.service.OrderEmailService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventConsumer {

    private final OrderRepository     orderRepository;
    private final OrderEventPublisher eventPublisher;
    private final OrderEmailService   emailService;

    @RabbitListener(queues = "${application.rabbitmq.queues.product-deleted}")
    @Transactional
    public void onProductDeleted(
            ProductDeletedMessage message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag
    ) throws IOException {
        try {
            log.info("Received ProductDeletedEvent: productId={}, sku={}",
                    message.productId(), message.sku());

            List<Order> affectedOrders = orderRepository
                    .findActiveOrdersContainingProduct(message.productId());

            if (affectedOrders.isEmpty()) {
                log.info("No active orders contain deleted product {}. Nothing to do.",
                        message.productId());
                channel.basicAck(tag, false);
                return;
            }

            log.warn("Found {} active order(s) containing deleted product {}. Cancelling.",
                    affectedOrders.size(), message.productId());

            for (Order order : affectedOrders) {
                cancelOrderDueToDeletedProduct(order, message);
            }

            channel.basicAck(tag, false);

        } catch (Exception ex) {
            log.error("Error processing ProductDeletedEvent for product {}: {}",
                    message.productId(), ex.getMessage(), ex);
            channel.basicNack(tag, false, false);
        }
    }

    private void cancelOrderDueToDeletedProduct(Order order,
                                                ProductDeletedMessage message) {
        String reason = String.format(
                "Automatically cancelled: product '%s' is no longer available.",
                message.sku());

        // Record status history
        OrderStatusHistory history = OrderStatusHistory.builder()
                .fromStatus(order.getStatus())
                .toStatus(OrderStatus.CANCELLED)
                .note(reason)
                .changedByRole("SYSTEM")
                .build();
        order.addStatusHistory(history);

        // Update order state
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(reason);

        orderRepository.save(order);

        // Email the customer
        emailService.sendOrderCancelled(
                order.getUserEmail(),
                order.getOrderNumber(),
                reason,
                false   // payment handling delegated to payment-service
        );

        // Publish order.cancelled so other services (product-service) release stock
        List<OrderItemInfo> itemInfos = order.getItems().stream()
                .map(i -> new OrderItemInfo(
                        i.getProductId(), i.getProductSku(),
                        i.getQuantity(), i.getUnitPrice()))
                .toList();

        eventPublisher.publishOrderCancelled(OrderCancelledEvent.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .userEmail(order.getUserEmail())
                .reason(reason)
                .items(itemInfos)
                .cancelledAt(order.getCancelledAt())
                .build());

        log.info("Order {} cancelled and customer notified (product deleted: {})",
                order.getOrderNumber(), message.productId());
    }

    public record ProductDeletedMessage(
            Long productId,
            String sku,
            Long deletedBy,
            String deletedAt
    ) {}
}
