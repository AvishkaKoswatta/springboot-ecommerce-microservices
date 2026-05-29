package com.microservice.orderservice.service;

import com.microservice.orderservice.dto.OrderDtos.*;
import com.microservice.orderservice.entity.OrderStatus;
import com.microservice.orderservice.response.PagedResponse;

public interface OrderService {

    // Customer operations
    OrderDto placeOrder(PlaceOrderRequest request, Long userId, String userEmail, String token);
    OrderDto getOrderById(Long orderId, Long userId);
    OrderDto getOrderByNumber(String orderNumber, Long userId);
    PagedResponse<OrderSummaryDto> getOrderHistory(Long userId, int page, int size);
    void cancelOrder(Long orderId, Long userId, CancelOrderRequest request);


    void confirmPayment(Long orderId, String paymentReference, String paymentStatus);

    // Admin operations
    PagedResponse<OrderSummaryDto> getAllOrders(int page, int size, String sortBy, String sortDir, OrderStatus status);
    OrderDto updateOrderStatus(Long orderId, UpdateOrderStatusRequest request, Long adminId);
    OrderDto getOrderByIdAdmin(Long orderId);
}
