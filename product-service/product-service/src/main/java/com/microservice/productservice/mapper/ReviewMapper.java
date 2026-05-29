package com.microservice.productservice.mapper;

import com.microservice.productservice.dto.ReviewDtos.*;
import com.microservice.productservice.entity.Review;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ReviewMapper {

    @Mapping(target = "productId", source = "product.id")
    ReviewDto toDto(Review review);

    List<ReviewDto> toDtoList(List<Review> reviews);
}
