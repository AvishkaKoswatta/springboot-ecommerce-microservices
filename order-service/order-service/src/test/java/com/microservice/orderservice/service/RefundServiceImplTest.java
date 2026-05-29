package com.microservice.orderservice.service;

import com.microservice.orderservice.dto.RefundDtos.*;
import com.microservice.orderservice.entity.*;
import com.microservice.orderservice.event.OrderEventPublisher;
import com.microservice.orderservice.exception.*;
import com.microservice.orderservice.mapper.RefundMapper;
import com.microservice.orderservice.repository.OrderRepository;
import com.microservice.orderservice.repository.RefundRequestRepository;
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
class RefundServiceImplTest {

    @Mock private RefundRequestRepository refundRepository;
    @Mock private OrderRepository         orderRepository;
    @Mock private RefundMapper            refundMapper;
    @Mock private OrderEventPublisher     eventPublisher;

    @InjectMocks private RefundServiceImpl refundService;

    private Order deliveredOrder;
    private RefundRequest pendingRefund;
    private RefundDto refundDto;

    @BeforeEach
    void setUp() {
        deliveredOrder = Order.builder()
                .id(1L).orderNumber("ORD-001")
                .userId(10L).userEmail("user@example.com")
                .status(OrderStatus.DELIVERED)
                .totalAmount(new BigDecimal("85.99"))
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        pendingRefund = RefundRequest.builder()
                .id(1L).order(deliveredOrder)
                .status(RefundStatus.PENDING)
                .reasonType(RefundReasonType.DAMAGED_PRODUCT)
                .requestedAmount(new BigDecimal("85.99"))
                .build();

        refundDto = RefundDto.builder()
                .id(1L).orderId(1L).orderNumber("ORD-001")
                .status(RefundStatus.PENDING)
                .requestedAmount(new BigDecimal("85.99"))
                .build();
    }

    @Test
    @DisplayName("requestRefund() — should create refund for DELIVERED order")
    void requestRefund_success() {
        RefundRequestDto req = new RefundRequestDto(
                RefundReasonType.DAMAGED_PRODUCT, "Item arrived broken",
                new BigDecimal("85.99"));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(deliveredOrder));
        when(refundRepository.existsByOrderId(1L)).thenReturn(false);
        when(refundRepository.save(any(RefundRequest.class))).thenReturn(pendingRefund);
        when(orderRepository.save(any())).thenReturn(deliveredOrder);
        when(refundMapper.toDto(pendingRefund)).thenReturn(refundDto);
        doNothing().when(eventPublisher).publishRefundRequested(any());

        RefundDto result = refundService.requestRefund(1L, req, 10L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RefundStatus.PENDING);
        verify(refundRepository).save(any(RefundRequest.class));
        verify(eventPublisher).publishRefundRequested(any());
    }

    @Test
    @DisplayName("requestRefund() — should throw when order not DELIVERED")
    void requestRefund_wrongState_throws() {
        deliveredOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(deliveredOrder));

        RefundRequestDto req = new RefundRequestDto(
                RefundReasonType.CHANGED_MIND, null, new BigDecimal("50.00"));

        assertThatThrownBy(() -> refundService.requestRefund(1L, req, 10L))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("DELIVERED or COMPLETED");
    }

    @Test
    @DisplayName("requestRefund() — should throw when refund already exists")
    void requestRefund_alreadyExists_throws() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(deliveredOrder));
        when(refundRepository.existsByOrderId(1L)).thenReturn(true);

        RefundRequestDto req = new RefundRequestDto(
                RefundReasonType.CHANGED_MIND, null, new BigDecimal("50.00"));

        assertThatThrownBy(() -> refundService.requestRefund(1L, req, 10L))
                .isInstanceOf(RefundAlreadyExistsException.class);
    }

    @Test
    @DisplayName("requestRefund() — should throw when amount exceeds order total")
    void requestRefund_excessiveAmount_throws() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(deliveredOrder));
        when(refundRepository.existsByOrderId(1L)).thenReturn(false);

        RefundRequestDto req = new RefundRequestDto(
                RefundReasonType.DAMAGED_PRODUCT, null, new BigDecimal("999.99")); // too high

        assertThatThrownBy(() -> refundService.requestRefund(1L, req, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot exceed order total");
    }

    @Test
    @DisplayName("resolveRefund() — should approve and set order to REFUNDED")
    void resolveRefund_approve_success() {
        ResolveRefundRequest req = new ResolveRefundRequest(
                RefundStatus.APPROVED, new BigDecimal("85.99"), "Approved — clear case");

        when(refundRepository.findById(1L)).thenReturn(Optional.of(pendingRefund));
        when(refundRepository.save(any())).thenReturn(pendingRefund);
        when(orderRepository.save(any())).thenReturn(deliveredOrder);
        when(refundMapper.toDto(any())).thenReturn(refundDto);

        refundService.resolveRefund(1L, req, 99L);

        assertThat(deliveredOrder.getStatus()).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    @DisplayName("resolveRefund() — should reject and revert order to COMPLETED")
    void resolveRefund_reject_orderRevertedToCompleted() {
        ResolveRefundRequest req = new ResolveRefundRequest(
                RefundStatus.REJECTED, null, "No valid reason provided");

        when(refundRepository.findById(1L)).thenReturn(Optional.of(pendingRefund));
        when(refundRepository.save(any())).thenReturn(pendingRefund);
        when(orderRepository.save(any())).thenReturn(deliveredOrder);
        when(refundMapper.toDto(any())).thenReturn(refundDto);

        refundService.resolveRefund(1L, req, 99L);

        assertThat(deliveredOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("resolveRefund() — should throw when refund already resolved")
    void resolveRefund_alreadyResolved_throws() {
        pendingRefund.setStatus(RefundStatus.APPROVED); // already resolved
        when(refundRepository.findById(1L)).thenReturn(Optional.of(pendingRefund));

        assertThatThrownBy(() -> refundService.resolveRefund(1L,
                new ResolveRefundRequest(RefundStatus.REJECTED, null, ""), 99L))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("already been resolved");
    }
}
