package com.microservice.orderservice.controller;

import com.microservice.orderservice.dto.OrderDtos.*;
import com.microservice.orderservice.entity.OrderStatus;
import com.microservice.orderservice.response.ApiResponse;
import com.microservice.orderservice.response.PagedResponse;
import com.microservice.orderservice.service.OrderService;
import com.microservice.orderservice.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Place a new order — full cart checkout.
     * JWT must contain userId and userEmail claims.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request,
            HttpServletRequest httpRequest,
            @AuthenticationPrincipal String username) {

        Long userId = SecurityUtil.getUserIdFromRequest(httpRequest);
        String userEmail = username;
        String token = extractToken(httpRequest);

        OrderDto order = orderService.placeOrder(request, userId, userEmail, token);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Order placed successfully", order));
    }

    /** Get a single order (owner only). */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(
            @PathVariable Long orderId,
            HttpServletRequest httpRequest) {

        Long userId = SecurityUtil.getUserIdFromRequest(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved",
                orderService.getOrderById(orderId, userId)));
    }

    /** Get by order number (owner only). */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderByNumber(
            @PathVariable String orderNumber,
            HttpServletRequest httpRequest) {

        Long userId = SecurityUtil.getUserIdFromRequest(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved",
                orderService.getOrderByNumber(orderNumber, userId)));
    }

    /** Full order history for the authenticated user. */
    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<PagedResponse<OrderSummaryDto>>> getMyOrders(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {

        Long userId = SecurityUtil.getUserIdFromRequest(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Order history retrieved",
                orderService.getOrderHistory(userId, page, size)));
    }

    /** Cancel an order (within the cancellation window). */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody CancelOrderRequest request,
            HttpServletRequest httpRequest) {

        Long userId = SecurityUtil.getUserIdFromRequest(httpRequest);
        orderService.cancelOrder(orderId, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully"));
    }

    @PostMapping("/{orderId}/confirm-payment")
    public ResponseEntity<ApiResponse<Void>> confirmPayment(
            @PathVariable Long orderId,
            @RequestBody ConfirmPaymentRequest request,
            HttpServletRequest httpRequest) {

        String token = extractToken(httpRequest);

        orderService.confirmPayment(
                orderId,
                request.paymentReference(),
                request.paymentStatus()
        );

        return ResponseEntity.ok(ApiResponse.success("Payment confirmed"));
    }

    public record ConfirmPaymentRequest(
            String paymentReference,
            String paymentStatus
    ) {}

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer "))
                ? header.substring(7) : "";
    }
}
