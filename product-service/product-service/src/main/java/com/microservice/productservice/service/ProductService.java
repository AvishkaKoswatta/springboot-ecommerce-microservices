package com.microservice.productservice.service;

import com.microservice.productservice.dto.ProductDtos.*;
import com.microservice.productservice.dto.ProductSearchRequest;
import com.microservice.productservice.response.PagedResponse;

import java.util.List;

public interface ProductService {
    ProductDto createProduct(CreateProductRequest request, Long adminUserId);
    ProductDto updateProduct(Long id, UpdateProductRequest request);
    void deleteProduct(Long id, Long deletedBy);
    ProductDto getProductById(Long id);
    ProductDto getProductBySlug(String slug);
    ProductDto getProductBySku(String sku);
    PagedResponse<ProductDto> getAllProducts(int page, int size, String sortBy, String sortDir);
    PagedResponse<ProductDto> searchProducts(ProductSearchRequest request);
    List<ProductDto> getFeaturedProducts();
    List<String> getAllBrands();
}
