package com.microservice.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.orderservice.dto.OrderDtos.*;
import com.microservice.orderservice.entity.*;
import com.microservice.orderservice.exception.*;
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

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private OrderService orderService;

    private OrderDto sampleOrderDto;

    @BeforeEach
    void setUp() {
        sampleOrderDto = OrderDto.builder()
                .id(1L).orderNumber("ORD-20240521-00001")
                .userId(10L).userEmail("user@example.com")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("85.99"))
                .createdAt(LocalDateTime.now())
                .items(List.of())
                .statusHistory(List.of())
                .build();
    }

    @Test
    @DisplayName("GET /orders/{id} — should return 200 for order owner")
    @WithMockUser(username = "user@example.com")
    void getOrderById_returns200() throws Exception {
        when(orderService.getOrderById(anyLong(), any())).thenReturn(sampleOrderDto);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-20240521-00001"));
    }

    @Test
    @DisplayName("GET /orders/{id} — should return 404 when not found")
    @WithMockUser(username = "user@example.com")
    void getOrderById_notFound_returns404() throws Exception {
        when(orderService.getOrderById(anyLong(), any()))
                .thenThrow(new OrderNotFoundException(99L));

        mockMvc.perform(get("/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /orders/my-orders — should return paginated order history")
    @WithMockUser(username = "user@example.com")
    void getMyOrders_returns200() throws Exception {
        PagedResponse<OrderSummaryDto> paged = PagedResponse.<OrderSummaryDto>builder()
                .content(List.of(OrderSummaryDto.builder()
                        .id(1L).orderNumber("ORD-001")
                        .status(OrderStatus.CONFIRMED)
                        .totalAmount(new BigDecimal("85.99")).build()))
                .page(0).size(10).totalElements(1).totalPages(1)
                .first(true).last(true).build();

        when(orderService.getOrderHistory(any(), anyInt(), anyInt())).thenReturn(paged);

        mockMvc.perform(get("/orders/my-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("POST /orders/{id}/cancel — should return 200 on valid cancel")
    @WithMockUser(username = "user@example.com")
    void cancelOrder_returns200() throws Exception {
        doNothing().when(orderService).cancelOrder(anyLong(), any(), any());

        mockMvc.perform(post("/orders/1/cancel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CancelOrderRequest("Changed my mind"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /orders/{id}/cancel — should return 422 when not cancellable")
    @WithMockUser(username = "user@example.com")
    void cancelOrder_notCancellable_returns422() throws Exception {
        doThrow(new OrderNotCancellableException("Order cannot be cancelled in status: SHIPPED"))
                .when(orderService).cancelOrder(anyLong(), any(), any());

        mockMvc.perform(post("/orders/1/cancel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CancelOrderRequest("Changed my mind"))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("GET /orders/{id} — should return 401 for unauthenticated request")
    void getOrderById_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isUnauthorized());
    }
}
