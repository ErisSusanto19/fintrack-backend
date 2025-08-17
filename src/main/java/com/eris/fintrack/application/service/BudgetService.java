package com.eris.fintrack.application.service;

import com.eris.fintrack.api.budget.dto.CreateUpdateBudgetRequest;
import com.eris.fintrack.domain.Budget;

import java.util.List;
import java.util.UUID;

public interface BudgetService {
    Budget createBudget(CreateUpdateBudgetRequest request);
    List<Budget> getBudgets(int year, int month);
    void deleteBudget(UUID budgetId);
}