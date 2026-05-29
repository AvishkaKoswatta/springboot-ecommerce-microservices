package com.microservice.orderservice.config;

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

    /* ── Exchanges ── */
    @Value("${application.rabbitmq.order-exchange}")   private String orderExchange;
    @Value("${application.rabbitmq.product-exchange}") private String productExchange;

    /* ── Order routing keys ── */
    @Value("${application.rabbitmq.routing-keys.order-placed}")    private String orderPlacedKey;
    @Value("${application.rabbitmq.routing-keys.order-confirmed}") private String orderConfirmedKey;
    @Value("${application.rabbitmq.routing-keys.order-cancelled}") private String orderCancelledKey;
    @Value("${application.rabbitmq.routing-keys.order-completed}") private String orderCompletedKey;
    @Value("${application.rabbitmq.routing-keys.refund-requested}") private String refundRequestedKey;

    /* ── Queue names ── */
    @Value("${application.rabbitmq.queues.order-placed}")    private String orderPlacedQ;
    @Value("${application.rabbitmq.queues.order-confirmed}") private String orderConfirmedQ;
    @Value("${application.rabbitmq.queues.order-cancelled}") private String orderCancelledQ;
    @Value("${application.rabbitmq.queues.order-completed}") private String orderCompletedQ;
    @Value("${application.rabbitmq.queues.refund-requested}") private String refundRequestedQ;
    @Value("${application.rabbitmq.queues.inventory-updated}") private String inventoryUpdatedQ;
    @Value("${application.rabbitmq.queues.low-stock-alert}")   private String lowStockAlertQ;
    @Value("${application.rabbitmq.queues.product-deleted}")   private String productDeletedQ;

    @Value("${application.rabbitmq.payment-exchange}") private String paymentExchange;
    @Value("${application.rabbitmq.routing-keys.payment-success}") private String paymentSuccessKey;
    @Value("${application.rabbitmq.routing-keys.payment-failed}")  private String paymentFailedKey;
    @Value("${application.rabbitmq.queues.payment-success}") private String paymentSuccessQ;
    @Value("${application.rabbitmq.queues.payment-failed}")  private String paymentFailedQ;


    /* ── Exchanges ── */
    @Bean public TopicExchange orderExchange()   { return ExchangeBuilder.topicExchange(orderExchange).durable(true).build(); }
    @Bean public TopicExchange productExchange() { return ExchangeBuilder.topicExchange(productExchange).durable(true).build(); }

    /* ── Queues ── */
    @Bean public Queue orderPlacedQueue()      { return QueueBuilder.durable(orderPlacedQ).build(); }
    @Bean public Queue orderConfirmedQueue()   { return QueueBuilder.durable(orderConfirmedQ).build(); }
    @Bean public Queue orderCancelledQueue()   { return QueueBuilder.durable(orderCancelledQ).build(); }
    @Bean public Queue orderCompletedQueue()   { return QueueBuilder.durable(orderCompletedQ).build(); }
    @Bean public Queue refundRequestedQueue()  { return QueueBuilder.durable(refundRequestedQ).build(); }
    @Bean public Queue inventoryUpdatedQueue() { return QueueBuilder.durable(inventoryUpdatedQ).build(); }
    @Bean public Queue lowStockAlertQueue()    { return QueueBuilder.durable(lowStockAlertQ).build(); }
    @Bean public Queue productDeletedQueue()   { return QueueBuilder.durable(productDeletedQ).build(); }

    /* ── Bindings for ORDER exchange → order queues ── */
    @Bean public Binding orderPlacedBinding()    { return BindingBuilder.bind(orderPlacedQueue()).to(orderExchange()).with(orderPlacedKey); }
    @Bean public Binding orderConfirmedBinding() { return BindingBuilder.bind(orderConfirmedQueue()).to(orderExchange()).with(orderConfirmedKey); }
    @Bean public Binding orderCancelledBinding() { return BindingBuilder.bind(orderCancelledQueue()).to(orderExchange()).with(orderCancelledKey); }
    @Bean public Binding orderCompletedBinding() { return BindingBuilder.bind(orderCompletedQueue()).to(orderExchange()).with(orderCompletedKey); }
    @Bean public Binding refundRequestedBinding() { return BindingBuilder.bind(refundRequestedQueue()).to(orderExchange()).with(refundRequestedKey); }

    /* ── Bindings for PRODUCT exchange → consumer queues in this service ── */
    @Bean public Binding inventoryUpdatedBinding() { return BindingBuilder.bind(inventoryUpdatedQueue()).to(productExchange()).with("inventory.updated"); }
    @Bean public Binding lowStockAlertBinding()    { return BindingBuilder.bind(lowStockAlertQueue()).to(productExchange()).with("inventory.low-stock"); }
    @Bean public Binding productDeletedBinding()   { return BindingBuilder.bind(productDeletedQueue()).to(productExchange()).with("product.deleted"); }

    /* ── Converter + Template ── */
    @Bean public MessageConverter jsonConverter() { return new Jackson2JsonMessageConverter(); }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(jsonConverter());
        return t;
    }

    @Bean public TopicExchange paymentExchange() {
        return ExchangeBuilder.topicExchange(paymentExchange).durable(true).build();
    }
    @Bean public Queue paymentSuccessQueue() { return QueueBuilder.durable(paymentSuccessQ).build(); }
    @Bean public Queue paymentFailedQueue()  { return QueueBuilder.durable(paymentFailedQ).build(); }

    @Bean public Binding paymentSuccessBinding() {
        return BindingBuilder.bind(paymentSuccessQueue()).to(paymentExchange()).with(paymentSuccessKey);
    }
    @Bean public Binding paymentFailedBinding() {
        return BindingBuilder.bind(paymentFailedQueue()).to(paymentExchange()).with(paymentFailedKey);
    }
}
