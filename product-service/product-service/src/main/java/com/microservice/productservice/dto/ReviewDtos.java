package com.microservice.productservice.dto;

import com.microservice.productservice.entity.ReviewStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class ReviewDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateReviewRequest {

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        private Integer rating;

        @Size(max = 200)
        private String title;

        @Size(max = 5000)
        private String body;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateReviewStatusRequest {
        @NotNull(message = "Status is required")
        private ReviewStatus status;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ReviewDto {
        private Long id;
        private Long productId;
        private Long userId;
        private String username;
        private Integer rating;
        private String title;
        private String body;
        private ReviewStatus status;
        private Integer helpfulCount;
        private Boolean verifiedPurchase;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ReviewSummaryDto {
        private BigDecimal averageRating;
        private int totalReviews;
        private Map<Integer, Long> ratingDistribution;
    }
}
