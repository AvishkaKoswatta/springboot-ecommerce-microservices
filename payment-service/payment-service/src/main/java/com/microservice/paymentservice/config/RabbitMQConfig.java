package com.microservice.paymentservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${application.rabbitmq.payment-exchange}") private String paymentExchange;
    @Value("${application.rabbitmq.order-exchange}")   private String orderExchange;

    @Value("${application.rabbitmq.routing-keys.payment-success}")  private String paymentSuccessKey;
    @Value("${application.rabbitmq.routing-keys.payment-failed}")   private String paymentFailedKey;
    @Value("${application.rabbitmq.routing-keys.refund-processed}") private String refundProcessedKey;
    @Value("${application.rabbitmq.routing-keys.refund-failed}")    private String refundFailedKey;

    @Value("${application.rabbitmq.queues.payment-success}")   private String paymentSuccessQ;
    @Value("${application.rabbitmq.queues.payment-failed}")    private String paymentFailedQ;
    @Value("${application.rabbitmq.queues.refund-processed}")  private String refundProcessedQ;
    @Value("${application.rabbitmq.queues.refund-failed}")     private String refundFailedQ;
    @Value("${application.rabbitmq.queues.payment-initiated}") private String paymentInitiatedQ;

    // ── Exchanges ─────────────────────────────────────────────────────────────
    @Bean public TopicExchange paymentExchange() {
        return ExchangeBuilder.topicExchange(paymentExchange).durable(true).build();
    }
    // Order exchange declared here so payment-service can bind its consumer queue
    @Bean public TopicExchange orderExchangeInPayment() {
        return ExchangeBuilder.topicExchange(orderExchange).durable(true).build();
    }

    // ── Queues ────────────────────────────────────────────────────────────────
    @Bean public Queue paymentSuccessQueue()   { return QueueBuilder.durable(paymentSuccessQ).build(); }
    @Bean public Queue paymentFailedQueue()    { return QueueBuilder.durable(paymentFailedQ).build(); }
    @Bean public Queue refundProcessedQueue()  { return QueueBuilder.durable(refundProcessedQ).build(); }
    @Bean public Queue refundFailedQueue()     { return QueueBuilder.durable(refundFailedQ).build(); }
    @Bean public Queue paymentInitiatedQueue() { return QueueBuilder.durable(paymentInitiatedQ).build(); }

    // ── Bindings: payment exchange → payment queues ───────────────────────────
    @Bean public Binding paymentSuccessBinding() {
        return BindingBuilder.bind(paymentSuccessQueue()).to(paymentExchange()).with(paymentSuccessKey);
    }
    @Bean public Binding paymentFailedBinding() {
        return BindingBuilder.bind(paymentFailedQueue()).to(paymentExchange()).with(paymentFailedKey);
    }
    @Bean public Binding refundProcessedBinding() {
        return BindingBuilder.bind(refundProcessedQueue()).to(paymentExchange()).with(refundProcessedKey);
    }
    @Bean public Binding refundFailedBinding() {
        return BindingBuilder.bind(refundFailedQueue()).to(paymentExchange()).with(refundFailedKey);
    }

    // ── Binding: order exchange → payment-initiated queue ─────────────────────
    @Bean public Binding paymentInitiatedBinding() {
        return BindingBuilder.bind(paymentInitiatedQueue())
                .to(orderExchangeInPayment())
                .with("payment.initiated");
    }

    // ── Converter + Template ──────────────────────────────────────────────────
    @Bean public MessageConverter jsonConverter() { return new Jackson2JsonMessageConverter(); }

    @Bean public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(jsonConverter());
        return t;
    }
}
