package com.microservice.paymentservice.controller;

import com.microservice.paymentservice.dto.PaymentDtos.*;
import com.microservice.paymentservice.response.ApiResponse;
import com.microservice.paymentservice.response.PagedResponse;
import com.microservice.paymentservice.service.PaymentService;
import com.microservice.paymentservice.util.SecurityUtil;
import com.microservice.paymentservice.webhook.WebhookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/payments")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService  paymentService;
    private final WebhookService  webhookService;

    // ─── List all payments ───────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PaymentSummaryDto>>> getAllPayments(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String status) {

        return ResponseEntity.ok(ApiResponse.success("Payments retrieved",
                paymentService.getAllPayments(page, size, status)));
    }

    // ─── List all refunds ────────────────────────────────────────────────────

    @GetMapping("/refunds")
    public ResponseEntity<ApiResponse<PagedResponse<RefundDto>>> getAllRefunds(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String status) {

        return ResponseEntity.ok(ApiResponse.success("Refunds retrieved",
                paymentService.getAllRefunds(page, size, status)));
    }

    // ─── Get payment by reference (admin can see any) ────────────────────────

    @GetMapping("/reference/{ref}")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentByRef(
            @PathVariable String ref,
            HttpServletRequest httpRequest) {

        // Admin bypasses ownership check — pass admin's userId
        Long adminId = SecurityUtil.getUserIdFromRequest(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved",
                paymentService.getPaymentByReference(ref, adminId)));
    }

    // ─── Process refund on behalf of customer ────────────────────────────────

    @PostMapping("/refund/{refundReference}/process")
    public ResponseEntity<ApiResponse<RefundDto>> processRefund(
            @PathVariable String refundReference,
            HttpServletRequest httpRequest) {

        Long userId = SecurityUtil.getUserIdFromRequest(httpRequest);

        return ResponseEntity.ok(
                ApiResponse.success("Refund processed",
                        paymentService.processRefund(refundReference, userId))
        );
    }

    // ─── Webhook events ──────────────────────────────────────────────────────

    @GetMapping("/webhooks")
    public ResponseEntity<ApiResponse<PagedResponse<WebhookEventDto>>> getAllWebhooks(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String status) {

        return ResponseEntity.ok(ApiResponse.success("Webhook events retrieved",
                webhookService.getAllWebhooks(page, size, status)));
    }

    @GetMapping("/webhooks/payment/{paymentId}")
    public ResponseEntity<ApiResponse<PagedResponse<WebhookEventDto>>> getWebhooksByPayment(
            @PathVariable Long paymentId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success("Webhook events retrieved",
                webhookService.getWebhooksByPayment(paymentId, page, size)));
    }

    @GetMapping("/webhooks/order/{orderId}")
    public ResponseEntity<ApiResponse<PagedResponse<WebhookEventDto>>> getWebhooksByOrder(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success("Webhook events retrieved",
                webhookService.getWebhooksByOrder(orderId, page, size)));
    }
}
