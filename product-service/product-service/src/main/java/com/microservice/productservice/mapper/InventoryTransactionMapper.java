package com.microservice.productservice.mapper;

import com.microservice.productservice.dto.InventoryDtos.*;
import com.microservice.productservice.entity.InventoryTransaction;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface InventoryTransactionMapper {

    @Mapping(target = "productId", source = "product.id")
    InventoryTransactionDto toDto(InventoryTransaction transaction);

    List<InventoryTransactionDto> toDtoList(List<InventoryTransaction> transactions);
}
