package com.microservice.paymentservice.service;

import com.microservice.paymentservice.dto.PaymentDtos.*;
import com.microservice.paymentservice.response.PagedResponse;

public interface PaymentService {
    PaymentDto initiatePayment(InitiatePaymentRequest request, Long userId);
    PaymentDto processPayment(ProcessPaymentRequest request, String token);
    PaymentDto cancelPayment(CancelPaymentRequest request, Long userId);
    PaymentDto getPaymentByReference(String paymentReference, Long userId);
    PaymentDto getPaymentByOrderId(Long orderId, Long userId);
    PagedResponse<PaymentSummaryDto> getPaymentHistory(Long userId, int page, int size);

    // Refund
    RefundDto initiateRefund(RefundRequest request, Long requestedBy);

    RefundDto processRefund(String refundReference, Long requestedBy);
    RefundDto getRefundByReference(String refundReference);

    // Admin
    PagedResponse<PaymentSummaryDto> getAllPayments(int page, int size, String status);
    PagedResponse<RefundDto> getAllRefunds(int page, int size, String status);
}
