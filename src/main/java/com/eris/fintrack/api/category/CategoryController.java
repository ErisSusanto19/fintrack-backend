package com.eris.fintrack.api.category;

import com.eris.fintrack.api.category.dto.CategoryResponse;
import com.eris.fintrack.api.category.dto.CreateCategoryRequest;
import com.eris.fintrack.api.category.dto.UpdateCategoryRequest;
import com.eris.fintrack.api.common.ApiResponse;
import com.eris.fintrack.api.mapper.CategoryMapper;
import com.eris.fintrack.application.service.CategoryService;
import com.eris.fintrack.domain.Category;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        Category createdCategory = categoryService.createCategory(request);
        CategoryResponse responseDto = categoryMapper.toDto(createdCategory);
        return new ResponseEntity<>(ApiResponse.success(responseDto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategoriesForCurrentUser();
        List<CategoryResponse> response = categories.stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> findCategoryById(@PathVariable UUID id) {
        Category category = categoryService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(categoryMapper.toDto(category)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategoryById(id);
        return new ResponseEntity<>(ApiResponse.success(null), HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(@PathVariable UUID id, @Valid @RequestBody UpdateCategoryRequest request){
        Category updatedCategory = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success(categoryMapper.toDto(updatedCategory)));
    }
}