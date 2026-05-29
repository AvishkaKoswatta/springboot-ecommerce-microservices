package com.microservice.paymentservice.repository;

import com.microservice.paymentservice.entity.WebhookDeliveryStatus;
import com.microservice.paymentservice.entity.WebhookEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    Optional<WebhookEvent> findByEventId(String eventId);

    boolean existsByEventId(String eventId);

    Page<WebhookEvent> findAllByOrderId(Long orderId, Pageable pageable);

    Page<WebhookEvent> findAllByPaymentId(Long paymentId, Pageable pageable);

    @Query("SELECT w FROM WebhookEvent w WHERE w.deliveryStatus IN ('PENDING','RETRYING') AND w.retryCount < 3")
    List<WebhookEvent> findPendingRetries();

    Page<WebhookEvent> findAllByDeliveryStatus(WebhookDeliveryStatus status, Pageable pageable);
}
