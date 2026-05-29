package com.microservice.paymentservice.event;

import com.microservice.paymentservice.entity.PaymentMethod;
import com.microservice.paymentservice.entity.PaymentStatus;
import com.microservice.paymentservice.repository.PaymentRepository;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Consumes PaymentInitiatedEvent from order-service.
 *
 * When order-service publishes payment.initiated (for online payment orders),
 * payment-service receives it and creates a payment record in INITIATED state.
 * The customer then calls POST /payments/process to submit card details
 * and trigger the mock gateway.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final PaymentRepository paymentRepository;

    @RabbitListener(queues = "${application.rabbitmq.queues.payment-initiated}")
    public void onPaymentInitiated(
            PaymentInitiatedMessage message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag
    ) throws IOException {
        try {
            log.info("Received PaymentInitiatedEvent: order={}, amount={}",
                    message.orderNumber(), message.amount());

            // Idempotency check — don't create duplicate payment records
            if (paymentRepository.existsByOrderId(message.orderId())) {
                log.warn("Payment record already exists for order {} — skipping duplicate event",
                        message.orderNumber());
                channel.basicAck(tag, false);
                return;
            }

            // Create the payment record in INITIATED state.
            // The actual processing happens when the customer submits card details.
            var payment = com.microservice.paymentservice.entity.Payment.builder()
                    .orderId(message.orderId())
                    .orderNumber(message.orderNumber())
                    .userId(message.userId())
                    .userEmail(message.userEmail())
                    .paymentReference(generatePaymentReference(message.orderNumber()))
                    .amount(message.amount())
                    .currency("USD")
                    .paymentMethod(parseMethod(message.paymentMethod()))
                    .status(PaymentStatus.INITIATED)
                    .initiatedAt(java.time.LocalDateTime.now())
                    .expiresAt(java.time.LocalDateTime.now().plusMinutes(15))
                    .build();

            paymentRepository.save(payment);

            log.info("Payment record created for order {}: ref={}",
                    message.orderNumber(), payment.getPaymentReference());

            channel.basicAck(tag, false);

        } catch (Exception ex) {
            log.error("Error processing PaymentInitiatedEvent for order {}: {}",
                    message.orderNumber(), ex.getMessage(), ex);
            channel.basicNack(tag, false, false);
        }
    }

    private String generatePaymentReference(String orderNumber) {
        return "PAY-" + orderNumber + "-" +
                java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private PaymentMethod parseMethod(String method) {
        try {
            // order-service uses CREDIT_CARD, DEBIT_CARD etc.
            return PaymentMethod.valueOf(method);
        } catch (Exception e) {
            return PaymentMethod.CREDIT_CARD;
        }
    }

    public record PaymentInitiatedMessage(
            Long       orderId,
            String     orderNumber,
            Long       userId,
            String     userEmail,
            BigDecimal amount,
            String     paymentMethod,
            String     initiatedAt
    ) {}
}
