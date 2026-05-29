package com.microservice.paymentservice.client;

import com.microservice.paymentservice.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderServiceClientFallback implements OrderServiceClient {

    @Override
    public ApiResponse<Void> confirmPayment(Long orderId,
                                             ConfirmPaymentRequest request,
                                             String token) {
        log.warn("order-service unavailable — confirmPayment fallback for order {}", orderId);
        // Payment succeeded — even if Feign fails, RabbitMQ event will notify order-service
        return ApiResponse.<Void>builder().success(false)
                .message("order-service unavailable — notified via RabbitMQ").build();
    }
}
