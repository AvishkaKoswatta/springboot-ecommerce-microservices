package com.microservice.productservice.service;

import com.microservice.productservice.dto.CategoryDtos.*;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(CreateCategoryRequest request);
    CategoryDto updateCategory(Long id, UpdateCategoryRequest request);
    void deleteCategory(Long id);
    CategoryDto getCategoryById(Long id);
    CategoryDto getCategoryBySlug(String slug);
    List<CategoryDto> getAllCategories();
    List<CategoryDto> getRootCategories();
    List<CategoryDto> getChildCategories(Long parentId);
}
