package com.eris.fintrack.api.budget.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class BudgetResponse {
    private UUID id;
    private UUID categoryId;
    private String categoryName;
    private int year;
    private int month;
    private BigDecimal amountLimit;
    private BigDecimal amountSpent;
    private BigDecimal remainingAmount;
    private double percentageSpent;
}