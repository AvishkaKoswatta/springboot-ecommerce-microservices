package com.microservice.orderservice.dto;

import com.microservice.orderservice.entity.RefundReasonType;
import com.microservice.orderservice.entity.RefundStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RefundDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RefundRequestDto {
        @NotNull private RefundReasonType reasonType;
        @Size(max = 500) private String reasonDetail;
        @NotNull @DecimalMin("0.01") private BigDecimal requestedAmount;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ResolveRefundRequest {
        @NotNull private RefundStatus status;
        private BigDecimal approvedAmount;
        @Size(max = 500) private String adminNotes;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RefundDto {
        private Long id;
        private Long orderId;
        private String orderNumber;
        private RefundStatus status;
        private RefundReasonType reasonType;
        private String reasonDetail;
        private BigDecimal requestedAmount;
        private BigDecimal approvedAmount;
        private String adminNotes;
        private LocalDateTime resolvedAt;
        private LocalDateTime createdAt;
    }
}
