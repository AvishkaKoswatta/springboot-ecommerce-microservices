package com.microservice.orderservice.repository;

import com.microservice.orderservice.entity.Order;
import com.microservice.orderservice.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findAllByUserId(Long userId, Pageable pageable);

    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findAllByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    boolean existsByOrderNumber(String orderNumber);

    @Query("""
            SELECT o FROM Order o
            WHERE o.userId = :userId
            ORDER BY o.createdAt DESC
            """)
    Page<Order> findOrderHistory(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT o FROM Order o
            WHERE o.status = 'PENDING'
            AND o.createdAt < :cutoff
            """)
    List<Order> findTimedOutPaymentOrders(@Param("cutoff") LocalDateTime cutoff);

    @Query("""
            SELECT o FROM Order o
            WHERE o.status IN ('PENDING','CONFIRMED')
            AND o.userId = :userId
            """)
    List<Order> findActiveOrdersByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") OrderStatus status);

    @Query("""
            SELECT o FROM Order o
            WHERE o.createdAt BETWEEN :from AND :to
            ORDER BY o.createdAt DESC
            """)
    List<Order> findOrdersBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
    SELECT DISTINCT o FROM Order o
    JOIN o.items i
    WHERE i.productId = :productId
    AND o.status IN ('PENDING', 'CONFIRMED', 'PROCESSING')
    """)
    List<Order> findActiveOrdersContainingProduct(
            @Param("productId") Long productId);

    @Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
}
