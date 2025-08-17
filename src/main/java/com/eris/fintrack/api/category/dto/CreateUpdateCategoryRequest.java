package com.eris.fintrack.api.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUpdateCategoryRequest {
    @NotBlank(message = "Category name is required")
    private String name;

    @NotBlank(message = "Category type is required (e.g., INCOME or EXPENSE)")
    private String type;
}