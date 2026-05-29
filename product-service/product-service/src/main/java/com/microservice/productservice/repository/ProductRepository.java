package com.microservice.productservice.repository;

import com.microservice.productservice.entity.Product;
import com.microservice.productservice.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySku(String sku);

    boolean existsBySlug(String slug); //validation before insert. prevent duplicate

    boolean existsBySku(String sku);

    Page<Product> findAllByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findAllByCategoryId(Long categoryId, Pageable pageable);

    Page<Product> findAllByCategoryIdAndStatus(Long categoryId, ProductStatus status, Pageable pageable);

    List<Product> findAllByFeaturedTrueAndStatus(ProductStatus status);

    //searchProducts
    @Query("""
            SELECT p FROM Product p
            WHERE p.status = 'ACTIVE'
            AND (
                LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(p.tags) LIKE LOWER(CONCAT('%', :query, '%'))
            )
            """)
    Page<Product> searchProducts(@Param("query") String query, Pageable pageable);

    //filterProducts
    @Query("""
            SELECT p FROM Product p
            WHERE p.status = 'ACTIVE'
            AND (:categoryId IS NULL OR p.category.id = :categoryId)
            AND (:brand IS NULL OR LOWER(p.brand) = LOWER(:brand))
            AND (:minPrice IS NULL OR p.price >= :minPrice)
            AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            """)
    Page<Product> filterProducts(
            @Param("categoryId") Long categoryId,
            @Param("brand")      String brand,
            @Param("minPrice")   BigDecimal minPrice,
            @Param("maxPrice")   BigDecimal maxPrice,
            Pageable pageable
    );

    //find products running out of stock
    @Query("SELECT p FROM Product p WHERE p.trackInventory = true AND (p.stockQuantity - p.reservedQuantity) <= p.lowStockThreshold AND p.status = 'ACTIVE'")
    List<Product> findLowStockProducts();

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :qty WHERE p.id = :id")
    void incrementStock(@Param("id") Long id, @Param("qty") int qty);

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :qty WHERE p.id = :id AND p.stockQuantity >= :qty")
    int decrementStock(@Param("id") Long id, @Param("qty") int qty);

    @Modifying
    @Query("UPDATE Product p SET p.reservedQuantity = p.reservedQuantity + :qty WHERE p.id = :id")
    void incrementReserved(@Param("id") Long id, @Param("qty") int qty);

    @Modifying
    @Query("UPDATE Product p SET p.reservedQuantity = p.reservedQuantity - :qty WHERE p.id = :id AND p.reservedQuantity >= :qty")
    int decrementReserved(@Param("id") Long id, @Param("qty") int qty);

    @Modifying
    @Query("UPDATE Product p SET p.averageRating = :rating, p.reviewCount = :count WHERE p.id = :id")
    void updateRatingStats(@Param("id") Long id, @Param("rating") BigDecimal rating, @Param("count") int count);

    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.brand IS NOT NULL AND p.status = 'ACTIVE' ORDER BY p.brand")
    List<String> findAllActiveBrands();
}
