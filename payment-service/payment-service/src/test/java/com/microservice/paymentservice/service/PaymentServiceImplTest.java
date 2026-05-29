package com.microservice.paymentservice.service;

import com.microservice.paymentservice.client.OrderServiceClient;
import com.microservice.paymentservice.config.PaymentProperties;
import com.microservice.paymentservice.dto.PaymentDtos.*;
import com.microservice.paymentservice.entity.*;
import com.microservice.paymentservice.event.PaymentEventPublisher;
import com.microservice.paymentservice.exception.*;
import com.microservice.paymentservice.mapper.PaymentMapper;
import com.microservice.paymentservice.repository.PaymentRepository;
import com.microservice.paymentservice.repository.RefundRepository;
import com.microservice.paymentservice.repository.WebhookEventRepository;
import com.microservice.paymentservice.util.MockGatewayService;
import com.microservice.paymentservice.util.MockGatewayService.GatewayResponse;
import com.microservice.paymentservice.webhook.WebhookDispatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private PaymentRepository      paymentRepository;
    @Mock private RefundRepository       refundRepository;
    @Mock private WebhookEventRepository webhookRepository;
    @Mock private PaymentMapper          mapper;
    @Mock private MockGatewayService     gateway;
    @Mock private PaymentEventPublisher  eventPublisher;
    @Mock private OrderServiceClient     orderClient;
    @Mock private WebhookDispatchService webhookDispatcher;
    @Mock private PaymentProperties      props;

    @InjectMocks private PaymentServiceImpl paymentService;

    private Payment testPayment;
    private PaymentDto testPaymentDto;

    @BeforeEach
    void setUp() {
        testPayment = Payment.builder()
                .id(1L)
                .orderId(10L)
                .orderNumber("ORD-001")
                .userId(5L)
                .userEmail("user@example.com")
                .paymentReference("PAY-ORD-001-ABCD1234")
                .amount(new BigDecimal("99.99"))
                .currency("USD")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.INITIATED)
                .initiatedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .refundedAmount(BigDecimal.ZERO)
                .build();

        testPaymentDto = PaymentDto.builder()
                .id(1L)
                .orderNumber("ORD-001")
                .paymentReference("PAY-ORD-001-ABCD1234")
                .amount(new BigDecimal("99.99"))
                .status(PaymentStatus.INITIATED)
                .build();
    }

    // ─── initiatePayment ─────────────────────────────────────────────────────


    @Test
    @DisplayName("initiatePayment() — should create payment record")
    void initiatePayment_success() {
        when(paymentRepository.existsByOrderId(10L)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(mapper.toDto(testPayment)).thenReturn(testPaymentDto);

        InitiatePaymentRequest req = InitiatePaymentRequest.builder()
                .orderId(10L)
                .orderNumber("ORD-001")
                .amount(new BigDecimal("99.99"))
                .currency("USD")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .userEmail("user@example.com")
                .build();

        when(props.getPaymentExpiryMinutes()).thenReturn(15);

        // FIX: pass userId separately
        PaymentDto result = paymentService.initiatePayment(req, 5L);

        assertThat(result).isNotNull();
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("initiatePayment() — should throw when payment already exists")
    void initiatePayment_duplicate_throws() {
        when(paymentRepository.existsByOrderId(10L)).thenReturn(true);

        InitiatePaymentRequest req = InitiatePaymentRequest.builder()
                .orderId(10L)
                .orderNumber("ORD-001")
                .amount(new BigDecimal("99.99"))
                .currency("USD")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .userEmail("user@example.com")
                .build();

        // FIX: pass userId separately
        assertThatThrownBy(() -> paymentService.initiatePayment(req, 5L))
                .isInstanceOf(PaymentAlreadyExistsException.class);
    }

    // ─── processPayment ───────────────────────────────────────────────────────

    @Test
    @DisplayName("processPayment() — should return SUCCESS when gateway approves")
    void processPayment_gatewaySuccess() {
        MockCardDetails card = new MockCardDetails(
                "4242424242424242", "12", "2030", "123", "John Doe");

        GatewayResponse gatewayResp = GatewayResponse.success("TXN-123", "00", "Approved");

        when(paymentRepository.findByPaymentReference("PAY-ORD-001-ABCD1234"))
                .thenReturn(Optional.of(testPayment));
        when(gateway.processPayment(any(), any(), any())).thenReturn(gatewayResp);

        Payment successPayment = Payment.builder()
                .id(1L).orderNumber("ORD-001").orderId(10L).userId(5L)
                .userEmail("user@example.com")
                .paymentReference("PAY-ORD-001-ABCD1234")
                .amount(new BigDecimal("99.99")).currency("USD")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.SUCCESS)
                .gatewayTransactionId("TXN-123")
                .refundedAmount(BigDecimal.ZERO)
                .confirmedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        when(paymentRepository.save(any())).thenReturn(successPayment);
        when(mapper.toDto(any())).thenReturn(testPaymentDto);
        doNothing().when(eventPublisher).publishPaymentSuccess(any());
        doNothing().when(webhookDispatcher).dispatchAsync(any(), any(), any());

        ProcessPaymentRequest req = new ProcessPaymentRequest("PAY-ORD-001-ABCD1234", card);
        PaymentDto result = paymentService.processPayment(req, "test-token");

        assertThat(result).isNotNull();
        verify(eventPublisher).publishPaymentSuccess(any());
        verify(webhookDispatcher).dispatchAsync(any(), any(), any());
    }

    @Test
    @DisplayName("processPayment() — should return FAILED when gateway declines")
    void processPayment_gatewayFailure() {
        MockCardDetails card = new MockCardDetails(
                "4000000000000002", "12", "2030", "123", "John Doe");

        GatewayResponse gatewayResp = GatewayResponse.failure(
                "TXN-FAIL", "05", "Do not honour", "Card declined");

        when(paymentRepository.findByPaymentReference("PAY-ORD-001-ABCD1234"))
                .thenReturn(Optional.of(testPayment));
        when(gateway.processPayment(any(), any(), any())).thenReturn(gatewayResp);

        Payment failedPayment = Payment.builder()
                .id(1L).orderNumber("ORD-001").orderId(10L).userId(5L)
                .userEmail("user@example.com")
                .paymentReference("PAY-ORD-001-ABCD1234")
                .amount(new BigDecimal("99.99")).currency("USD")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.FAILED)
                .refundedAmount(BigDecimal.ZERO)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        when(paymentRepository.save(any())).thenReturn(failedPayment);
        when(mapper.toDto(any())).thenReturn(testPaymentDto);
        doNothing().when(eventPublisher).publishPaymentFailed(any());
        doNothing().when(webhookDispatcher).dispatchAsync(any(), any(), any());

        ProcessPaymentRequest req = new ProcessPaymentRequest("PAY-ORD-001-ABCD1234", card);
        paymentService.processPayment(req, "test-token");

        verify(eventPublisher).publishPaymentFailed(any());
        verify(eventPublisher, never()).publishPaymentSuccess(any());
    }

    @Test
    @DisplayName("processPayment() — should throw when payment expired")
    void processPayment_expired_throws() {
        testPayment.setExpiresAt(LocalDateTime.now().minusMinutes(1)); // already expired

        when(paymentRepository.findByPaymentReference("PAY-ORD-001-ABCD1234"))
                .thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any())).thenReturn(testPayment);

        MockCardDetails card = new MockCardDetails(
                "4242424242424242", "12", "2030", "123", "John Doe");

        assertThatThrownBy(() -> paymentService.processPayment(
                new ProcessPaymentRequest("PAY-ORD-001-ABCD1234", card), "token"))
                .isInstanceOf(InvalidPaymentStateException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("processPayment() — should throw when not in INITIATED state")
    void processPayment_wrongState_throws() {
        testPayment.setStatus(PaymentStatus.SUCCESS);

        when(paymentRepository.findByPaymentReference("PAY-ORD-001-ABCD1234"))
                .thenReturn(Optional.of(testPayment));

        MockCardDetails card = new MockCardDetails(
                "4242424242424242", "12", "2030", "123", "John Doe");

        assertThatThrownBy(() -> paymentService.processPayment(
                new ProcessPaymentRequest("PAY-ORD-001-ABCD1234", card), "token"))
                .isInstanceOf(InvalidPaymentStateException.class);
    }

    // ─── initiateRefund ───────────────────────────────────────────────────────

    @Test
    @DisplayName("initiateRefund() — should refund successfully")
    void initiateRefund_success() {
        testPayment.setStatus(PaymentStatus.SUCCESS);
        testPayment.setGatewayTransactionId("TXN-SUCCESS");

        when(paymentRepository.findByPaymentReference("PAY-ORD-001-ABCD1234"))
                .thenReturn(Optional.of(testPayment));

        GatewayResponse refundResp = GatewayResponse.success("REF-TXN-001", "00", "Refund approved");
        when(gateway.processRefund(anyString(), any(), anyString())).thenReturn(refundResp);

        com.microservice.paymentservice.entity.Refund savedRefund =
                com.microservice.paymentservice.entity.Refund.builder()
                        .id(1L).payment(testPayment)
                        .refundReference("REF-001")
                        .amount(new BigDecimal("50.00"))
                        .status(com.microservice.paymentservice.entity.RefundStatus.SUCCESS)
                        .build();

        when(refundRepository.save(any())).thenReturn(savedRefund);
        when(paymentRepository.save(any())).thenReturn(testPayment);
        when(mapper.toRefundDto(any())).thenReturn(new RefundDto());
        doNothing().when(eventPublisher).publishRefundProcessed(any());
        doNothing().when(webhookDispatcher).dispatchAsync(any(), any(), any());

        RefundRequest req = new RefundRequest("PAY-ORD-001-ABCD1234",
                new BigDecimal("50.00"), "Customer request");

        RefundDto result = paymentService.initiateRefund(req, 5L);

        assertThat(result).isNotNull();
        verify(eventPublisher).publishRefundProcessed(any());
    }

    @Test
    @DisplayName("initiateRefund() — should throw when refund exceeds refundable amount")
    void initiateRefund_excessAmount_throws() {
        testPayment.setStatus(PaymentStatus.SUCCESS);
        testPayment.setRefundedAmount(new BigDecimal("50.00")); // already refunded 50

        when(paymentRepository.findByPaymentReference("PAY-ORD-001-ABCD1234"))
                .thenReturn(Optional.of(testPayment));

        // Try to refund 99.99 but only 49.99 is refundable (99.99 - 50.00)
        RefundRequest req = new RefundRequest("PAY-ORD-001-ABCD1234",
                new BigDecimal("99.99"), "Greedy refund");

        assertThatThrownBy(() -> paymentService.initiateRefund(req, 5L))
                .isInstanceOf(RefundNotAllowedException.class)
                .hasMessageContaining("exceeds refundable amount");
    }

    @Test
    @DisplayName("initiateRefund() — should throw when payment not SUCCESS")
    void initiateRefund_notSuccess_throws() {
        testPayment.setStatus(PaymentStatus.INITIATED);

        when(paymentRepository.findByPaymentReference("PAY-ORD-001-ABCD1234"))
                .thenReturn(Optional.of(testPayment));

        assertThatThrownBy(() -> paymentService.initiateRefund(
                new RefundRequest("PAY-ORD-001-ABCD1234",
                        new BigDecimal("50.00"), "reason"), 5L))
                .isInstanceOf(RefundNotAllowedException.class);
    }
}
