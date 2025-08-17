package com.eris.fintrack.application.service;

import com.eris.fintrack.api.category.dto.CreateUpdateCategoryRequest;
import com.eris.fintrack.domain.Category;
import com.eris.fintrack.domain.User;
import com.eris.fintrack.infrastructure.persistence.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserContextService userContextService;

    @Override
    @Transactional
    public Category createCategory(CreateUpdateCategoryRequest request) {
        User currentUser = userContextService.getCurrentUser();

        Category category = Category.builder()
                .user(currentUser)
                .name(request.getName())
                .type(request.getType().toUpperCase())
                .build();

        return categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategoriesForCurrentUser() {
        User currentUser = userContextService.getCurrentUser();
        return categoryRepository.findByUserId(currentUser.getId());
    }

    @Override
    @Transactional
    public void deleteCategoryById(UUID categoryId) {
        User currentUser = userContextService.getCurrentUser();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!category.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Access Denied: You do not own this category");
        }

        categoryRepository.delete(category);
    }
}