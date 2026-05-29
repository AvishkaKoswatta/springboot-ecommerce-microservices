package com.microservice.paymentservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${application.rabbitmq.payment-exchange}") private String exchange;
    @Value("${application.rabbitmq.routing-keys.payment-success}")  private String paymentSuccessKey;
    @Value("${application.rabbitmq.routing-keys.payment-failed}")   private String paymentFailedKey;
    @Value("${application.rabbitmq.routing-keys.refund-processed}") private String refundProcessedKey;
    @Value("${application.rabbitmq.routing-keys.refund-failed}")    private String refundFailedKey;

    @Async("asyncTaskExecutor")
    public void publishPaymentSuccess(PaymentEvents.PaymentSuccessEvent e) {
        send(paymentSuccessKey, e);
        log.info("Published PaymentSuccessEvent: order={}", e.getOrderNumber());
    }

    @Async("asyncTaskExecutor")
    public void publishPaymentFailed(PaymentEvents.PaymentFailedEvent e) {
        send(paymentFailedKey, e);
        log.warn("Published PaymentFailedEvent: order={}, reason={}", e.getOrderNumber(), e.getFailureReason());
    }

    @Async("asyncTaskExecutor")
    public void publishRefundProcessed(PaymentEvents.RefundProcessedEvent e) {
        send(refundProcessedKey, e);
        log.info("Published RefundProcessedEvent: order={}, amount={}", e.getOrderNumber(), e.getRefundAmount());
    }

    @Async("asyncTaskExecutor")
    public void publishRefundFailed(PaymentEvents.RefundFailedEvent e) {
        send(refundFailedKey, e);
        log.warn("Published RefundFailedEvent: order={}", e.getOrderNumber());
    }

    private void send(String routingKey, Object payload) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, payload);
        } catch (Exception ex) {
            log.error("Failed to publish event [{}]: {}", routingKey, ex.getMessage());
        }
    }
}
