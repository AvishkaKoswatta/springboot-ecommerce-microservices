package com.microservice.productservice.mapper;

import com.microservice.productservice.dto.CategoryDtos.*;
import com.microservice.productservice.entity.Category;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CategoryMapper {

    @Mapping(target = "parentId",   source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "productCount", expression = "java(category.getProducts().size())")
    @Mapping(target = "children",   source = "children")
    CategoryDto toDto(Category category);

    List<CategoryDto> toDtoList(List<Category> categories);
}
