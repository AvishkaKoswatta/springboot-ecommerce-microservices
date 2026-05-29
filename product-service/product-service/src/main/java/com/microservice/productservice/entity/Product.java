package com.microservice.productservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_slug",      columnList = "slug",      unique = true),
        @Index(name = "idx_product_sku",       columnList = "sku",       unique = true),
        @Index(name = "idx_product_category",  columnList = "category_id"),
        @Index(name = "idx_product_status",    columnList = "status"),
        @Index(name = "idx_product_created_by",columnList = "created_by")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 220) //SEO-friendly URL text
    private String slug;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "compare_at_price", precision = 12, scale = 2) //Original price before discount
    private BigDecimal compareAtPrice;

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProductStatus status = ProductStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY) //Many products belong to one category
    @JoinColumn(name = "category_id") //foreign key to category table
    private Category category;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "weight_grams")
    private Integer weightGrams;

    @Column(name = "tags", length = 500)
    private String tags;

    @Column(name = "featured")
    @Builder.Default
    private Boolean featured = false;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    // Inventory (stored on product for simplicity — extracted to InventoryRecord for audit)
    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "reserved_quantity", nullable = false) //temporarily hold
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "low_stock_threshold")
    @Builder.Default
    private Integer lowStockThreshold = 10;

    @Column(name = "track_inventory")
    @Builder.Default
    private Boolean trackInventory = true;
    // trackInventory = false -> No stock tracking needed, like digital products. No out of stock, No physical

    @Column(name = "allow_backorder")
    @Builder.Default
    private Boolean allowBackorder = false; //customer can not buy if stock is 0

    // Relations
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) //One product → many images
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    // Denormalised averages — updated via @Formula alternative (maintained in service)
    @Column(name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Integer getAvailableQuantity() {
        return stockQuantity - reservedQuantity;
    }

    public boolean isInStock() {
        if (!trackInventory) return true; // if not care about stock(like digital products are unlimited)
        if (allowBackorder) return true; // if can still purchase even stock is 0
        return getAvailableQuantity() > 0;
    }


    public boolean isLowStock() {
        return trackInventory && getAvailableQuantity() <= lowStockThreshold;
    }
}
