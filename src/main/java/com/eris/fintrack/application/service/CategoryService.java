package com.eris.fintrack.application.service;

import com.eris.fintrack.api.category.dto.CreateUpdateCategoryRequest;
import com.eris.fintrack.domain.Category;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    Category createCategory(CreateUpdateCategoryRequest request);
    List<Category> getAllCategoriesForCurrentUser();
    Category findById(UUID categoryId);
    void deleteCategoryById(UUID categoryId);
}