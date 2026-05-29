package com.microservice.paymentservice.client;

import com.microservice.paymentservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "order-service",
        //url = "${services.order-service-url:}",
        fallback = OrderServiceClientFallback.class
)
public interface OrderServiceClient {

    /**
     * Called after payment succeeds — confirms the order in order-service.
     */
    @PostMapping("/orders/{orderId}/confirm-payment")
    ApiResponse<Void> confirmPayment(
            @PathVariable Long orderId,
            @RequestBody ConfirmPaymentRequest request,
            @RequestHeader("Authorization") String token);

    record ConfirmPaymentRequest(
            String paymentReference,
            String paymentMethod
    ) {}
}
