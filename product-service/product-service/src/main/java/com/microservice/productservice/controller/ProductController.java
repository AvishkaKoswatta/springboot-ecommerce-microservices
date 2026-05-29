package com.microservice.productservice.controller;

import com.microservice.productservice.dto.ProductDtos.*;
import com.microservice.productservice.dto.ProductSearchRequest;
import com.microservice.productservice.response.ApiResponse;
import com.microservice.productservice.response.PagedResponse;
import com.microservice.productservice.service.ProductService;
import com.microservice.productservice.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;



import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ─── Public read endpoints ──────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProductDto>>> getAllProducts(
            @RequestParam(defaultValue = "0")          int page,
            @RequestParam(defaultValue = "20")         int size,
            @RequestParam(defaultValue = "createdAt")  String sortBy,
            @RequestParam(defaultValue = "desc")       String sortDir) {
        return ResponseEntity.ok(ApiResponse.success("Products retrieved",
                productService.getAllProducts(page, size, sortBy, sortDir)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<ProductDto>>> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "0")         int page,
            @RequestParam(defaultValue = "20")        int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")      String sortDir) {

        List<Long> resolvedCategoryIds = (categoryIds != null && !categoryIds.isEmpty())
                ? categoryIds
                : (categoryId != null ? List.of(categoryId) : null);

        ProductSearchRequest req = ProductSearchRequest.builder()
                .query(query).categoryId(categoryId).categoryIds(resolvedCategoryIds).brand(brand)
                .minPrice(minPrice).maxPrice(maxPrice)
                .featured(featured).inStock(inStock)
                .page(page).size(size).sortBy(sortBy).sortDir(sortDir)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Search results", productService.searchProducts(req)));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getFeaturedProducts() {
        return ResponseEntity.ok(ApiResponse.success("Featured products", productService.getFeaturedProducts()));
    }

    @GetMapping("/brands")
    public ResponseEntity<ApiResponse<List<String>>> getAllBrands() {
        return ResponseEntity.ok(ApiResponse.success("Brands retrieved", productService.getAllBrands()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Product retrieved", productService.getProductById(id)));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success("Product retrieved", productService.getProductBySlug(slug)));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductBySku(@PathVariable String sku) {
        return ResponseEntity.ok(ApiResponse.success("Product retrieved", productService.getProductBySku(sku)));
    }

    // ─── Admin write endpoints ──────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        // Extract admin user ID from JWT principal (email stored; use a fixed sentinel if ID not in token)
        // In production, embed userId claim in the JWT from user-service.
        Long adminId = 0L; // placeholder — replace with claim extraction
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Product created", productService.createProduct(request, adminId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Product updated", productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id, 0L);
        return ResponseEntity.ok(ApiResponse.success("Product deleted"));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<ProductDto>>> searchProductsPost(
            @RequestBody ProductSearchRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Search results", productService.searchProducts(req)));
    }
}
