package com.microservice.orderservice.service;

import com.microservice.orderservice.dto.RefundDtos.*;
import com.microservice.orderservice.response.PagedResponse;

public interface RefundService {
    RefundDto requestRefund(Long orderId, RefundRequestDto request, Long userId);
    RefundDto resolveRefund(Long refundId, ResolveRefundRequest request, Long adminId);
    RefundDto getRefundByOrderId(Long orderId, Long userId);
    PagedResponse<RefundDto> getPendingRefunds(int page, int size);
}
