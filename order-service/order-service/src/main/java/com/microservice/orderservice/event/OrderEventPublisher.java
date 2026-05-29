package com.microservice.orderservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor @Slf4j
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${application.rabbitmq.order-exchange}") private String exchange;
    @Value("${application.rabbitmq.routing-keys.order-placed}")    private String orderPlacedKey;
    @Value("${application.rabbitmq.routing-keys.order-confirmed}") private String orderConfirmedKey;
    @Value("${application.rabbitmq.routing-keys.order-cancelled}") private String orderCancelledKey;
    @Value("${application.rabbitmq.routing-keys.order-completed}") private String orderCompletedKey;
    @Value("${application.rabbitmq.routing-keys.refund-requested}") private String refundRequestedKey;

    @Async("asyncTaskExecutor")
    public void publishOrderPlaced(OrderEvents.OrderPlacedEvent e) {
        send(orderPlacedKey, e);
        log.info("Published OrderPlacedEvent: {}", e.getOrderNumber());
    }

    @Async("asyncTaskExecutor")
    public void publishOrderConfirmed(OrderEvents.OrderConfirmedEvent e) {
        send(orderConfirmedKey, e);
        log.info("Published OrderConfirmedEvent: {}", e.getOrderNumber());
    }

    @Async("asyncTaskExecutor")
    public void publishOrderCancelled(OrderEvents.OrderCancelledEvent e) {
        send(orderCancelledKey, e);
        log.info("Published OrderCancelledEvent: {}", e.getOrderNumber());
    }

    @Async("asyncTaskExecutor")
    public void publishOrderCompleted(OrderEvents.OrderCompletedEvent e) {
        send(orderCompletedKey, e);
        log.info("Published OrderCompletedEvent: {}", e.getOrderNumber());
    }

    @Async("asyncTaskExecutor")
    public void publishRefundRequested(OrderEvents.RefundRequestedEvent e) {
        send(refundRequestedKey, e);
        log.info("Published RefundRequestedEvent: {}", e.getOrderNumber());
    }

    private void send(String key, Object payload) {
        try {
            rabbitTemplate.convertAndSend(exchange, key, payload);
        } catch (Exception ex) {
            log.error("Failed to publish event [{}]: {}", key, ex.getMessage());
        }
    }
}
