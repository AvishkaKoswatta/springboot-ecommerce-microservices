package com.microservice.orderservice.mapper;

import com.microservice.orderservice.dto.OrderDtos.*;
import com.microservice.orderservice.entity.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrderMapper {

    @Mapping(target = "totalItemCount", expression = "java(order.getTotalItemCount())")
    OrderDto toDto(Order order);

    @Mapping(target = "totalItemCount", expression = "java(order.getTotalItemCount())")
    OrderSummaryDto toSummaryDto(Order order);

    List<OrderSummaryDto> toSummaryDtoList(List<Order> orders);

    OrderItemDto toItemDto(OrderItem item);

    OrderStatusHistoryDto toHistoryDto(OrderStatusHistory history);

    @Mapping(target = "recipientName", source = "recipientName")
    ShippingAddressDto toAddressDto(ShippingAddress address);
}
