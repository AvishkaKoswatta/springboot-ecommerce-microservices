package com.microservice.orderservice.repository;

import com.microservice.orderservice.entity.RefundRequest;
import com.microservice.orderservice.entity.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {
    Optional<RefundRequest> findByOrderId(Long orderId);
    boolean existsByOrderId(Long orderId);
    Page<RefundRequest> findAllByStatus(RefundStatus status, Pageable pageable);
}
