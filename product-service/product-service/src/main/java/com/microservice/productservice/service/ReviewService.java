package com.microservice.productservice.service;

import com.microservice.productservice.dto.ReviewDtos.*;
import com.microservice.productservice.response.PagedResponse;

public interface ReviewService {
    ReviewDto createReview(Long productId, CreateReviewRequest request, Long userId, String username);
    ReviewDto updateReviewStatus(Long reviewId, UpdateReviewStatusRequest request);
    void deleteReview(Long reviewId);
    PagedResponse<ReviewDto> getApprovedReviews(Long productId, int page, int size);
    PagedResponse<ReviewDto> getAllReviews(Long productId, int page, int size);
    PagedResponse<ReviewDto> getPendingReviews(int page, int size);
    ReviewSummaryDto getReviewSummary(Long productId);
}
