package com.microservice.orderservice.service;

import com.microservice.orderservice.dto.RefundDtos.*;
import com.microservice.orderservice.entity.*;
import com.microservice.orderservice.event.OrderEventPublisher;
import com.microservice.orderservice.event.OrderEvents.*;
import com.microservice.orderservice.exception.*;
import com.microservice.orderservice.mapper.RefundMapper;
import com.microservice.orderservice.repository.OrderRepository;
import com.microservice.orderservice.repository.RefundRequestRepository;
import com.microservice.orderservice.response.PagedResponse;
import com.microservice.orderservice.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundServiceImpl implements RefundService {

    private final RefundRequestRepository refundRepository;
    private final OrderRepository         orderRepository;
    private final RefundMapper            refundMapper;
    private final OrderEventPublisher     eventPublisher;
    private final OrderEmailService emailService;

    @Override
    @Transactional
    public RefundDto requestRefund(Long orderId, RefundRequestDto req, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Ownership check
        if (!order.getUserId().equals(userId) && !SecurityUtil.isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have access to this order");
        }

        // Eligible statuses
        if (order.getStatus() != OrderStatus.DELIVERED && order.getStatus() != OrderStatus.COMPLETED) {
            throw new InvalidOrderStateException(
                    "Refunds can only be requested for DELIVERED or COMPLETED orders. " +
                    "Current status: " + order.getStatus());
        }

        if (refundRepository.existsByOrderId(orderId)) {
            throw new RefundAlreadyExistsException(
                    "A refund request already exists for order: " + order.getOrderNumber());
        }

        // Requested amount must not exceed order total
        if (req.getRequestedAmount().compareTo(order.getTotalAmount()) > 0) {
            throw new IllegalArgumentException(
                    "Requested refund amount (" + req.getRequestedAmount() +
                    ") cannot exceed order total (" + order.getTotalAmount() + ")");
        }

        RefundRequest refund = RefundRequest.builder()
                .order(order)
                .status(RefundStatus.PENDING)
                .reasonType(req.getReasonType())
                .reasonDetail(req.getReasonDetail())
                .requestedAmount(req.getRequestedAmount())
                .build();

        RefundRequest saved = refundRepository.save(refund);

        // Transition order status
        order.setStatus(OrderStatus.REFUND_REQUESTED);
        orderRepository.save(order);

        log.info("Refund requested for order {} by user {}", order.getOrderNumber(), userId);

        eventPublisher.publishRefundRequested(RefundRequestedEvent.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(userId)
                .userEmail(order.getUserEmail())
                .requestedAmount(req.getRequestedAmount())
                .reasonType(req.getReasonType().name())
                .reasonDetail(req.getReasonDetail())
                .requestedAt(LocalDateTime.now())
                .build());

        return refundMapper.toDto(saved);
    }

    @Override
    @Transactional
    public RefundDto resolveRefund(Long refundId, ResolveRefundRequest req, Long adminId) {
        RefundRequest refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new OrderNotFoundException("Refund not found with id: " + refundId));

        if (refund.getStatus() != RefundStatus.PENDING) {
            throw new InvalidOrderStateException(
                    "Refund has already been resolved with status: " + refund.getStatus());
        }

        refund.setStatus(req.getStatus());
        refund.setApprovedAmount(req.getApprovedAmount());
        refund.setAdminNotes(req.getAdminNotes());
        refund.setResolvedAt(LocalDateTime.now());
        refund.setResolvedBy(adminId);

        // Update order status based on resolution
        Order order = refund.getOrder();
        if (req.getStatus() == RefundStatus.APPROVED) {
            order.setStatus(OrderStatus.REFUNDED);
        }
        if (req.getStatus() == RefundStatus.APPROVED) {
            emailService.sendRefundProcessed(
                    refund.getOrder().getUserEmail(),
                    refund.getOrder().getOrderNumber(),
                    req.getApprovedAmount());
        }
        else if (req.getStatus() == RefundStatus.REJECTED) {
            // Revert order back to COMPLETED if refund is rejected
            order.setStatus(OrderStatus.COMPLETED);
        }
        orderRepository.save(order);

        RefundRequest saved = refundRepository.save(refund);
        log.info("Refund {} resolved as {} by admin {}", refundId, req.getStatus(), adminId);
        return refundMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RefundDto getRefundByOrderId(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.getUserId().equals(userId) && !SecurityUtil.isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have access to this order");
        }

        RefundRequest refund = refundRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "No refund request found for order: " + orderId));
        return refundMapper.toDto(refund);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<RefundDto> getPendingRefunds(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<RefundDto> result = refundRepository
                .findAllByStatus(RefundStatus.PENDING, pageable)
                .map(refundMapper::toDto);
        return PagedResponse.from(result);
    }
}
