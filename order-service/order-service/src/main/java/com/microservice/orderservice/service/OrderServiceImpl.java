package com.microservice.orderservice.service;

import com.microservice.orderservice.client.ProductServiceClient;
import com.microservice.orderservice.client.UserServiceClient;
import com.microservice.orderservice.config.OrderProperties;
import com.microservice.orderservice.dto.OrderDtos.*;
import com.microservice.orderservice.entity.*;
import com.microservice.orderservice.event.OrderEventPublisher;
import com.microservice.orderservice.event.OrderEvents.*;
import com.microservice.orderservice.exception.*;
import com.microservice.orderservice.mapper.OrderMapper;
import com.microservice.orderservice.repository.OrderRepository;
import com.microservice.orderservice.response.PagedResponse;
import com.microservice.orderservice.util.OrderNumberGenerator;
import com.microservice.orderservice.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository      orderRepository;
    private final OrderMapper          orderMapper;
    private final OrderEventPublisher  eventPublisher;
    private final ProductServiceClient productClient;
    private final UserServiceClient    userClient;
    private final OrderProperties      props;
    private final OrderEmailService    emailService;

    // ─────────────────────────────────────────────────────────────────────────
    // Place Order
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public OrderDto placeOrder(PlaceOrderRequest req, Long userId,
                               String userEmail, String token) {

        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        // ── Validate every product against product-service ────────────────────
        for (CartItemRequest cartItem : req.getItems()) {

            var response = productClient.getProductById(cartItem.getProductId());

            if (response == null || !response.isSuccess() || response.getData() == null) {
                throw new ProductServiceException(
                        "Could not validate product ID: " + cartItem.getProductId()
                                + ". product-service may be unavailable.");
            }

            ProductServiceClient.ProductDto product = response.getData();

            if (!"ACTIVE".equals(product.status())) {
                throw new IllegalArgumentException(
                        "Product '" + product.name() + "' is not available for purchase.");
            }

            if (product.trackInventory()
                    && !product.allowBackorder()
                    && product.availableQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for '" + product.name()
                                + "'. Available: " + product.availableQuantity()
                                + ", requested: " + cartItem.getQuantity());
            }

            BigDecimal lineTotal = product.price()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            orderItems.add(OrderItem.builder()
                    .productId(product.id())
                    .productName(product.name())
                    .productSku(product.sku())
                    .productImageUrl(product.primaryImageUrl())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(product.price())
                    .totalPrice(lineTotal)
                    .build());
        }

        // ── Shipping fee — free above threshold ───────────────────────────────
        BigDecimal shippingFee = subtotal.compareTo(props.getFreeShippingThreshold()) >= 0
                ? BigDecimal.ZERO
                : props.getFlatShippingFee();

        BigDecimal totalAmount = subtotal.add(shippingFee);

        // ── Build shipping address ─────────────────────────────────────────────
        ShippingAddressRequest sa = req.getShippingAddress();
        ShippingAddress address = ShippingAddress.builder()
                .recipientName(sa.getRecipientName())
                .phone(sa.getPhone())
                .addressLine1(sa.getAddressLine1())
                .addressLine2(sa.getAddressLine2())
                .city(sa.getCity())
                .state(sa.getState())
                .postalCode(sa.getPostalCode())
                .country(sa.getCountry())
                .build();

        String orderNumber = generateUniqueOrderNumber();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userId(userId)
                .userEmail(userEmail)
                .status(OrderStatus.PENDING)
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(totalAmount)
                .shippingAddress(address)

                .paymentStatus("PENDING")
                .customerNotes(req.getCustomerNotes())
                .build();

        orderItems.forEach(order::addItem);

        order.addStatusHistory(OrderStatusHistory.builder()
                .toStatus(OrderStatus.PENDING)
                .note("Order placed — awaiting payment")
                .changedByRole("CUSTOMER")
                .build());

        // ── Step 1: Persist order ─────────────────────────────────────────────
        Order saved = orderRepository.save(order);
        log.info("Order saved: {} by user {}", orderNumber, userId);

        // ── Step 2: Reserve stock via Feign (synchronous) ─────────────────────
        // All orders use RESERVATION until payment-service confirms payment.
        // Stock is finalised (RESERVATION_CANCEL + STOCK_OUT) by payment-service
        // after successful payment, or returned (via OrderCancelledEvent) on timeout.
        for (OrderItem item : saved.getItems()) {
            try {
                productClient.adjustStock(
                        item.getProductId(),
                        new ProductServiceClient.StockAdjustRequest(
                                item.getQuantity(),
                                "RESERVATION",
                                "Order placed — awaiting payment: " + orderNumber,
                                orderNumber),
                        "Bearer " + token);

                log.info("Stock RESERVATION for product {} (order {})",
                        item.getProductId(), orderNumber);

            } catch (Exception ex) {
                log.warn("Stock reservation failed for product {} (order {}): {}",
                        item.getProductId(), orderNumber, ex.getMessage());
            }
        }

        // ── Step 3: Build item info for RabbitMQ events ───────────────────────
        List<OrderItemInfo> itemInfos = saved.getItems().stream()
                .map(i -> new OrderItemInfo(
                        i.getProductId(),
                        i.getProductSku(),
                        i.getQuantity(),
                        i.getUnitPrice()))
                .toList();

        // ── Step 4: Publish OrderPlacedEvent (payment-service picks this up) ──
        eventPublisher.publishOrderPlaced(OrderPlacedEvent.builder()
                .orderId(saved.getId())
                .orderNumber(saved.getOrderNumber())
                .userId(userId)
                .userEmail(userEmail)
                .totalAmount(totalAmount)
                .items(itemInfos == null ? List.of() : itemInfos)
                .placedAt(saved.getCreatedAt())
                .build());

        // ── Step 5: Notify customer that order was received ────────────────────
        emailService.sendOrderPlaced(userEmail, saved.getOrderNumber(), saved.getTotalAmount());

        return orderMapper.toDto(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Read (Customer)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long orderId, Long userId) {
        return orderMapper.toDto(findOrderForUser(orderId, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderByNumber(String orderNumber, Long userId) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderNumber));
        assertOwnership(order, userId);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderSummaryDto> getOrderHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderSummaryDto> result = orderRepository
                .findOrderHistory(userId, pageable)
                .map(orderMapper::toSummaryDto);
        return PagedResponse.from(result);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cancel Order
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId, CancelOrderRequest req) {
        Order order = findOrderForUser(orderId, userId);

        if (!order.isCancellable()) {
            throw new OrderNotCancellableException(
                    "Order " + order.getOrderNumber()
                            + " cannot be cancelled in status: " + order.getStatus());
        }

        if (!SecurityUtil.isAdmin()
                && !order.isWithinCancellationWindow(props.getCancellationWindowMinutes())) {
            throw new OrderNotCancellableException(
                    "Cancellation window has expired. Orders can only be cancelled within "
                            + props.getCancellationWindowMinutes() + " minutes of placing.");
        }

        transitionStatus(order, OrderStatus.CANCELLED,
                "Cancelled by customer: " + req.getReason(), userId, "CUSTOMER");
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(req.getReason());

        orderRepository.save(order);
        log.info("Order cancelled: {} by user {}", order.getOrderNumber(), userId);

        // ── Build items list for stock-return event ────────────────────────────
        List<OrderItemInfo> itemInfos = order.getItems().stream()
                .map(i -> new OrderItemInfo(
                        i.getProductId(),
                        i.getProductSku(),
                        i.getQuantity(),
                        i.getUnitPrice()))
                .toList();

        // ── Publish OrderCancelledEvent ────────────────────────────────────────
        // product-service's OrderEventConsumer receives this and calls STOCK_IN
        // to return the reserved stock.
        eventPublisher.publishOrderCancelled(OrderCancelledEvent.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(userId)
                .userEmail(order.getUserEmail())
                .reason(req.getReason())
                .items(itemInfos)
                .cancelledAt(order.getCancelledAt())
                .build());

        // ── Send cancellation email (async, last) ─────────────────────────────
        emailService.sendOrderCancelled(
                order.getUserEmail(),
                order.getOrderNumber(),
                req.getReason(),
                false);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Admin operations
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderSummaryDto> getAllOrders(int page, int size,
                                                       String sortBy, String sortDir,
                                                       OrderStatus status) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderSummaryDto> result = (status != null
                ? orderRepository.findAllByStatus(status, pageable)
                : orderRepository.findAll(pageable))
                .map(orderMapper::toSummaryDto);

        return PagedResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderByIdAdmin(Long orderId) {
        return orderMapper.toDto(orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId)));
    }

    @Override
    @Transactional
    public OrderDto updateOrderStatus(Long orderId, UpdateOrderStatusRequest req, Long adminId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        validateStatusTransition(order.getStatus(), req.getStatus());

        if (req.getTrackingNumber() != null) {
            order.setTrackingNumber(req.getTrackingNumber());
        }
        if (req.getStatus() == OrderStatus.SHIPPED) {
            order.setShippedAt(LocalDateTime.now());
        }
        if (req.getStatus() == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }
        if (req.getStatus() == OrderStatus.COMPLETED) {
            order.setDeliveredAt(order.getDeliveredAt() != null
                    ? order.getDeliveredAt()
                    : LocalDateTime.now());
        }

        transitionStatus(order, req.getStatus(), req.getNote(), adminId, "ADMIN");
        Order saved = orderRepository.save(order);

        log.info("Order {} status updated to {} by admin {}",
                order.getOrderNumber(), req.getStatus(), adminId);

        // ── Publish events based on new status ────────────────────────────────
        if (req.getStatus() == OrderStatus.COMPLETED) {
            eventPublisher.publishOrderCompleted(OrderCompletedEvent.builder()
                    .orderId(saved.getId())
                    .orderNumber(saved.getOrderNumber())
                    .userId(saved.getUserId())
                    .totalAmount(saved.getTotalAmount())
                    .completedAt(LocalDateTime.now())
                    .build());
        }

        // ── Send status-based emails ───────────────────────────────────────────
        if (req.getStatus() == OrderStatus.SHIPPED) {
            emailService.sendOrderShipped(
                    order.getUserEmail(),
                    order.getOrderNumber(),
                    req.getTrackingNumber());
        }

        return orderMapper.toDto(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scheduled: auto-expire unpaid orders (payment-service should confirm
    // within the timeout window by publishing an event back)
    // ─────────────────────────────────────────────────────────────────────────

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expireTimedOutOrders() {
        LocalDateTime cutoff = LocalDateTime.now()
                .minusMinutes(props.getPaymentTimeoutMinutes());

        List<Order> timedOut = orderRepository.findTimedOutPaymentOrders(cutoff);

        if (timedOut.isEmpty()) return;

        log.info("Found {} unpaid order(s) to expire", timedOut.size());

        timedOut.forEach(order -> {
            log.warn("Expiring order {} — payment not received within {} minutes",
                    order.getOrderNumber(), props.getPaymentTimeoutMinutes());

            transitionStatus(order, OrderStatus.CANCELLED,
                    "Auto-cancelled: payment timeout", null, "SYSTEM");
            order.setCancelledAt(LocalDateTime.now());
            order.setCancellationReason("Payment not received within allowed time");
            orderRepository.save(order);

            List<OrderItemInfo> itemInfos = order.getItems().stream()
                    .map(i -> new OrderItemInfo(
                            i.getProductId(),
                            i.getProductSku(),
                            i.getQuantity(),
                            i.getUnitPrice()))
                    .toList();

            eventPublisher.publishOrderCancelled(OrderCancelledEvent.builder()
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .userId(order.getUserId())
                    .userEmail(order.getUserEmail())
                    .reason("Payment timeout — order auto-cancelled")
                    .items(itemInfos)
                    .cancelledAt(order.getCancelledAt())
                    .build());

            emailService.sendOrderCancelled(
                    order.getUserEmail(),
                    order.getOrderNumber(),
                    "Payment was not received within the allowed time.",
                    false);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private Order findOrderForUser(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        assertOwnership(order, userId);
        return order;
    }

    private void assertOwnership(Order order, Long userId) {
        if (!order.getUserId().equals(userId) && !SecurityUtil.isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have access to order: " + order.getOrderNumber());
        }
    }

    private void transitionStatus(Order order, OrderStatus to,
                                  String note, Long changedBy, String role) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .fromStatus(order.getStatus())
                .toStatus(to)
                .note(note)
                .changedBy(changedBy)
                .changedByRole(role)
                .build();
        order.setStatus(to);
        order.addStatusHistory(history);
    }

    private void validateStatusTransition(OrderStatus from, OrderStatus to) {
        boolean valid = switch (from) {
            case PENDING          -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED        -> to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED;
            case PROCESSING       -> to == OrderStatus.SHIPPED   || to == OrderStatus.CANCELLED;
            case SHIPPED          -> to == OrderStatus.DELIVERED;
            case DELIVERED        -> to == OrderStatus.COMPLETED || to == OrderStatus.REFUND_REQUESTED;
            case REFUND_REQUESTED -> to == OrderStatus.REFUNDED  || to == OrderStatus.COMPLETED;
            default               -> false;
        };
        if (!valid) {
            throw new InvalidOrderStateException(
                    "Cannot transition order from " + from + " to " + to);
        }
    }

    private String generateUniqueOrderNumber() {
        String num;
        int attempts = 0;
        do {
            num = OrderNumberGenerator.generate();
            attempts++;
        } while (orderRepository.existsByOrderNumber(num) && attempts < 10);
        return num;
    }

    @Override
    @Transactional
    public void confirmPayment(
            Long orderId,
            String paymentReference,
            String paymentStatus
    ) {

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found: " + orderId));


        order.setPaymentReference(paymentReference);
        order.setPaymentStatus(paymentStatus);

        if ("SUCCESS".equalsIgnoreCase(paymentStatus)) {

            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);

            // ✅ BUILD ITEMS HERE
            List<OrderItemInfo> itemInfos =
                    order.getItems().stream()
                            .map(item ->
                                    OrderItemInfo.builder()
                                            .productId(item.getProductId())
                                            .productSku(item.getProductSku())
                                            .quantity(item.getQuantity())
                                            .unitPrice(item.getUnitPrice())
                                            .build()
                            )
                            .toList();

            // 🔥 PUBLISH EVENT
            eventPublisher.publishOrderConfirmed(
                    OrderConfirmedEvent.builder()
                            .orderId(order.getId())
                            .orderNumber(order.getOrderNumber())
                            .userId(order.getUserId())
                            .userEmail(order.getUserEmail())
                            .userName(order.getUserName())
                            .totalAmount(order.getTotalAmount())
                            .items(itemInfos)
                            .confirmedAt(LocalDateTime.now())
                            .build()
            );

        } else if ("FAILED".equalsIgnoreCase(paymentStatus)) {

            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        }
    }
}
