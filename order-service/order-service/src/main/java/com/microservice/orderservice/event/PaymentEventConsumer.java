package com.microservice.orderservice.event;

import com.microservice.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = "${application.rabbitmq.queues.payment-success}")
    public void consume(PaymentSuccessEvent event) {

        log.info("Payment event received for orderId={}", event.getOrderId());

        orderService.confirmPayment(
                event.getOrderId(),
                event.getPaymentReference(),
                event.getStatus()
        );
    }

//    @RabbitListener(queues = "${application.rabbitmq.queues.payment-failed}")
//    public void consumePaymentFailed(PaymentFailedEvent event) {
//        log.warn("Payment failed for orderId={}, reason={}", event.getOrderId(), event.getFailureReason());
//        orderService.confirmPayment(
//                event.getOrderId(),
//                event.getPaymentReference(),
//                "FAILED"
//        );
//    }
}