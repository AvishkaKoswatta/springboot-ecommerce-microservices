package com.microservice.productservice.repository;

import com.microservice.productservice.entity.Review;
import com.microservice.productservice.entity.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findAllByProductIdAndStatus(Long productId, ReviewStatus status, Pageable pageable);

    Page<Review> findAllByProductId(Long productId, Pageable pageable);

    Optional<Review> findByProductIdAndUserId(Long productId, Long userId);

    boolean existsByProductIdAndUserId(Long productId, Long userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.status = 'APPROVED'")
    Double getAverageRating(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.status = 'APPROVED'")
    int getApprovedReviewCount(@Param("productId") Long productId);

    @Query("""
            SELECT r.rating, COUNT(r) FROM Review r
            WHERE r.product.id = :productId AND r.status = 'APPROVED'
            GROUP BY r.rating ORDER BY r.rating DESC
            """)
    java.util.List<Object[]> getRatingDistribution(@Param("productId") Long productId);

    Page<Review> findAllByStatus(ReviewStatus status, Pageable pageable);
}
