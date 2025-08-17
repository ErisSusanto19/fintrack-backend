package com.eris.fintrack.application.service;

import com.eris.fintrack.api.budget.dto.CreateUpdateBudgetRequest;
import com.eris.fintrack.api.exception.BadRequestException;
import com.eris.fintrack.api.exception.ForbiddenException;
import com.eris.fintrack.api.exception.ResourceNotFoundException;
import com.eris.fintrack.domain.Budget;
import com.eris.fintrack.domain.Category;
import com.eris.fintrack.domain.User;
import com.eris.fintrack.infrastructure.persistence.BudgetRepository;
import com.eris.fintrack.infrastructure.persistence.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserContextService userContextService;

    @Override
    @Transactional
    public Budget createBudget(CreateUpdateBudgetRequest request) {
        User currentUser = userContextService.getCurrentUser();

        budgetRepository.findByUserIdAndCategoryIdAndYearAndMonth(
                        currentUser.getId(), request.getCategoryId(), request.getYear(), request.getMonth())
                .ifPresent(b -> {
                    throw new BadRequestException("A budget for this category and period already exists.");
                });

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            if (!category.getUser().getId().equals(currentUser.getId())) {
                throw new ForbiddenException("Access Denied: Category does not belong to user");
            }
        }

        Budget budget = Budget.builder()
                .user(currentUser)
                .category(category)
                .year(request.getYear())
                .month(request.getMonth())
                .amountLimit(request.getAmountLimit())
                .build();

        return budgetRepository.save(budget);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Budget> getBudgets(int year, int month) {
        User currentUser = userContextService.getCurrentUser();
        return budgetRepository.findByUserIdAndYearAndMonth(currentUser.getId(), year, month);
    }

    @Override
    @Transactional
    public void deleteBudget(UUID budgetId) {
        User currentUser = userContextService.getCurrentUser();
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        if (!budget.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access Denied: You do not own this budget");
        }
        budgetRepository.delete(budget);
    }
}