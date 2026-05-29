package com.microservice.paymentservice.repository;

import com.microservice.paymentservice.entity.Refund;
import com.microservice.paymentservice.entity.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findAllByPaymentId(Long paymentId);

    Optional<Refund> findByRefundReference(String refundReference);

    Page<Refund> findAllByStatus(RefundStatus status, Pageable pageable);

    @Query("SELECT SUM(r.amount) FROM Refund r WHERE r.payment.id = :paymentId AND r.status = 'SUCCESS'")
    java.math.BigDecimal sumSuccessfulRefundsByPaymentId(@Param("paymentId") Long paymentId);
}
