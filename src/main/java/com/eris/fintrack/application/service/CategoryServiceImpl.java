package com.eris.fintrack.application.service;

import com.eris.fintrack.api.category.dto.CreateUpdateCategoryRequest;
import com.eris.fintrack.domain.Category;
import com.eris.fintrack.domain.User;
import com.eris.fintrack.infrastructure.persistence.CategoryRepository;
import com.eris.fintrack.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Category createCategory(CreateUpdateCategoryRequest request) {
        User currentUser = getCurrentUser();

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
        User currentUser = getCurrentUser();
        return categoryRepository.findByUserId(currentUser.getId());
    }

    @Override
    @Transactional
    public void deleteCategoryById(UUID categoryId) {
        User currentUser = getCurrentUser();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!category.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Access Denied: You do not own this category");
        }

        categoryRepository.delete(category);
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
    }
}