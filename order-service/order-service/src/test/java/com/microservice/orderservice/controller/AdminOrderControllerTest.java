package com.microservice.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.orderservice.dto.OrderDtos.*;
import com.microservice.orderservice.entity.*;
import com.microservice.orderservice.exception.InvalidOrderStateException;
import com.microservice.orderservice.response.PagedResponse;
import com.microservice.orderservice.service.OrderService;
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

@WebMvcTest(AdminOrderController.class)
class AdminOrderControllerTest {

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private OrderService orderService;

    private OrderDto sampleOrderDto;

    @BeforeEach
    void setUp() {
        sampleOrderDto = OrderDto.builder()
                .id(1L).orderNumber("ORD-001")
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("85.99"))
                .createdAt(LocalDateTime.now())
                .items(List.of()).statusHistory(List.of()).build();
    }

    @Test
    @DisplayName("GET /admin/orders — ADMIN should get paginated orders")
    @WithMockUser(roles = "ADMIN")
    void getAllOrders_asAdmin_returns200() throws Exception {
        PagedResponse<OrderSummaryDto> paged = PagedResponse.<OrderSummaryDto>builder()
                .content(List.of(OrderSummaryDto.builder().id(1L).orderNumber("ORD-001").build()))
                .page(0).size(20).totalElements(1).totalPages(1).first(true).last(true).build();

        when(orderService.getAllOrders(anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(paged);

        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /admin/orders — USER role should get 403")
    @WithMockUser(roles = "USER")
    void getAllOrders_asUser_returns403() throws Exception {
        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /admin/orders/{id}/status — should update status")
    @WithMockUser(roles = "ADMIN")
    void updateOrderStatus_asAdmin_returns200() throws Exception {
        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(
                OrderStatus.PROCESSING, "Being packed", null);

        when(orderService.updateOrderStatus(anyLong(), any(), any())).thenReturn(sampleOrderDto);

        mockMvc.perform(patch("/admin/orders/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /admin/orders/{id}/status — should return 409 on invalid transition")
    @WithMockUser(roles = "ADMIN")
    void updateOrderStatus_invalidTransition_returns409() throws Exception {
        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(
                OrderStatus.PENDING, "Invalid", null);

        when(orderService.updateOrderStatus(anyLong(), any(), any()))
                .thenThrow(new InvalidOrderStateException("Cannot transition CONFIRMED → PENDING"));

        mockMvc.perform(patch("/admin/orders/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }
}
