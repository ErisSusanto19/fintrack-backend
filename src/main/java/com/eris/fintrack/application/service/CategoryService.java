package com.eris.fintrack.application.service;

import com.eris.fintrack.api.category.dto.CreateCategoryRequest;
import com.eris.fintrack.api.category.dto.UpdateCategoryRequest;
import com.eris.fintrack.domain.Category;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    Category createCategory(CreateCategoryRequest request);
    List<Category> getAllCategoriesForCurrentUser();
    Category findById(UUID categoryId);
    void deleteCategoryById(UUID categoryId);
    Category updateCategory(UUID categoryId, UpdateCategoryRequest request);
}