package com.eris.fintrack.api.budget;

import com.eris.fintrack.api.budget.dto.BudgetResponse;
import com.eris.fintrack.api.budget.dto.CreateBudgetRequest;
import com.eris.fintrack.api.budget.dto.UpdateBudgetRequest;
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
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(@Valid @RequestBody CreateBudgetRequest request) {
        Budget budget = budgetService.createBudget(request);
        return new ResponseEntity<>(ApiResponse.success(budgetMapper.toDto(budget)), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgets(
            @RequestParam int year,
            @RequestParam int month) {
        List<BudgetResponse> budgets = budgetService.getBudgets(year, month);
        return ResponseEntity.ok(ApiResponse.success(budgets));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> findBudgetById(@PathVariable UUID id) {
        BudgetResponse budget = budgetService.findBudgetWithStatusById(id);
        return ResponseEntity.ok(ApiResponse.success(budget));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(@PathVariable UUID id) {
        budgetService.deleteBudget(id);
        return new ResponseEntity<>(ApiResponse.success(null), HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBudgetRequest request) {
        Budget updatedBudget = budgetService.updateBudget(id, request);
        return ResponseEntity.ok(ApiResponse.success(budgetMapper.toDto(updatedBudget)));
    }
}