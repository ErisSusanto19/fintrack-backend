package com.eris.fintrack.api.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCategoryRequest {
    @NotBlank(message = "Category name is required")
    private String name;
}
