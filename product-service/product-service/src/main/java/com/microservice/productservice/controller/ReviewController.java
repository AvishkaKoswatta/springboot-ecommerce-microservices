package com.microservice.productservice.controller;

import com.microservice.productservice.dto.ReviewDtos.*;
import com.microservice.productservice.response.ApiResponse;
import com.microservice.productservice.response.PagedResponse;
import com.microservice.productservice.security.CustomPrincipal;
import com.microservice.productservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // ─── Public read ────────────────────────────────────────────────────────

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewDto>>> getApprovedReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved",
                reviewService.getApprovedReviews(productId, page, size)));
    }

    @GetMapping("/products/{productId}/reviews/summary")
    public ResponseEntity<ApiResponse<ReviewSummaryDto>> getReviewSummary(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success("Review summary retrieved",
                reviewService.getReviewSummary(productId)));
    }

    // ─── Authenticated users ────────────────────────────────────────────────

    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<ReviewDto>> createReview(
            @PathVariable Long productId,
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal CustomPrincipal principal) {

        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }

        Long userId = principal.userId();
        String username = principal.username();

        ReviewDto dto = reviewService.createReview(productId, request, userId, username);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Review submitted for moderation", dto));
    }
    // ─── Admin ───────────────────────────────────────────────────────────────

    @GetMapping("/products/{productId}/reviews/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewDto>>> getAllReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("All reviews retrieved",
                reviewService.getAllReviews(productId, page, size)));
    }

    @GetMapping("/reviews/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewDto>>> getPendingReviews(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Pending reviews retrieved",
                reviewService.getPendingReviews(page, size)));
    }

    @PatchMapping("/reviews/{reviewId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReviewDto>> updateReviewStatus(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Review status updated",
                reviewService.updateReviewStatus(reviewId, request)));
    }

    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted"));
    }


}
