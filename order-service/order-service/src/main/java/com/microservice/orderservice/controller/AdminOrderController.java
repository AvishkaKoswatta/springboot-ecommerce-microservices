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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    /** List all orders — filterable by status, sortable, paginated. */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<OrderSummaryDto>>> getAllOrders(
            @RequestParam(defaultValue = "0")          int page,
            @RequestParam(defaultValue = "20")         int size,
            @RequestParam(defaultValue = "createdAt")  String sortBy,
            @RequestParam(defaultValue = "desc")       String sortDir,
            @RequestParam(required = false)            OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved",
                orderService.getAllOrders(page, size, sortBy, sortDir, status)));
    }

    /** Get full order detail (admin can see any order). */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success("Order retrieved",
                orderService.getOrderByIdAdmin(orderId)));
    }

    /**
     * Update order status — drives the fulfilment lifecycle:
     * CONFIRMED → PROCESSING → SHIPPED → DELIVERED → COMPLETED
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            HttpServletRequest httpRequest) {

        Long adminId = SecurityUtil.getUserIdFromRequest(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Order status updated",
                orderService.updateOrderStatus(orderId, request, adminId)));
    }
}
