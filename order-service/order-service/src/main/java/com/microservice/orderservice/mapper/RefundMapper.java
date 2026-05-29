package com.microservice.orderservice.mapper;

import com.microservice.orderservice.dto.RefundDtos.*;
import com.microservice.orderservice.entity.RefundRequest;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RefundMapper {

    @Mapping(target = "orderId",     source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    RefundDto toDto(RefundRequest refundRequest);
}
