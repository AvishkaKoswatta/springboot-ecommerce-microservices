package com.microservice.productservice.service;

import com.microservice.productservice.dto.CategoryDtos.*;
import com.microservice.productservice.entity.Category;
import com.microservice.productservice.exception.CategoryNotFoundException;
import com.microservice.productservice.exception.DuplicateResourceException;
import com.microservice.productservice.mapper.CategoryMapper;
import com.microservice.productservice.repository.CategoryRepository;
import com.microservice.productservice.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto createCategory(CreateCategoryRequest request) {
        String slug = generateUniqueSlug(request.getName());

        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category already exists with name: " + request.getName());
        }

        Category.CategoryBuilder builder = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl());

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getParentId()));
            builder.parent(parent);
        }

        Category saved = categoryRepository.save(builder.build());
        log.info("Category created: {} (slug: {})", saved.getName(), saved.getSlug());
        return categoryMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        if (StringUtils.hasText(request.getName()) && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(request.getName())) {
                throw new DuplicateResourceException("Category already exists with name: " + request.getName());
            }
            category.setName(request.getName());
            category.setSlug(generateUniqueSlug(request.getName()));
        }

        if (StringUtils.hasText(request.getDescription())) category.setDescription(request.getDescription());
        if (StringUtils.hasText(request.getImageUrl()))    category.setImageUrl(request.getImageUrl());
        if (request.getActive() != null)                   category.setActive(request.getActive());

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new IllegalArgumentException("A category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getParentId()));
            category.setParent(parent);
        }

        Category saved = categoryRepository.save(category);
        log.info("Category updated: {} (id: {})", saved.getName(), saved.getId());
        return categoryMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        if (!category.getProducts().isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete category with " + category.getProducts().size() + " product(s) assigned to it");
        }

        categoryRepository.delete(category);
        log.info("Category deleted: {} (id: {})", category.getName(), id);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        return categoryMapper.toDto(categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryBySlug(String slug) {
        return categoryMapper.toDto(categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with slug: " + slug)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryMapper.toDtoList(categoryRepository.findAllActive());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getRootCategories() {
        return categoryMapper.toDtoList(categoryRepository.findAllByParentIsNullAndActiveTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getChildCategories(Long parentId) {
        categoryRepository.findById(parentId)
                .orElseThrow(() -> new CategoryNotFoundException(parentId));
        return categoryMapper.toDtoList(categoryRepository.findAllByParentIdAndActiveTrue(parentId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    private String generateUniqueSlug(String name) {
        String base = SlugUtil.toSlug(name);
        String slug = base;
        int attempt = 0;
        while (categoryRepository.existsBySlug(slug)) {
            slug = SlugUtil.toUniqueSlug(base, ++attempt);
        }
        return slug;
    }
}
