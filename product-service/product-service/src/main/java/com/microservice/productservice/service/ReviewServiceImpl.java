package com.microservice.productservice.service;

import com.microservice.productservice.dto.ReviewDtos.*;
import com.microservice.productservice.entity.Review;
import com.microservice.productservice.entity.ReviewStatus;
import com.microservice.productservice.exception.ProductNotFoundException;
import com.microservice.productservice.exception.ReviewAlreadyExistsException;
import com.microservice.productservice.exception.ReviewNotFoundException;
import com.microservice.productservice.mapper.ReviewMapper;
import com.microservice.productservice.repository.ProductRepository;
import com.microservice.productservice.repository.ReviewRepository;
import com.microservice.productservice.response.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository  reviewRepository;
    private final ProductRepository productRepository;
    private final ReviewMapper      reviewMapper;

    @Override
    @Transactional
    public ReviewDto createReview(Long productId, CreateReviewRequest req, Long userId, String username) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (reviewRepository.existsByProductIdAndUserId(productId, userId)) {
            throw new ReviewAlreadyExistsException(
                    "You have already submitted a review for this product");
        }

        var product = productRepository.findById(productId).orElseThrow();

        Review review = Review.builder()
                .product(product)
                .userId(userId)
                .username(username)
                .rating(req.getRating())
                .title(req.getTitle())
                .body(req.getBody())
                .status(ReviewStatus.PENDING)
                .verifiedPurchase(false)
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review created for product {} by user {}", productId, userId);
        return reviewMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ReviewDto updateReviewStatus(Long reviewId, UpdateReviewStatusRequest req) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        review.setStatus(req.getStatus());
        Review saved = reviewRepository.save(review);

        // Recalculate rating averages when a review is approved or rejected
        if (req.getStatus() == ReviewStatus.APPROVED || req.getStatus() == ReviewStatus.REJECTED) {
            recalculateProductRating(review.getProduct().getId());
        }

        log.info("Review {} status updated to {}", reviewId, req.getStatus());
        return reviewMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
        Long productId = review.getProduct().getId();
        reviewRepository.delete(review);
        recalculateProductRating(productId);
        log.info("Review {} deleted", reviewId);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewDto> getApprovedReviews(Long productId, int page, int size) {
        productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReviewDto> result = reviewRepository
                .findAllByProductIdAndStatus(productId, ReviewStatus.APPROVED, pageable)
                .map(reviewMapper::toDto);
        return PagedResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewDto> getAllReviews(Long productId, int page, int size) {
        productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReviewDto> result = reviewRepository
                .findAllByProductId(productId, pageable)
                .map(reviewMapper::toDto);
        return PagedResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewDto> getPendingReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<ReviewDto> result = reviewRepository
                .findAllByStatus(ReviewStatus.PENDING, pageable)
                .map(reviewMapper::toDto);
        return PagedResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewSummaryDto getReviewSummary(Long productId) {
        productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));

        Double avg = reviewRepository.getAverageRating(productId);
        int count  = reviewRepository.getApprovedReviewCount(productId);

        List<Object[]> dist = reviewRepository.getRatingDistribution(productId);
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) distribution.put(i, 0L);
        dist.forEach(row -> distribution.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue()));

        return ReviewSummaryDto.builder()
                .averageRating(avg != null
                        ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO)
                .totalReviews(count)
                .ratingDistribution(distribution)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void recalculateProductRating(Long productId) {
        Double avg = reviewRepository.getAverageRating(productId);
        int count  = reviewRepository.getApprovedReviewCount(productId);
        BigDecimal rating = avg != null
                ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        productRepository.updateRatingStats(productId, rating, count);
        log.debug("Rating recalculated for product {}: avg={}, count={}", productId, rating, count);
    }
}
