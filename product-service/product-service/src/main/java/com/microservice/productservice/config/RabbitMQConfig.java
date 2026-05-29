package com.microservice.productservice.config;

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

    @Value("${application.rabbitmq.exchange}")
    private String exchange;

    @Value("${application.rabbitmq.queues.product-created}")
    private String productCreatedQueue;

    @Value("${application.rabbitmq.queues.product-updated}")
    private String productUpdatedQueue;

    @Value("${application.rabbitmq.queues.product-deleted}")
    private String productDeletedQueue;

    @Value("${application.rabbitmq.queues.inventory-updated}")
    private String inventoryUpdatedQueue;

    @Value("${application.rabbitmq.queues.low-stock-alert}")
    private String lowStockAlertQueue;

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

    // Add these two fields
    @Value("${application.rabbitmq.order-exchange}")
    private String orderExchange;

    @Value("${application.rabbitmq.queues.order-placed}")
    private String orderPlacedQ;

    @Value("${application.rabbitmq.queues.order-cancelled}")
    private String orderCancelledQ;

    @Value("${application.rabbitmq.queues.order-confirmed}")
    private String orderConfirmedQ;

    // Exchange
    @Bean
    public TopicExchange productExchange() {
        return ExchangeBuilder.topicExchange(exchange).durable(true).build();
    }

    // Queues
    @Bean public Queue productCreatedQueue()   { return QueueBuilder.durable(productCreatedQueue).build(); }
    @Bean public Queue productUpdatedQueue()   { return QueueBuilder.durable(productUpdatedQueue).build(); }
    @Bean public Queue productDeletedQueue()   { return QueueBuilder.durable(productDeletedQueue).build(); }
    @Bean public Queue inventoryUpdatedQueue() { return QueueBuilder.durable(inventoryUpdatedQueue).build(); }
    @Bean public Queue lowStockAlertQueue()    { return QueueBuilder.durable(lowStockAlertQueue).build(); }

    // Bindings
    @Bean
    public Binding productCreatedBinding() {
        return BindingBuilder.bind(productCreatedQueue()).to(productExchange()).with(productCreatedKey);
    }
    @Bean
    public Binding productUpdatedBinding() {
        return BindingBuilder.bind(productUpdatedQueue()).to(productExchange()).with(productUpdatedKey);
    }
    @Bean
    public Binding productDeletedBinding() {
        return BindingBuilder.bind(productDeletedQueue()).to(productExchange()).with(productDeletedKey);
    }
    @Bean
    public Binding inventoryUpdatedBinding() {
        return BindingBuilder.bind(inventoryUpdatedQueue()).to(productExchange()).with(inventoryUpdatedKey);
    }
    @Bean
    public Binding lowStockAlertBinding() {
        return BindingBuilder.bind(lowStockAlertQueue()).to(productExchange()).with(lowStockAlertKey);
    }

    // JSON message converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // ── New beans ──────────────────────────────────────────────

    // Order exchange (declared by order-service, but product-service
// must also declare it to create bindings)
    @Bean
    public TopicExchange orderExchange() {
        return ExchangeBuilder.topicExchange(orderExchange).durable(true).build();
    }

    // Queue: order.placed.queue
    @Bean
    public Queue orderPlacedQueue() {
        return QueueBuilder.durable(orderPlacedQ).build();
    }

    // Queue: order.cancelled.queue  (already added earlier)
    @Bean
    public Queue orderCancelledQueueInProduct() {
        return QueueBuilder.durable(orderCancelledQ).build();
    }

    // Binding: order.placed.queue ← order.exchange / order.placed
    @Bean
    public Binding orderPlacedBinding() {
        return BindingBuilder
                .bind(orderPlacedQueue())
                .to(orderExchange())
                .with("order.placed");
    }

    // Binding: order.cancelled.queue ← order.exchange / order.cancelled
    @Bean
    public Binding orderCancelledBindingInProduct() {
        return BindingBuilder
                .bind(orderCancelledQueueInProduct())
                .to(orderExchange())
                .with("order.cancelled");
    }

    @Bean
    public Queue orderConfirmedQueue() {
        return QueueBuilder.durable(orderConfirmedQ).build();
    }

    @Bean
    public Binding orderConfirmedBinding() {
        return BindingBuilder
                .bind(orderConfirmedQueue())
                .to(orderExchange())
                .with("order.confirmed");
    }
}
