package com.microservice.orderservice.controller;

import com.microservice.orderservice.dto.RefundDtos.*;
import com.microservice.orderservice.response.ApiResponse;
import com.microservice.orderservice.response.PagedResponse;
import com.microservice.orderservice.service.RefundService;
import com.microservice.orderservice.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    // ── Customer ─────────────────────────────────────────────────────────────

    /** Submit a refund/return request for a delivered order. */
    @PostMapping("/orders/{orderId}/refund")
    public ResponseEntity<ApiResponse<RefundDto>> requestRefund(
            @PathVariable Long orderId,
            @Valid @RequestBody RefundRequestDto request,
            HttpServletRequest httpRequest) {

        Long userId = SecurityUtil.getUserIdFromRequest(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Refund request submitted",
                        refundService.requestRefund(orderId, request, userId)));
    }

    /** Get refund status for an order. */
    @GetMapping("/orders/{orderId}/refund")
    public ResponseEntity<ApiResponse<RefundDto>> getRefund(
            @PathVariable Long orderId,
            HttpServletRequest httpRequest) {

        Long userId = SecurityUtil.getUserIdFromRequest(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Refund retrieved",
                refundService.getRefundByOrderId(orderId, userId)));
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    /** List all pending refund requests. */
    @GetMapping("/admin/refunds/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<RefundDto>>> getPendingRefunds(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Pending refunds retrieved",
                refundService.getPendingRefunds(page, size)));
    }

    /** Approve or reject a refund. */
    @PatchMapping("/admin/refunds/{refundId}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RefundDto>> resolveRefund(
            @PathVariable Long refundId,
            @Valid @RequestBody ResolveRefundRequest request,
            HttpServletRequest httpRequest) {

        Long adminId = SecurityUtil.getUserIdFromRequest(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Refund resolved",
                refundService.resolveRefund(refundId, request, adminId)));
    }
}
