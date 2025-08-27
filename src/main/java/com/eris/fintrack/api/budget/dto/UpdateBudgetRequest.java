package com.eris.fintrack.api.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateBudgetRequest {
    @NotNull(message = "Amount limit is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount limit must be positive")
    private BigDecimal amountLimit;
}
