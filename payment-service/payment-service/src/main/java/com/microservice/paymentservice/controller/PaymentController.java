package com.microservice.paymentservice.controller;

import com.microservice.paymentservice.dto.PaymentDtos.*;
import com.microservice.paymentservice.response.ApiResponse;
import com.microservice.paymentservice.response.PagedResponse;
import com.microservice.paymentservice.service.PaymentService;
import com.microservice.paymentservice.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ─────────────────────────────────────────────────────────────────────────
    // Initiate a payment session
    // Called directly when the payment.initiated RabbitMQ event is not used
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<PaymentDto>> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request,
            HttpServletRequest httpRequest) {

        Long userId = SecurityUtil.getUserIdFromRequest(httpRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "Payment session initiated",
                        paymentService.initiatePayment(request, userId)
                ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Process payment — submit card details to mock gateway
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<PaymentDto>> processPayment(
            @Valid @RequestBody ProcessPaymentRequest request,
            HttpServletRequest httpRequest) {

        String token = SecurityUtil.extractToken(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Payment processed",
                paymentService.processPayment(request, token)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cancel an INITIATED payment session
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<PaymentDto>> cancelPayment(
            @Valid @RequestBody CancelPaymentRequest request,
            HttpServletRequest httpRequest) {

        Long userId = SecurityUtil.getUserIdFromRequest(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Payment cancelled",
                paymentService.cancelPayment(request, userId)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Get payment by reference
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/reference/{ref}")
    public ResponseEntity<ApiResponse<PaymentDto>> getByReference(
            @PathVariable String ref,
            HttpServletRequest httpRequest) {

        Long userId = SecurityUtil.getUserIdFromRequest(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved",
                paymentService.getPaymentByReference(ref, userId)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Get payment by order ID
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentDto>> getByOrderId(
            @PathVariable Long orderId,
            HttpServletRequest httpRequest) {

        Long userId = SecurityUtil.getUserIdFromRequest(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved",
                paymentService.getPaymentByOrderId(orderId, userId)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Payment history for authenticated user
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/my-payments")
    public ResponseEntity<ApiResponse<PagedResponse<PaymentSummaryDto>>> getMyPayments(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {

        Long userId = SecurityUtil.getUserIdFromRequest(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Payment history retrieved",
                paymentService.getPaymentHistory(userId, page, size)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Request a refund
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<RefundDto>> initiateRefund(
            @Valid @RequestBody RefundRequest request,
            HttpServletRequest httpRequest) {

        Long userId = SecurityUtil.getUserIdFromRequest(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Refund initiated",
                        paymentService.initiateRefund(request, userId)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Get refund by reference
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/refund/{refundRef}")
    public ResponseEntity<ApiResponse<RefundDto>> getRefund(
            @PathVariable String refundRef) {

        return ResponseEntity.ok(ApiResponse.success("Refund retrieved",
                paymentService.getRefundByReference(refundRef)));
    }
}
