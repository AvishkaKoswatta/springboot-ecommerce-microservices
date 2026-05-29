package com.microservice.productservice.mapper;

import com.microservice.productservice.dto.ProductDtos.*;
import com.microservice.productservice.entity.Product;
import com.microservice.productservice.entity.ProductImage;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProductMapper {

    @Mapping(target = "categoryId",   source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "inStock",      expression = "java(product.isInStock())")
    @Mapping(target = "lowStock",     expression = "java(product.isLowStock())")
    @Mapping(target = "availableQuantity", expression = "java(product.getAvailableQuantity())")
    ProductDto toDto(Product product);

    List<ProductDto> toDtoList(List<Product> products);

    @Mapping(target = "primary", source = "primary")
    ProductImageDto toImageDto(ProductImage image);
}
