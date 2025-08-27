package com.eris.fintrack.api.budget.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateBudgetRequest {

    private UUID categoryId;

    @NotNull(message = "Year is required")
    private Integer year;

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @NotNull(message = "Amount limit is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount limit must be positive")
    private BigDecimal amountLimit;
}