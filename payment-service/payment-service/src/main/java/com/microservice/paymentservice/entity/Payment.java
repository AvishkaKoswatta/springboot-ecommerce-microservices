package com.microservice.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_order",     columnList = "order_id"),
        @Index(name = "idx_payment_ref",       columnList = "payment_reference", unique = true),
        @Index(name = "idx_payment_status",    columnList = "status"),
        @Index(name = "idx_payment_user",      columnList = "user_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ── Order reference ── */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_number", nullable = false, length = 40)
    private String orderNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_email", nullable = false, length = 100)
    private String userEmail;

    /* ── Payment identity ── */
    @Column(name = "payment_reference", nullable = false, unique = true, length = 80)
    private String paymentReference;

    @Column(name = "gateway_transaction_id", length = 120)
    private String gatewayTransactionId;

    /* ── Amount ── */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    /* ── Method & Status ── */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.INITIATED;

    /* ── Gateway response ── */
    @Column(name = "gateway_response_code", length = 20)
    private String gatewayResponseCode;

    @Column(name = "gateway_response_message", length = 300)
    private String gatewayResponseMessage;

    @Column(name = "gateway_raw_response", columnDefinition = "TEXT")
    private String gatewayRawResponse;

    /* ── Timestamps ── */
    @Column(name = "initiated_at")
    private LocalDateTime initiatedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /* ── Refund tracking ── */
    @Column(name = "refunded_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Refund> refunds = new ArrayList<>();

    /* ── Webhook ── */
    @Column(name = "webhook_delivered")
    @Builder.Default
    private Boolean webhookDelivered = false;

    @Column(name = "webhook_delivered_at")
    private LocalDateTime webhookDeliveredAt;

    /* ── Audit ── */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /* ── Helpers ── */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean canBeRefunded() {
        return status == PaymentStatus.SUCCESS
                && refundedAmount.compareTo(amount) < 0;
    }

    public BigDecimal getRefundableAmount() {
        return amount.subtract(refundedAmount);
    }
}
