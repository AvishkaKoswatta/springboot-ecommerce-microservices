package com.microservice.productservice.service;

import com.microservice.productservice.client.UserServiceClient;
import com.microservice.productservice.dto.ProductDtos.*;
import com.microservice.productservice.dto.ProductSearchRequest;
import com.microservice.productservice.entity.*;
import com.microservice.productservice.event.ProductEventPublisher;
import com.microservice.productservice.event.ProductEvents.*;
import com.microservice.productservice.exception.CategoryNotFoundException;
import com.microservice.productservice.exception.DuplicateResourceException;
import com.microservice.productservice.exception.ProductNotFoundException;
import com.microservice.productservice.mapper.ProductMapper;
import com.microservice.productservice.repository.CategoryRepository;
import com.microservice.productservice.repository.ProductRepository;
import com.microservice.productservice.response.PagedResponse;
import com.microservice.productservice.util.ProductSpecification;
import com.microservice.productservice.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository      productRepository;
    private final CategoryRepository     categoryRepository;
    private final ProductMapper          productMapper;
    private final ProductEventPublisher  eventPublisher;
    private final UserServiceClient      userServiceClient;

    // ─────────────────────────────────────────────────────────────────────────
    // Create
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ProductDto createProduct(CreateProductRequest req, Long adminUserId) {
        String sku  = StringUtils.hasText(req.getSku()) ? req.getSku() : generateSku(); //If user already sends SKU use it Else generate automatically
        String slug = generateUniqueProductSlug(req.getName()); //Product name iPhone 15 Pro Max Slug becomes iphone-15-pro-max

        if (productRepository.existsBySku(sku)) {
            throw new DuplicateResourceException("Product already exists with SKU: " + sku);
        }

        Product.ProductBuilder builder = Product.builder()
                .name(req.getName())
                .slug(slug)
                .sku(sku)
                .description(req.getDescription())
                .shortDescription(req.getShortDescription())
                .price(req.getPrice())
                .compareAtPrice(req.getCompareAtPrice())
                .costPrice(req.getCostPrice())
                .status(req.getStatus() != null ? req.getStatus() : ProductStatus.DRAFT)
                .brand(req.getBrand())
                .weightGrams(req.getWeightGrams())
                .tags(req.getTags())
                .featured(req.getFeatured() != null ? req.getFeatured() : false)
                .createdBy(adminUserId)
                .stockQuantity(req.getStockQuantity() != null ? req.getStockQuantity() : 0)
                .lowStockThreshold(req.getLowStockThreshold() != null ? req.getLowStockThreshold() : 10)
                .trackInventory(req.getTrackInventory() != null ? req.getTrackInventory() : true)
                .allowBackorder(req.getAllowBackorder() != null ? req.getAllowBackorder() : false);

        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(req.getCategoryId()));
            builder.category(category);
        }

        Product product = builder.build(); //Still NOT saved in database. Only Java object currently

        // Attach images
        if (req.getImages() != null) {
            req.getImages().forEach(imgReq -> {
                ProductImage img = ProductImage.builder()
                        .product(product)
                        .url(imgReq.getUrl())
                        .altText(imgReq.getAltText())
                        .displayOrder(imgReq.getDisplayOrder() != null ? imgReq.getDisplayOrder() : 0)
                        .primary(imgReq.getPrimary() != null ? imgReq.getPrimary() : false)
                        .build();
                product.getImages().add(img);
            });
        }

        Product saved = productRepository.save(product);
        log.info("Product created: {} (SKU: {})", saved.getName(), saved.getSku());

        // Publish event
        eventPublisher.publishProductCreated(ProductCreatedEvent.builder()
                .productId(saved.getId())
                .name(saved.getName())
                .sku(saved.getSku())
                .price(saved.getPrice())
                .categoryId(saved.getCategory() != null ? saved.getCategory().getId() : null)
                .categoryName(saved.getCategory() != null ? saved.getCategory().getName() : null)
                .createdBy(adminUserId)
                .createdAt(saved.getCreatedAt())
                .build());

        return productMapper.toDto(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Update
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ProductDto updateProduct(Long id, UpdateProductRequest req) { //a dto
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        //Request DTO → Entity
        if (StringUtils.hasText(req.getName()) && !req.getName().equals(product.getName())) {
            product.setName(req.getName());
            product.setSlug(generateUniqueProductSlugExcluding(req.getName(), id));
        }
        if (req.getDescription()      != null) product.setDescription(req.getDescription());
        if (req.getShortDescription() != null) product.setShortDescription(req.getShortDescription());
        if (req.getPrice()            != null) product.setPrice(req.getPrice());
        if (req.getCompareAtPrice()   != null) product.setCompareAtPrice(req.getCompareAtPrice());
        if (req.getCostPrice()        != null) product.setCostPrice(req.getCostPrice());
        if (req.getStatus()           != null) product.setStatus(req.getStatus());
        if (req.getBrand()            != null) product.setBrand(req.getBrand());
        if (req.getWeightGrams()      != null) product.setWeightGrams(req.getWeightGrams());
        if (req.getTags()             != null) product.setTags(req.getTags());
        if (req.getFeatured()         != null) product.setFeatured(req.getFeatured());
        if (req.getLowStockThreshold()!= null) product.setLowStockThreshold(req.getLowStockThreshold());
        if (req.getTrackInventory()   != null) product.setTrackInventory(req.getTrackInventory());
        if (req.getAllowBackorder()    != null) product.setAllowBackorder(req.getAllowBackorder());

        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(req.getCategoryId()));
            product.setCategory(category);
        }

        // Replace images if provided
        if (req.getImages() != null) {
            product.getImages().clear();
            req.getImages().forEach(imgReq -> {
                ProductImage img = ProductImage.builder()
                        .product(product)
                        .url(imgReq.getUrl())
                        .altText(imgReq.getAltText())
                        .displayOrder(imgReq.getDisplayOrder() != null ? imgReq.getDisplayOrder() : 0)
                        .primary(imgReq.getPrimary() != null ? imgReq.getPrimary() : false)
                        .build();
                product.getImages().add(img);
            });
        }

        Product saved = productRepository.save(product);
        log.info("Product updated: {} (id: {})", saved.getName(), saved.getId());

        eventPublisher.publishProductUpdated(ProductUpdatedEvent.builder()
                .productId(saved.getId())
                .name(saved.getName())
                .sku(saved.getSku())
                .price(saved.getPrice())
                .status(saved.getStatus().name())
                .categoryId(saved.getCategory() != null ? saved.getCategory().getId() : null)
                .updatedAt(saved.getUpdatedAt())
                .build());

        //Entity → Response DTO
        return productMapper.toDto(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Delete
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteProduct(Long id, Long deletedBy) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        String sku = product.getSku();
        productRepository.delete(product);
        log.info("Product deleted: id={}, sku={}", id, sku);

        eventPublisher.publishProductDeleted(ProductDeletedEvent.builder()
                .productId(id)
                .sku(sku)
                .deletedBy(deletedBy)
                .deletedAt(LocalDateTime.now().toString())
                .build());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Read
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        return productMapper.toDto(productRepository.findById(id) //from database to frontend, so map entity to dto
                .orElseThrow(() -> new ProductNotFoundException(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductBySlug(String slug) {
        return productMapper.toDto(productRepository.findBySlug(slug)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with slug: " + slug)));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductBySku(String sku) {
        return productMapper.toDto(productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku)));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductDto> getAllProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductDto> result = productRepository.findAll(pageable).map(productMapper::toDto);
        return PagedResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductDto> searchProducts(ProductSearchRequest req) {
        Sort sort = req.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(req.getSortBy()).ascending() : Sort.by(req.getSortBy()).descending();
        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);
        Page<ProductDto> result = productRepository
                .findAll(ProductSpecification.fromSearchRequest(req), pageable)
                .map(productMapper::toDto);
        return PagedResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getFeaturedProducts() {
        return productMapper.toDtoList(
                productRepository.findAllByFeaturedTrueAndStatus(ProductStatus.ACTIVE));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllBrands() {
        return productRepository.findAllActiveBrands();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private String generateUniqueProductSlug(String name) {
        String base = SlugUtil.toSlug(name);
        String slug = base;
        int attempt = 0;
        while (productRepository.existsBySlug(slug)) {
            slug = SlugUtil.toUniqueSlug(base, ++attempt);
        }
        return slug;
    }

    private String generateUniqueProductSlugExcluding(String name, Long excludeId) {
        String base = SlugUtil.toSlug(name);
        String slug = base;
        int attempt = 0;
        while (productRepository.existsBySlug(slug)) {
            // If the conflicting slug belongs to this product, it's fine
            String finalSlug = slug;
            productRepository.findBySlug(slug).ifPresent(p -> {
                if (!p.getId().equals(excludeId))
                    throw new DuplicateResourceException("Slug conflict for: " + finalSlug);
            });
            slug = SlugUtil.toUniqueSlug(base, ++attempt);
        }
        return slug;
    }

    private String generateSku() {
        return "SKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
