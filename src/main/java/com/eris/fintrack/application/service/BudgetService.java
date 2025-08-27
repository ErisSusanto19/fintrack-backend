package com.eris.fintrack.application.service;

import com.eris.fintrack.api.budget.dto.BudgetResponse;
import com.eris.fintrack.api.budget.dto.CreateBudgetRequest;
import com.eris.fintrack.api.budget.dto.UpdateBudgetRequest;
import com.eris.fintrack.domain.Budget;

import java.util.List;
import java.util.UUID;

public interface BudgetService {
    Budget createBudget(CreateBudgetRequest request);
    List<BudgetResponse> getBudgets(int year, int month);
    Budget findById(UUID budgetId);
    void deleteBudget(UUID budgetId);
    Budget updateBudget(UUID budgetId, UpdateBudgetRequest request);
    BudgetResponse findBudgetWithStatusById(UUID budgetId);
}