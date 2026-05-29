package com.microservice.productservice.util;

import com.microservice.productservice.dto.ProductSearchRequest;
import com.microservice.productservice.entity.Product;
import com.microservice.productservice.entity.ProductStatus;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    //"build WHERE clause dynamically"
    private ProductSpecification() {}

    public static Specification<Product> fromSearchRequest(ProductSearchRequest req) {
        return (Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>(); //all WHERE conditions
//root	- Product table
//cb - SQL builder
//query - overall query


            // Full-text search across name, description, brand, tags

            //WHERE
            //name LIKE '%iphone%'
            //OR description LIKE '%iphone%'
            //OR brand LIKE '%iphone%'
            //OR tags LIKE '%iphone%'

            if (req.getQuery() != null && !req.getQuery().isBlank()) {
                String pattern = "%" + req.getQuery().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")),        pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("brand")),       pattern),
                        cb.like(cb.lower(root.get("tags")),        pattern)
                ));
            }

            // Category filter
            // Category filter — prefer categoryIds list (parent + children), fall back to single
            if (req.getCategoryIds() != null && !req.getCategoryIds().isEmpty()) {
                predicates.add(root.get("category").get("id").in(req.getCategoryIds()));
            } else if (req.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), req.getCategoryId()));
            }

            // Brand filter
            if (req.getBrand() != null && !req.getBrand().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("brand")),
                        req.getBrand().toLowerCase()));
            }

            // Price range
            if (req.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), req.getMinPrice()));
            }
            if (req.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), req.getMaxPrice()));
            }

            // Status — default to ACTIVE for public searches
            if (req.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), req.getStatus()));
            } else {
                predicates.add(cb.equal(root.get("status"), ProductStatus.ACTIVE));
            }

            // Featured flag
            if (req.getFeatured() != null) {
                predicates.add(cb.equal(root.get("featured"), req.getFeatured()));
            }

            // In-stock filter
            if (Boolean.TRUE.equals(req.getInStock())) {
                predicates.add(cb.greaterThan(
                        cb.diff(root.get("stockQuantity"), root.get("reservedQuantity")),
                        0));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

//iphone, category=1, featured=true

//WHERE
//(
// name LIKE '%iphone%'
// OR description LIKE '%iphone%'
//)
//AND category_id = 1
//AND featured = true
//AND status = 'ACTIVE'