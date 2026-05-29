package com.microservice.paymentservice.mapper;

import com.microservice.paymentservice.dto.PaymentDtos.*;
import com.microservice.paymentservice.entity.Payment;
import com.microservice.paymentservice.entity.Refund;
import com.microservice.paymentservice.entity.WebhookEvent;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PaymentMapper {

    @Mapping(target = "refundableAmount", expression = "java(payment.getRefundableAmount())")
    PaymentDto toDto(Payment payment);

    PaymentSummaryDto toSummaryDto(Payment payment);

    @Mapping(target = "paymentId", source = "payment.id")
    @Mapping(target = "paymentReference", source = "payment.paymentReference")
    RefundDto toRefundDto(Refund refund);

    List<RefundDto> toRefundDtoList(List<Refund> refunds);

    WebhookEventDto toWebhookDto(WebhookEvent event);

    List<WebhookEventDto> toWebhookDtoList(List<WebhookEvent> events);
}
