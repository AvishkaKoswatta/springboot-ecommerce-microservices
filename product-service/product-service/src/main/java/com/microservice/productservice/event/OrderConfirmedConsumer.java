package com.microservice.productservice.event;

import com.microservice.productservice.dto.InventoryDtos;
import com.microservice.productservice.entity.TransactionType;
import com.microservice.productservice.repository.ProductRepository;
import com.microservice.productservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderConfirmedConsumer {
    private final InventoryService inventoryService;
    private final ProductRepository productRepository;

    @RabbitListener(queues = "${application.rabbitmq.queues.order-confirmed}")
    @Transactional
    public void onOrderConfirmed(OrderConfirmedMessage message) {

        log.info("Order confirmed received: {}", message.orderNumber());

        List<OrderItemInfo> items =
                message.items() == null ? List.of() : message.items();

        for (OrderItemInfo item : items) {

            // 1️⃣  Release the reservation hold
            inventoryService.adjustStock(
                    item.productId(),
                    InventoryDtos.AdjustStockRequest.builder()
                            .quantity(item.quantity())
                            .type(TransactionType.RESERVATION_CANCEL)
                            .reason("Order confirmed: " + message.orderNumber())
                            .referenceId(message.orderNumber())
                            .build(),
                    null
            );

            // 2️⃣  Deduct actual stock
            inventoryService.adjustStock(
                    item.productId(),
                    InventoryDtos.AdjustStockRequest.builder()
                            .quantity(item.quantity())
                            .type(TransactionType.STOCK_OUT)
                            .reason("Order confirmed — stock deducted: " + message.orderNumber())
                            .referenceId(message.orderNumber())
                            .build(),
                    null
            );
        }
    }

        public record OrderConfirmedMessage(
                Long orderId,
                String orderNumber,
                Long userId,
                String userEmail,
                String userName,
                java.math.BigDecimal totalAmount,
                List<OrderItemInfo> items,
                java.time.LocalDateTime confirmedAt
        ) {}

        public record OrderItemInfo(
                Long productId,
                String productSku,
                Integer quantity,
                java.math.BigDecimal unitPrice
        ) {}
    }

