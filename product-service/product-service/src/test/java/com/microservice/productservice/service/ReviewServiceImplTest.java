package com.microservice.productservice.service;

import com.microservice.productservice.dto.ReviewDtos.*;
import com.microservice.productservice.entity.*;
import com.microservice.productservice.exception.ProductNotFoundException;
import com.microservice.productservice.exception.ReviewAlreadyExistsException;
import com.microservice.productservice.mapper.ReviewMapper;
import com.microservice.productservice.repository.ProductRepository;
import com.microservice.productservice.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock private ReviewRepository  reviewRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ReviewMapper      reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Product testProduct;
    private Review testReview;
    private ReviewDto testReviewDto;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L).name("Test Product").sku("SKU-001")
                .price(new BigDecimal("99.99")).status(ProductStatus.ACTIVE)
                .createdBy(1L).stockQuantity(10).build();

        testReview = Review.builder()
                .id(1L).product(testProduct).userId(2L).username("john@example.com")
                .rating(5).title("Great!").body("Loved it.")
                .status(ReviewStatus.PENDING).build();

        testReviewDto = ReviewDto.builder()
                .id(1L).productId(1L).userId(2L).rating(5)
                .title("Great!").status(ReviewStatus.PENDING).build();
    }

    @Test
    @DisplayName("createReview() - should save and return review dto")
    void createReview_success() {
        CreateReviewRequest req = new CreateReviewRequest(5, "Great!", "Loved it.");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(reviewRepository.existsByProductIdAndUserId(1L, 2L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(reviewMapper.toDto(testReview)).thenReturn(testReviewDto);

        ReviewDto result = reviewService.createReview(1L, req, 2L, "john@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(5);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("createReview() - should throw when user already reviewed")
    void createReview_alreadyExists_throws() {
        CreateReviewRequest req = new CreateReviewRequest(4, "Good", "Nice.");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(reviewRepository.existsByProductIdAndUserId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(1L, req, 2L, "john@example.com"))
                .isInstanceOf(ReviewAlreadyExistsException.class);
    }

    @Test
    @DisplayName("createReview() - should throw when product not found")
    void createReview_productNotFound_throws() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                reviewService.createReview(99L, new CreateReviewRequest(5, "x", "y"), 1L, "u"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("updateReviewStatus() - should approve review and recalculate rating")
    void updateReviewStatus_approve_success() {
        UpdateReviewStatusRequest req = new UpdateReviewStatusRequest(ReviewStatus.APPROVED);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any())).thenReturn(testReview);
        when(reviewMapper.toDto(testReview)).thenReturn(testReviewDto);
        when(reviewRepository.getAverageRating(1L)).thenReturn(5.0);
        when(reviewRepository.getApprovedReviewCount(1L)).thenReturn(1);
        doNothing().when(productRepository).updateRatingStats(anyLong(), any(), anyInt());

        ReviewDto result = reviewService.updateReviewStatus(1L, req);

        assertThat(result).isNotNull();
        verify(productRepository).updateRatingStats(eq(1L), any(BigDecimal.class), eq(1));
    }

    @Test
    @DisplayName("getReviewSummary() - should return summary with distribution")
    void getReviewSummary_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(reviewRepository.getAverageRating(1L)).thenReturn(4.5);
        when(reviewRepository.getApprovedReviewCount(1L)).thenReturn(10);
        when(reviewRepository.getRatingDistribution(1L)).thenReturn(
                List.of(new Object[]{5, 6L}, new Object[]{4, 4L}));

        ReviewSummaryDto summary = reviewService.getReviewSummary(1L);

        assertThat(summary.getTotalReviews()).isEqualTo(10);
        assertThat(summary.getAverageRating()).isEqualByComparingTo("4.50");
        assertThat(summary.getRatingDistribution().get(5)).isEqualTo(6L);
        assertThat(summary.getRatingDistribution().get(3)).isEqualTo(0L);
    }
}
