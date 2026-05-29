package com.microservice.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.paymentservice.dto.PaymentDtos.*;
import com.microservice.paymentservice.entity.*;
import com.microservice.paymentservice.exception.*;
import com.microservice.paymentservice.response.PagedResponse;
import com.microservice.paymentservice.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private PaymentService paymentService;

    private PaymentDto samplePaymentDto;

    @BeforeEach
    void setUp() {
        samplePaymentDto = PaymentDto.builder()
                .id(1L).orderId(10L).orderNumber("ORD-001")
                .userId(5L).userEmail("user@example.com")
                .paymentReference("PAY-ORD-001-ABCD1234")
                .amount(new BigDecimal("99.99")).currency("USD")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.INITIATED)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .refundedAmount(BigDecimal.ZERO)
                .refundableAmount(new BigDecimal("99.99"))
                .webhookDelivered(false)
                .refunds(List.of())
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ─── GET /payments/reference/{ref} ────────────────────────────────────────

    @Test
    @DisplayName("GET /payments/reference/{ref} — should return 200")
    @WithMockUser(username = "user@example.com")
    void getByReference_returns200() throws Exception {
        when(paymentService.getPaymentByReference(anyString(), any()))
                .thenReturn(samplePaymentDto);

        mockMvc.perform(get("/payments/reference/PAY-ORD-001-ABCD1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentReference")
                        .value("PAY-ORD-001-ABCD1234"));
    }

    @Test
    @DisplayName("GET /payments/reference/{ref} — should return 404 when not found")
    @WithMockUser(username = "user@example.com")
    void getByReference_notFound_returns404() throws Exception {
        when(paymentService.getPaymentByReference(anyString(), any()))
                .thenThrow(new PaymentNotFoundException("Payment not found: PAY-UNKNOWN"));

        mockMvc.perform(get("/payments/reference/PAY-UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── GET /payments/order/{orderId} ────────────────────────────────────────

    @Test
    @DisplayName("GET /payments/order/{orderId} — should return 200")
    @WithMockUser(username = "user@example.com")
    void getByOrderId_returns200() throws Exception {
        when(paymentService.getPaymentByOrderId(anyLong(), any()))
                .thenReturn(samplePaymentDto);

        mockMvc.perform(get("/payments/order/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-001"));
    }

    // ─── POST /payments/initiate ──────────────────────────────────────────────

    @Test
    @DisplayName("POST /payments/initiate — should return 201")
    @WithMockUser(username = "user@example.com")
    void initiatePayment_returns201() throws Exception {
        InitiatePaymentRequest req = InitiatePaymentRequest.builder()
                .orderId(10L)
                .orderNumber("ORD-001")
                .amount(new BigDecimal("99.99"))
                .currency("USD")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .userEmail("user@example.com")
                .build();

        when(paymentService.initiatePayment(
                any(InitiatePaymentRequest.class),
                anyLong()))
                .thenReturn(samplePaymentDto);

        mockMvc.perform(post("/payments/initiate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.paymentReference")
                        .value("PAY-ORD-001-ABCD1234"));
    }

    @Test
    @DisplayName("POST /payments/initiate — should return 409 when duplicate")
    @WithMockUser(username = "user@example.com")
    void initiatePayment_duplicate_returns409() throws Exception {
        InitiatePaymentRequest req = InitiatePaymentRequest.builder()
                .orderId(10L)
                .orderNumber("ORD-001")
                .amount(new BigDecimal("99.99"))
                .currency("USD")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .userEmail("user@example.com")
                .build();

        when(paymentService.initiatePayment(
                any(InitiatePaymentRequest.class),
                anyLong()))
                .thenThrow(new PaymentAlreadyExistsException(
                        "Payment already exists for order: ORD-001"));

        mockMvc.perform(post("/payments/initiate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    // ─── POST /payments/process ───────────────────────────────────────────────

    @Test
    @DisplayName("POST /payments/process — should return 200 on success")
    @WithMockUser(username = "user@example.com")
    void processPayment_returns200() throws Exception {
        MockCardDetails card = new MockCardDetails(
                "4242424242424242", "12", "2030", "123", "John Doe");
        ProcessPaymentRequest req = new ProcessPaymentRequest(
                "PAY-ORD-001-ABCD1234", card);

        PaymentDto successDto = PaymentDto.builder()
                .id(1L).paymentReference("PAY-ORD-001-ABCD1234")
                .status(PaymentStatus.SUCCESS)
                .amount(new BigDecimal("99.99"))
                .refundedAmount(BigDecimal.ZERO)
                .refundableAmount(new BigDecimal("99.99"))
                .refunds(List.of())
                .build();

        when(paymentService.processPayment(any(), any())).thenReturn(successDto);

        mockMvc.perform(post("/payments/process")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("POST /payments/process — should return 409 when expired")
    @WithMockUser(username = "user@example.com")
    void processPayment_expired_returns409() throws Exception {
        MockCardDetails card = new MockCardDetails(
                "4242424242424242", "12", "2030", "123", "John Doe");
        ProcessPaymentRequest req = new ProcessPaymentRequest(
                "PAY-ORD-001-ABCD1234", card);

        when(paymentService.processPayment(any(), any()))
                .thenThrow(new InvalidPaymentStateException(
                        "Payment session has expired."));

        mockMvc.perform(post("/payments/process")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    // ─── POST /payments/cancel ────────────────────────────────────────────────

    @Test
    @DisplayName("POST /payments/cancel — should return 200")
    @WithMockUser(username = "user@example.com")
    void cancelPayment_returns200() throws Exception {
        CancelPaymentRequest req = new CancelPaymentRequest(
                "PAY-ORD-001-ABCD1234", "Changed my mind");

        PaymentDto cancelledDto = PaymentDto.builder()
                .id(1L).paymentReference("PAY-ORD-001-ABCD1234")
                .status(PaymentStatus.CANCELLED)
                .refundedAmount(BigDecimal.ZERO)
                .refundableAmount(BigDecimal.ZERO)
                .refunds(List.of())
                .build();

        when(paymentService.cancelPayment(any(), any())).thenReturn(cancelledDto);

        mockMvc.perform(post("/payments/cancel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    // ─── GET /payments/my-payments ────────────────────────────────────────────

    @Test
    @DisplayName("GET /payments/my-payments — should return paged history")
    @WithMockUser(username = "user@example.com")
    void getMyPayments_returns200() throws Exception {
        PaymentSummaryDto summary = PaymentSummaryDto.builder()
                .id(1L).orderNumber("ORD-001")
                .amount(new BigDecimal("99.99"))
                .status(PaymentStatus.SUCCESS)
                .build();

        PagedResponse<PaymentSummaryDto> paged = PagedResponse.<PaymentSummaryDto>builder()
                .content(List.of(summary)).page(0).size(10)
                .totalElements(1).totalPages(1).first(true).last(true).build();

        when(paymentService.getPaymentHistory(any(), anyInt(), anyInt()))
                .thenReturn(paged);

        mockMvc.perform(get("/payments/my-payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    // ─── POST /payments/refund ────────────────────────────────────────────────

    @Test
    @DisplayName("POST /payments/refund — should return 201 on success")
    @WithMockUser(username = "user@example.com")
    void initiateRefund_returns201() throws Exception {
        RefundRequest req = new RefundRequest(
                "PAY-ORD-001-ABCD1234", new BigDecimal("50.00"), "Damaged item");

        RefundDto refundDto = RefundDto.builder()
                .id(1L).paymentId(1L).refundReference("REF-001")
                .amount(new BigDecimal("50.00"))
                .status(com.microservice.paymentservice.entity.RefundStatus.SUCCESS)
                .build();

        when(paymentService.initiateRefund(any(), any())).thenReturn(refundDto);

        mockMvc.perform(post("/payments/refund")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("POST /payments/refund — should return 422 when not allowed")
    @WithMockUser(username = "user@example.com")
    void initiateRefund_notAllowed_returns422() throws Exception {
        RefundRequest req = new RefundRequest(
                "PAY-ORD-001-ABCD1234", new BigDecimal("999.99"), "Too much");

        when(paymentService.initiateRefund(any(), any()))
                .thenThrow(new RefundNotAllowedException("Exceeds refundable amount"));

        mockMvc.perform(post("/payments/refund")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity());
    }

    // ─── Unauthenticated ──────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /payments/reference/{ref} — should return 401 when unauthenticated")
    void getByReference_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/payments/reference/PAY-ORD-001"))
                .andExpect(status().isUnauthorized());
    }
}
