package com.microservice.orderservice.service;

import com.microservice.orderservice.client.ProductServiceClient;
import com.microservice.orderservice.client.UserServiceClient;
import com.microservice.orderservice.config.OrderProperties;
import com.microservice.orderservice.dto.OrderDtos.*;
import com.microservice.orderservice.entity.*;
import com.microservice.orderservice.event.OrderEventPublisher;
import com.microservice.orderservice.exception.*;
import com.microservice.orderservice.mapper.OrderMapper;
import com.microservice.orderservice.repository.OrderRepository;
import com.microservice.orderservice.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository      orderRepository;
    @Mock private OrderMapper          orderMapper;
    @Mock private OrderEventPublisher  eventPublisher;
    @Mock private ProductServiceClient productClient;
    @Mock private UserServiceClient    userClient;
    @Mock private OrderProperties      props;

    @InjectMocks private OrderServiceImpl orderService;

    private Order testOrder;
    private OrderDto testOrderDto;
    private ProductServiceClient.ProductDto activeProduct;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .id(1L).orderNumber("ORD-20240521-00001")
                .userId(10L).userEmail("user@example.com")
                .status(OrderStatus.PENDING)
                .subtotal(new BigDecimal("80.00"))
                .shippingFee(new BigDecimal("5.99"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("85.99"))
                .shippingAddress(ShippingAddress.builder()
                        .recipientName("John Doe").phone("+1234567890")
                        .addressLine1("123 Main St").city("Colombo")
                        .postalCode("10000").country("Sri Lanka").build())
                .createdAt(LocalDateTime.now())
                .build();

        testOrderDto = OrderDto.builder()
                .id(1L).orderNumber("ORD-20240521-00001")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("85.99"))
                .build();

        activeProduct = new ProductServiceClient.ProductDto(
                1L, "Test Product", "SKU-001", "ACTIVE",
                new BigDecimal("40.00"), 100, true, false, null);
    }

    // ─── placeOrder ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("placeOrder() — should create order and return dto")
    void placeOrder_success() {
        when(props.getFreeShippingThreshold()).thenReturn(new BigDecimal("100.00"));
        when(props.getFlatShippingFee()).thenReturn(new BigDecimal("5.99"));

        ApiResponse<ProductServiceClient.ProductDto> productResponse =
                ApiResponse.<ProductServiceClient.ProductDto>builder()
                        .success(true).data(activeProduct).build();
        when(productClient.getProductById(1L)).thenReturn(productResponse);
        when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDto(testOrder)).thenReturn(testOrderDto);
        doNothing().when(eventPublisher).publishOrderPlaced(any());

        PlaceOrderRequest req = PlaceOrderRequest.builder()
                .items(List.of(new CartItemRequest(1L, 2)))
                .shippingAddress(ShippingAddressRequest.builder()
                        .recipientName("John Doe").phone("+1234567890")
                        .addressLine1("123 Main St").city("Colombo")
                        .postalCode("10000").country("Sri Lanka").build())
                .build();

        OrderDto result = orderService.placeOrder(req, 10L, "user@example.com", "token");

        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isEqualTo("ORD-20240521-00001");
        verify(orderRepository).save(any(Order.class));
        verify(eventPublisher).publishOrderPlaced(any());
    }

    @Test
    @DisplayName("placeOrder() — order is always placed as PENDING")
    void placeOrder_alwaysPending() {
        when(props.getFreeShippingThreshold()).thenReturn(new BigDecimal("100.00"));
        when(props.getFlatShippingFee()).thenReturn(new BigDecimal("5.99"));

        ApiResponse<ProductServiceClient.ProductDto> productResponse =
                ApiResponse.<ProductServiceClient.ProductDto>builder()
                        .success(true).data(activeProduct).build();
        when(productClient.getProductById(1L)).thenReturn(productResponse);
        when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(orderCaptor.capture())).thenReturn(testOrder);
        when(orderMapper.toDto(testOrder)).thenReturn(testOrderDto);
        doNothing().when(eventPublisher).publishOrderPlaced(any());

        PlaceOrderRequest req = PlaceOrderRequest.builder()
                .items(List.of(new CartItemRequest(1L, 1)))
                .shippingAddress(ShippingAddressRequest.builder()
                        .recipientName("John Doe").phone("+1234567890")
                        .addressLine1("123 Main St").city("Colombo")
                        .postalCode("10000").country("Sri Lanka").build())
                .build();

        orderService.placeOrder(req, 10L, "user@example.com", "token");

        Order capturedOrder = orderCaptor.getValue();
        assertThat(capturedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(eventPublisher).publishOrderPlaced(any());
        verify(eventPublisher, never()).publishOrderConfirmed(any());
    }

    @Test
    @DisplayName("placeOrder() — should throw when product is unavailable")
    void placeOrder_productUnavailable_throws() {
        when(props.getFreeShippingThreshold()).thenReturn(new BigDecimal("100.00"));
        when(props.getFlatShippingFee()).thenReturn(new BigDecimal("5.99"));

        ProductServiceClient.ProductDto inactiveProduct =
                new ProductServiceClient.ProductDto(2L, "Inactive", "SKU-002",
                        "INACTIVE", new BigDecimal("20.00"), 0, true, false, null);

        when(productClient.getProductById(2L)).thenReturn(
                ApiResponse.<ProductServiceClient.ProductDto>builder()
                        .success(true).data(inactiveProduct).build());

        PlaceOrderRequest req = PlaceOrderRequest.builder()
                .items(List.of(new CartItemRequest(2L, 1)))
                .shippingAddress(ShippingAddressRequest.builder()
                        .recipientName("John").phone("+1234567890")
                        .addressLine1("123 St").city("City")
                        .postalCode("10000").country("LK").build())
                .build();

        assertThatThrownBy(() -> orderService.placeOrder(req, 10L, "u@e.com", "token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not available");
    }

    @Test
    @DisplayName("placeOrder() — should throw on insufficient stock")
    void placeOrder_insufficientStock_throws() {
        when(props.getFreeShippingThreshold()).thenReturn(new BigDecimal("100.00"));
        when(props.getFlatShippingFee()).thenReturn(new BigDecimal("5.99"));

        ProductServiceClient.ProductDto lowStockProduct =
                new ProductServiceClient.ProductDto(1L, "Low Stock", "SKU-001",
                        "ACTIVE", new BigDecimal("40.00"), 1, true, false, null);

        when(productClient.getProductById(1L)).thenReturn(
                ApiResponse.<ProductServiceClient.ProductDto>builder()
                        .success(true).data(lowStockProduct).build());

        PlaceOrderRequest req = PlaceOrderRequest.builder()
                .items(List.of(new CartItemRequest(1L, 5))) // request 5, only 1 available
                .shippingAddress(ShippingAddressRequest.builder()
                        .recipientName("John").phone("+1234567890")
                        .addressLine1("123 St").city("City")
                        .postalCode("10000").country("LK").build())
                .build();

        assertThatThrownBy(() -> orderService.placeOrder(req, 10L, "u@e.com", "token"))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("placeOrder() — free shipping when subtotal above threshold")
    void placeOrder_freeShipping_whenAboveThreshold() {
        when(props.getFreeShippingThreshold()).thenReturn(new BigDecimal("100.00"));
        when(props.getFlatShippingFee()).thenReturn(new BigDecimal("5.99"));

        // 3 units × £40 = £120 → above threshold → free shipping
        ProductServiceClient.ProductDto product =
                new ProductServiceClient.ProductDto(1L, "Product", "SKU-001",
                        "ACTIVE", new BigDecimal("40.00"), 100, true, false, null);

        when(productClient.getProductById(1L)).thenReturn(
                ApiResponse.<ProductServiceClient.ProductDto>builder()
                        .success(true).data(product).build());
        when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(captor.capture())).thenReturn(testOrder);
        when(orderMapper.toDto(testOrder)).thenReturn(testOrderDto);
        doNothing().when(eventPublisher).publishOrderPlaced(any());

        PlaceOrderRequest req = PlaceOrderRequest.builder()
                .items(List.of(new CartItemRequest(1L, 3)))
                .shippingAddress(ShippingAddressRequest.builder()
                        .recipientName("John").phone("+1234567890")
                        .addressLine1("123 St").city("City")
                        .postalCode("10000").country("LK").build())
                .build();

        orderService.placeOrder(req, 10L, "u@e.com", "token");

        Order saved = captor.getValue();
        assertThat(saved.getShippingFee()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ─── cancelOrder ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("cancelOrder() — should cancel PENDING order within window")
    void cancelOrder_success() {
        when(props.getCancellationWindowMinutes()).thenReturn(30);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any())).thenReturn(testOrder);
        doNothing().when(eventPublisher).publishOrderCancelled(any());

        CancelOrderRequest req = new CancelOrderRequest("Changed my mind");
        assertThatCode(() -> orderService.cancelOrder(1L, 10L, req)).doesNotThrowAnyException();

        verify(orderRepository).save(any(Order.class));
        verify(eventPublisher).publishOrderCancelled(any());
    }

    @Test
    @DisplayName("cancelOrder() — should throw when order already shipped")
    void cancelOrder_alreadyShipped_throws() {
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() ->
                orderService.cancelOrder(1L, 10L, new CancelOrderRequest("Want to cancel")))
                .isInstanceOf(OrderNotCancellableException.class);
    }

    @Test
    @DisplayName("cancelOrder() — should throw when not order owner")
    void cancelOrder_wrongUser_throws() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Different userId
        assertThatThrownBy(() ->
                orderService.cancelOrder(1L, 99L, new CancelOrderRequest("Cancel")))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    // ─── getOrderHistory ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getOrderHistory() — should return paged orders for user")
    void getOrderHistory_success() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Order> page = new PageImpl<>(List.of(testOrder), pageable, 1);

        when(orderRepository.findOrderHistory(eq(10L), any(Pageable.class))).thenReturn(page);
        when(orderMapper.toSummaryDto(testOrder)).thenReturn(
                OrderSummaryDto.builder().id(1L).orderNumber("ORD-20240521-00001").build());

        var result = orderService.getOrderHistory(10L, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
