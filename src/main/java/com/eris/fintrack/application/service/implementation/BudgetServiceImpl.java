package com.eris.fintrack.application.service.implementation;

import com.eris.fintrack.api.budget.dto.BudgetResponse;
import com.eris.fintrack.api.budget.dto.CreateBudgetRequest;
import com.eris.fintrack.api.budget.dto.UpdateBudgetRequest;
import com.eris.fintrack.api.exception.BadRequestException;
import com.eris.fintrack.api.exception.ForbiddenException;
import com.eris.fintrack.api.exception.ResourceNotFoundException;
import com.eris.fintrack.application.service.BudgetService;
import com.eris.fintrack.domain.Budget;
import com.eris.fintrack.domain.Category;
import com.eris.fintrack.domain.User;
import com.eris.fintrack.infrastructure.persistence.BudgetRepository;
import com.eris.fintrack.infrastructure.persistence.CategoryRepository;
import com.eris.fintrack.infrastructure.persistence.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserContextService userContextService;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public Budget createBudget(CreateBudgetRequest request) {
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
    public List<BudgetResponse> getBudgets(int year, int month) {
        User currentUser = userContextService.getCurrentUser();

        List<Budget> budgets = budgetRepository.findByUserIdAndYearAndMonth(currentUser.getId(), year, month);

        return budgets.stream()
                .map(this::enrichBudgetWithStatus)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Budget findById(UUID budgetId) {
        User currentUser = userContextService.getCurrentUser();
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        if (!budget.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access Denied: You do not own this budget");
        }
        return budget;
    }

    @Override
    @Transactional
    public void deleteBudget(UUID budgetId) {
        User currentUser = userContextService.getCurrentUser();
        Budget budget = this.findById(budgetId);
        budgetRepository.delete(budget);
    }

    @Override
    @Transactional
    public Budget updateBudget(UUID budgetId, UpdateBudgetRequest request) {
        Budget budgetToUpdate = this.findById(budgetId);

        budgetToUpdate.setAmountLimit(request.getAmountLimit());

        return budgetRepository.save(budgetToUpdate);
    }

    @Override
    @Transactional
    public BudgetResponse findBudgetWithStatusById(UUID budgetId){
        Budget budget = this.findById(budgetId);

        return enrichBudgetWithStatus(budget);
    }

    private BudgetResponse enrichBudgetWithStatus(Budget budget) {
        User currentUser = budget.getUser();
        LocalDate startDate = YearMonth.of(budget.getYear(), budget.getMonth()).atDay(1);
        LocalDate endDate = YearMonth.of(budget.getYear(), budget.getMonth()).atEndOfMonth();
        UUID categoryId = (budget.getCategory() != null) ? budget.getCategory().getId() : null;

        BigDecimal amountSpent = transactionRepository.sumExpensesByUserIdAndCategoryAndDateRange(
                currentUser.getId(), categoryId, startDate, endDate);

        BigDecimal remainingAmount = budget.getAmountLimit().subtract(amountSpent);
        BigDecimal percentage = BigDecimal.ZERO;
        if (budget.getAmountLimit().compareTo(BigDecimal.ZERO) > 0) {
            percentage = amountSpent.multiply(new BigDecimal("100"))
                    .divide(budget.getAmountLimit(), 2, RoundingMode.HALF_UP);
        }

        return BudgetResponse.builder()
                    .id(budget.getId())
                    .categoryId(categoryId)
                    .categoryName(budget.getCategory() != null ? budget.getCategory().getName() : "Overall Budget")
                    .year(budget.getYear())
                    .month(budget.getMonth())
                    .amountLimit(budget.getAmountLimit())
                    .amountSpent(amountSpent)
                    .remainingAmount(remainingAmount)
                    .percentageSpent(percentage.doubleValue())
                    .build();
    }
}