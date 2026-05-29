package com.microservice.productservice.service;

import com.microservice.productservice.client.UserServiceClient;
import com.microservice.productservice.dto.ProductDtos.*;
import com.microservice.productservice.entity.*;
import com.microservice.productservice.event.ProductEventPublisher;
import com.microservice.productservice.exception.DuplicateResourceException;
import com.microservice.productservice.exception.ProductNotFoundException;
import com.microservice.productservice.mapper.ProductMapper;
import com.microservice.productservice.repository.CategoryRepository;
import com.microservice.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository     productRepository;
    @Mock private CategoryRepository    categoryRepository;
    @Mock private ProductMapper         productMapper;
    @Mock private ProductEventPublisher eventPublisher;
    @Mock private UserServiceClient     userServiceClient;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private ProductDto testProductDto;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L).name("Electronics").slug("electronics").build();

        testProduct = Product.builder()
                .id(1L).name("Test Product").slug("test-product").sku("SKU-001")
                .price(new BigDecimal("99.99")).status(ProductStatus.ACTIVE)
                .category(testCategory).stockQuantity(50).reservedQuantity(0)
                .lowStockThreshold(10).trackInventory(true).createdBy(1L)
                .build();

        testProductDto = ProductDto.builder()
                .id(1L).name("Test Product").slug("test-product").sku("SKU-001")
                .price(new BigDecimal("99.99")).status(ProductStatus.ACTIVE)
                .categoryId(1L).categoryName("Electronics").build();
    }

    @Test
    @DisplayName("createProduct() - should create and return product dto")
    void createProduct_success() {
        CreateProductRequest req = CreateProductRequest.builder()
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .categoryId(1L)
                .stockQuantity(50)
                .build();

        when(productRepository.existsBySlug(anyString())).thenReturn(false);
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);
        doNothing().when(eventPublisher).publishProductCreated(any());

        ProductDto result = productService.createProduct(req, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Product");
        verify(productRepository).save(any(Product.class));
        verify(eventPublisher).publishProductCreated(any());
    }

    @Test
    @DisplayName("createProduct() - should throw when SKU already exists")
    void createProduct_duplicateSku_throws() {
        CreateProductRequest req = CreateProductRequest.builder()
                .name("Test Product").sku("SKU-001")
                .price(new BigDecimal("99.99")).build();

        when(productRepository.existsBySlug(anyString())).thenReturn(false);
        when(productRepository.existsBySku("SKU-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(req, 1L))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("SKU");
    }

    @Test
    @DisplayName("getProductById() - should return dto when found")
    void getProductById_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);

        ProductDto result = productService.getProductById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getProductById() - should throw when not found")
    void getProductById_notFound_throws() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("updateProduct() - should update fields and return dto")
    void updateProduct_success() {
        UpdateProductRequest req = UpdateProductRequest.builder()
                .price(new BigDecimal("149.99"))
                .status(ProductStatus.ACTIVE)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);
        doNothing().when(eventPublisher).publishProductUpdated(any());

        ProductDto result = productService.updateProduct(1L, req);

        assertThat(result).isNotNull();
        verify(productRepository).save(testProduct);
        verify(eventPublisher).publishProductUpdated(any());
    }

    @Test
    @DisplayName("deleteProduct() - should delete and publish event")
    void deleteProduct_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepository).delete(testProduct);
        doNothing().when(eventPublisher).publishProductDeleted(any());

        assertThatCode(() -> productService.deleteProduct(1L, 1L)).doesNotThrowAnyException();
        verify(productRepository).delete(testProduct);
        verify(eventPublisher).publishProductDeleted(any());
    }

    @Test
    @DisplayName("getAllProducts() - should return paged response")
    void getAllProducts_returnsPaged() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
        Page<Product> page = new PageImpl<>(List.of(testProduct), pageable, 1);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);

        var result = productService.getAllProducts(0, 20, "createdAt", "desc");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
