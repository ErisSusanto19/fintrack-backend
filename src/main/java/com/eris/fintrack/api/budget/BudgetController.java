package com.eris.fintrack.api.budget;

import com.eris.fintrack.api.budget.dto.BudgetResponse;
import com.eris.fintrack.api.budget.dto.CreateUpdateBudgetRequest;
import com.eris.fintrack.api.common.ApiResponse;
import com.eris.fintrack.api.mapper.BudgetMapper;
import com.eris.fintrack.application.service.BudgetService;
import com.eris.fintrack.domain.Budget;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
    private final BudgetMapper budgetMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(@Valid @RequestBody CreateUpdateBudgetRequest request) {
        Budget budget = budgetService.createBudget(request);
        return new ResponseEntity<>(ApiResponse.success(budgetMapper.toDto(budget)), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgets(
            @RequestParam int year,
            @RequestParam int month) {
        List<Budget> budgets = budgetService.getBudgets(year, month);
        List<BudgetResponse> response = budgets.stream()
                .map(budgetMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> findBudgetById(@PathVariable UUID id) {
        Budget budget = budgetService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(budgetMapper.toDto(budget)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(@PathVariable UUID id) {
        budgetService.deleteBudget(id);
        return new ResponseEntity<>(ApiResponse.success(null), HttpStatus.NO_CONTENT);
    }
}