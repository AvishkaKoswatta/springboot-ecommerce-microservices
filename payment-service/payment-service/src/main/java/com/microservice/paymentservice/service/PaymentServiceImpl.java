package com.microservice.paymentservice.service;

import com.microservice.paymentservice.client.OrderServiceClient;
import com.microservice.paymentservice.config.PaymentProperties;
import com.microservice.paymentservice.dto.PaymentDtos.*;
import com.microservice.paymentservice.entity.*;
import com.microservice.paymentservice.event.PaymentEventPublisher;
import com.microservice.paymentservice.event.PaymentEvents.*;
import com.microservice.paymentservice.exception.*;
import com.microservice.paymentservice.mapper.PaymentMapper;
import com.microservice.paymentservice.repository.PaymentRepository;
import com.microservice.paymentservice.repository.RefundRepository;
import com.microservice.paymentservice.repository.WebhookEventRepository;
import com.microservice.paymentservice.util.MockGatewayService;
import com.microservice.paymentservice.util.MockGatewayService.GatewayResponse;
import com.microservice.paymentservice.webhook.WebhookDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.microservice.paymentservice.response.PagedResponse;
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository     paymentRepository;
    private final RefundRepository      refundRepository;
    private final WebhookEventRepository webhookRepository;
    private final PaymentMapper         mapper;
    private final MockGatewayService    gateway;
    private final PaymentEventPublisher eventPublisher;
    private final OrderServiceClient    orderClient;
    private final WebhookDispatchService webhookDispatcher;
    private final PaymentProperties     props;

    // ─────────────────────────────────────────────────────────────────────────
    // Initiate Payment
    // Called directly by customer OR auto-created via RabbitMQ consumer
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PaymentDto initiatePayment(InitiatePaymentRequest req, Long userId) {
        if (paymentRepository.existsByOrderId(req.getOrderId())) {
            throw new PaymentAlreadyExistsException(
                    "A payment already exists for order: " + req.getOrderNumber());
        }

        Payment payment = Payment.builder()
                .orderId(req.getOrderId())
                .orderNumber(req.getOrderNumber())
                .userId(userId)
                .userEmail(req.getUserEmail())
                .paymentReference(generatePaymentRef(req.getOrderNumber()))
                .amount(req.getAmount())
                .currency(req.getCurrency() != null ? req.getCurrency() : "USD")
                .paymentMethod(req.getPaymentMethod())
                .status(PaymentStatus.INITIATED)
                .initiatedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(props.getPaymentExpiryMinutes()))
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Payment initiated: ref={}, order={}", saved.getPaymentReference(), req.getOrderNumber());
        return mapper.toDto(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Process Payment — submit to mock gateway
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PaymentDto processPayment(ProcessPaymentRequest req, String token) {
        Payment payment = paymentRepository.findByPaymentReference(req.getPaymentReference())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found: " + req.getPaymentReference()));

        if (payment.getStatus() != PaymentStatus.INITIATED) {
            throw new InvalidPaymentStateException(
                    "Payment cannot be processed in status: " + payment.getStatus());
        }

        if (payment.isExpired()) {
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);
            throw new InvalidPaymentStateException("Payment session has expired. Please create a new payment.");
        }

        // Mark as processing
        payment.setStatus(PaymentStatus.PROCESSING);
        paymentRepository.save(payment);

        // ── Call mock gateway ─────────────────────────────────────────────────
        GatewayResponse gatewayResponse = gateway.processPayment(
                req.getCardDetails(),
                payment.getAmount(),
                payment.getCurrency());

        payment.setGatewayTransactionId(gatewayResponse.getTransactionId());
        payment.setGatewayResponseCode(gatewayResponse.getResponseCode());
        payment.setGatewayResponseMessage(gatewayResponse.getResponseMessage());
        payment.setGatewayRawResponse(gatewayResponse.toJson());

        if (gatewayResponse.isSuccess()) {
            return handlePaymentSuccess(payment, token);
        } else {
            return handlePaymentFailure(payment, gatewayResponse.getFailureDetail());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cancel Payment
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PaymentDto cancelPayment(CancelPaymentRequest req, Long userId) {
        Payment payment = paymentRepository.findByPaymentReference(req.getPaymentReference())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found: " + req.getPaymentReference()));

        if (payment.getStatus() != PaymentStatus.INITIATED) {
            throw new InvalidPaymentStateException(
                    "Only INITIATED payments can be cancelled. Current: " + payment.getStatus());
        }

        if (!payment.getUserId().equals(userId) && !isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have access to this payment");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setGatewayResponseMessage(req.getReason() != null
                ? req.getReason() : "Cancelled by user");
        Payment saved = paymentRepository.save(payment);

        log.info("Payment cancelled: ref={}", req.getPaymentReference());
        return mapper.toDto(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Refund
    // ─────────────────────────────────────────────────────────────────────────

//    @Override
//    @Transactional
//    public RefundDto initiateRefund(RefundRequest req, Long requestedBy) {
//        Payment payment = paymentRepository.findByPaymentReference(req.getPaymentReference())
//                .orElseThrow(() -> new PaymentNotFoundException(
//                        "Payment not found: " + req.getPaymentReference()));
//
//        if (!payment.canBeRefunded()) {
//            throw new RefundNotAllowedException(
//                    "Payment cannot be refunded. Status: " + payment.getStatus()
//                    + ". Refundable amount: " + payment.getRefundableAmount());
//        }
//
//        if (req.getAmount().compareTo(payment.getRefundableAmount()) > 0) {
//            throw new RefundNotAllowedException(
//                    "Requested refund (" + req.getAmount()
//                    + ") exceeds refundable amount (" + payment.getRefundableAmount() + ")");
//        }
//
//        // Call mock gateway for refund
//        GatewayResponse gatewayResponse = gateway.processRefund(
//                payment.getGatewayTransactionId(),
//                req.getAmount(),
//                req.getReason());
//
//        Refund refund = Refund.builder()
//                .payment(payment)
//                .refundReference(generateRefundRef(payment.getPaymentReference()))
//                .amount(req.getAmount())
//                .reason(req.getReason())
//                .requestedBy(requestedBy)
//                .gatewayResponseCode(gatewayResponse.getResponseCode())
//                .gatewayResponseMessage(gatewayResponse.getResponseMessage())
//                .build();
//
//        if (gatewayResponse.isSuccess()) {
//            refund.setStatus(RefundStatus.SUCCESS);
//            refund.setGatewayRefundId(gatewayResponse.getTransactionId());
//            refund.setProcessedAt(LocalDateTime.now());
//
//            // Update payment refunded amount and status
//            BigDecimal newRefundedAmount = payment.getRefundedAmount().add(req.getAmount());
//            payment.setRefundedAmount(newRefundedAmount);
//            payment.setStatus(newRefundedAmount.compareTo(payment.getAmount()) >= 0
//                    ? PaymentStatus.REFUNDED
//                    : PaymentStatus.PARTIALLY_REFUNDED);
//            paymentRepository.save(payment);
//
//            log.info("Refund successful: ref={}, amount={}", refund.getRefundReference(), req.getAmount());
//
//            // Publish refund processed event
//            eventPublisher.publishRefundProcessed(RefundProcessedEvent.builder()
//                    .paymentId(payment.getId())
//                    .orderId(payment.getOrderId())
//                    .orderNumber(payment.getOrderNumber())
//                    .userId(payment.getUserId())
//                    .userEmail(payment.getUserEmail())
//                    .refundReference(refund.getRefundReference())
//                    .refundAmount(req.getAmount())
//                    .totalRefunded(newRefundedAmount)
//                    .reason(req.getReason())
//                    .processedAt(LocalDateTime.now())
//                    .build());
//
//            // Fire webhook
//            webhookDispatcher.dispatchAsync(WebhookEventType.REFUND_PROCESSED,
//                    payment, buildRefundPayload(refund, payment));
//
//        } else {
//            refund.setStatus(RefundStatus.FAILED);
//            refund.setFailedAt(LocalDateTime.now());
//            refund.setFailureReason(gatewayResponse.getFailureDetail());
//
//            log.warn("Refund failed: ref={}, reason={}", refund.getRefundReference(),
//                    gatewayResponse.getFailureDetail());
//
//            eventPublisher.publishRefundFailed(RefundFailedEvent.builder()
//                    .paymentId(payment.getId())
//                    .orderId(payment.getOrderId())
//                    .orderNumber(payment.getOrderNumber())
//                    .refundReference(refund.getRefundReference())
//                    .refundAmount(req.getAmount())
//                    .failureReason(gatewayResponse.getFailureDetail())
//                    .failedAt(LocalDateTime.now())
//                    .build());
//
//            webhookDispatcher.dispatchAsync(WebhookEventType.REFUND_FAILED,
//                    payment, buildRefundPayload(refund, payment));
//        }
//
//        Refund saved = refundRepository.save(refund);
//        return mapper.toRefundDto(saved);
//    }



    @Override
    @Transactional
    public RefundDto initiateRefund(RefundRequest req, Long requestedBy) {

        Payment payment = paymentRepository.findByPaymentReference(req.getPaymentReference())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found: " + req.getPaymentReference()));

        if (!payment.canBeRefunded()) {
            throw new RefundNotAllowedException(
                    "Payment cannot be refunded. Status: " + payment.getStatus());
        }

        if (req.getAmount().compareTo(payment.getRefundableAmount()) > 0) {
            throw new RefundNotAllowedException("Exceeds refundable amount");
        }

        Refund refund = Refund.builder()
                .payment(payment)
                .refundReference(generateRefundRef(payment.getPaymentReference()))
                .amount(req.getAmount())
                .reason(req.getReason())
                .requestedBy(requestedBy)
                .status(RefundStatus.INITIATED)
                .build();

        Refund saved = refundRepository.save(refund);

        log.info("Refund INITIATED: {}", saved.getRefundReference());

        return mapper.toRefundDto(saved);
    }

    @Override
    @Transactional
    public RefundDto processRefund(String refundReference, Long requestedBy) {

        Refund refund = refundRepository.findByRefundReference(refundReference)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Refund not found: " + refundReference));

        if (refund.getStatus() != RefundStatus.INITIATED) {
            throw new RefundNotAllowedException(
                    "Refund already processed: " + refund.getStatus());
        }

        Payment payment = refund.getPayment();

        GatewayResponse gatewayResponse = gateway.processRefund(
                payment.getGatewayTransactionId(),
                refund.getAmount(),
                refund.getReason()
        );

        if (gatewayResponse.isSuccess()) {

            refund.setStatus(RefundStatus.SUCCESS);
            refund.setGatewayRefundId(gatewayResponse.getTransactionId());
            refund.setProcessedAt(LocalDateTime.now());

            BigDecimal newRefunded = payment.getRefundedAmount().add(refund.getAmount());
            payment.setRefundedAmount(newRefunded);

            payment.setStatus(
                    newRefunded.compareTo(payment.getAmount()) >= 0
                            ? PaymentStatus.REFUNDED
                            : PaymentStatus.PARTIALLY_REFUNDED
            );

            paymentRepository.save(payment);

        } else {

            refund.setStatus(RefundStatus.FAILED);
            refund.setFailureReason(gatewayResponse.getFailureDetail());
            refund.setFailedAt(LocalDateTime.now());
        }

        Refund saved = refundRepository.save(refund);
        return mapper.toRefundDto(saved);
    }




    // ─────────────────────────────────────────────────────────────────────────
    // Read
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PaymentDto getPaymentByReference(String ref, Long userId) {
        Payment payment = paymentRepository.findByPaymentReference(ref)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + ref));
        assertOwnership(payment, userId);
        return mapper.toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDto getPaymentByOrderId(Long orderId, Long userId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "No payment found for order: " + orderId));
        assertOwnership(payment, userId);
        return mapper.toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PaymentSummaryDto> getPaymentHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PaymentSummaryDto> result = paymentRepository
                .findAllByUserId(userId, pageable)
                .map(mapper::toSummaryDto);
        return PagedResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public RefundDto getRefundByReference(String refundRef) {
        Refund refund = refundRepository.findByRefundReference(refundRef)
                .orElseThrow(() -> new PaymentNotFoundException("Refund not found: " + refundRef));
        return mapper.toRefundDto(refund);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Admin
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PaymentSummaryDto> getAllPayments(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PaymentSummaryDto> result = (status != null
                ? paymentRepository.findAllByStatus(PaymentStatus.valueOf(status), pageable)
                : paymentRepository.findAll(pageable))
                .map(mapper::toSummaryDto);
        return PagedResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<RefundDto> getAllRefunds(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<RefundDto> result = (status != null
                ? refundRepository.findAllByStatus(RefundStatus.valueOf(status), pageable)
                : refundRepository.findAll(pageable))
                .map(mapper::toRefundDto);
        return PagedResponse.from(result);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scheduled: expire stale payment sessions
    // ─────────────────────────────────────────────────────────────────────────

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expireStalePayments() {
        List<Payment> expired = paymentRepository.findExpiredPayments(LocalDateTime.now());
        if (expired.isEmpty()) return;

        log.info("Expiring {} stale payment session(s)", expired.size());
        expired.forEach(p -> {
            p.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(p);

            eventPublisher.publishPaymentFailed(PaymentFailedEvent.builder()
                    .paymentId(p.getId()).orderId(p.getOrderId())
                    .orderNumber(p.getOrderNumber()).userId(p.getUserId())
                    .userEmail(p.getUserEmail()).amount(p.getAmount())
                    .paymentReference(p.getPaymentReference())
                    .gatewayResponseCode("TIMEOUT")
                    .failureReason("Payment session expired")
                    .failedAt(LocalDateTime.now()).build());

            log.info("Payment expired: ref={}", p.getPaymentReference());
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private PaymentDto handlePaymentSuccess(Payment payment, String token) {
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setConfirmedAt(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);

        log.info("Payment SUCCESS: ref={}, txn={}", payment.getPaymentReference(),
                payment.getGatewayTransactionId());

        // Notify order-service via Feign (fast path)
        try {
            orderClient.confirmPayment(
                    payment.getOrderId(),
                    new OrderServiceClient.ConfirmPaymentRequest(
                            payment.getPaymentReference(),
                            payment.getPaymentMethod().name()),
                    "Bearer " + token);
        } catch (Exception ex) {
            log.warn("Feign confirmPayment failed — RabbitMQ event will handle it: {}",
                    ex.getMessage());
        }

        // Notify via RabbitMQ (reliable path — fires even if Feign fails)
        eventPublisher.publishPaymentSuccess(PaymentSuccessEvent.builder()
                .paymentId(saved.getId())
                .orderId(saved.getOrderId())
                .orderNumber(saved.getOrderNumber())
                .userId(saved.getUserId())
                .userEmail(saved.getUserEmail())
                .amount(saved.getAmount())
                .currency(saved.getCurrency())
                .paymentMethod(saved.getPaymentMethod().name())
                .paymentReference(saved.getPaymentReference())
                .gatewayTransactionId(saved.getGatewayTransactionId())
                .confirmedAt(saved.getConfirmedAt())
                .status("SUCCESS")
                .build());

        // Fire webhook async
        webhookDispatcher.dispatchAsync(WebhookEventType.PAYMENT_SUCCESS, saved,
                buildPaymentPayload(saved));

        return mapper.toDto(saved);
    }

    private PaymentDto handlePaymentFailure(Payment payment, String detail) {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailedAt(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);

        log.warn("Payment FAILED: ref={}, reason={}", payment.getPaymentReference(), detail);

        eventPublisher.publishPaymentFailed(PaymentFailedEvent.builder()
                .paymentId(saved.getId())
                .orderId(saved.getOrderId())
                .orderNumber(saved.getOrderNumber())
                .userId(saved.getUserId())
                .userEmail(saved.getUserEmail())
                .amount(saved.getAmount())
                .paymentReference(saved.getPaymentReference())
                .gatewayResponseCode(saved.getGatewayResponseCode())
                .failureReason(detail)
                .failedAt(saved.getFailedAt())
                .build());

        webhookDispatcher.dispatchAsync(WebhookEventType.PAYMENT_FAILED, saved,
                buildPaymentPayload(saved));

        return mapper.toDto(saved);
    }

    private void assertOwnership(Payment payment, Long userId) {
        if (!payment.getUserId().equals(userId) && !isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have access to this payment");
        }
    }

    private boolean isAdmin() {
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private String generatePaymentRef(String orderNumber) {
        return "PAY-" + orderNumber + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateRefundRef(String paymentRef) {
        return "REF-" + paymentRef.substring(4, 14) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String buildPaymentPayload(Payment p) {
        return """
                {"paymentId":%d,"orderNumber":"%s","amount":%s,"currency":"%s",
                "status":"%s","paymentReference":"%s","gatewayTransactionId":"%s"}
                """.formatted(p.getId(), p.getOrderNumber(), p.getAmount(),
                p.getCurrency(), p.getStatus(), p.getPaymentReference(),
                p.getGatewayTransactionId());
    }

    private String buildRefundPayload(Refund r, Payment p) {
        return """
                {"refundId":%d,"refundReference":"%s","paymentReference":"%s",
                "orderNumber":"%s","amount":%s,"status":"%s"}
                """.formatted(r.getId(), r.getRefundReference(), p.getPaymentReference(),
                p.getOrderNumber(), r.getAmount(), r.getStatus());
    }
}
