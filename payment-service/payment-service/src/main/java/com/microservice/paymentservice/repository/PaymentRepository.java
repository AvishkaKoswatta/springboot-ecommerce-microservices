package com.microservice.paymentservice.repository;

import com.microservice.paymentservice.entity.Payment;
import com.microservice.paymentservice.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentReference(String paymentReference);

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByOrderNumber(String orderNumber);

    Page<Payment> findAllByUserId(Long userId, Pageable pageable);

    Page<Payment> findAllByStatus(PaymentStatus status, Pageable pageable);

    boolean existsByOrderId(Long orderId);

    @Query("SELECT p FROM Payment p WHERE p.status = 'INITIATED' AND p.expiresAt < :now")
    List<Payment> findExpiredPayments(@Param("now") LocalDateTime now);

    @Query("SELECT p FROM Payment p WHERE p.webhookDelivered = false AND p.status IN ('SUCCESS','FAILED')")
    List<Payment> findPendingWebhookDeliveries();

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    long countByStatus(@Param("status") PaymentStatus status);
}
