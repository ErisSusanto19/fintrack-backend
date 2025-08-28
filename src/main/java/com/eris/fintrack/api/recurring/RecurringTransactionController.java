package com.eris.fintrack.api.recurring;

import com.eris.fintrack.api.common.ApiResponse;
import com.eris.fintrack.api.mapper.RecurringTransactionMapper;
import com.eris.fintrack.api.recurring.dto.CreateRecurringTransactionRequest;
import com.eris.fintrack.api.recurring.dto.RecurringTransactionResponse;
import com.eris.fintrack.api.recurring.dto.UpdateRecurringTransactionRequest;
import com.eris.fintrack.application.service.RecurringTransactionService;
import com.eris.fintrack.domain.RecurringTransaction;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/recurring-transactions")
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;
    private final RecurringTransactionMapper recurringTransactionMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> create(@Valid @RequestBody CreateRecurringTransactionRequest request) {
        RecurringTransaction created = recurringTransactionService.create(request);
        return new ResponseEntity<>(ApiResponse.success(recurringTransactionMapper.toDto(created)), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RecurringTransactionResponse>>> findAll() {
        List<RecurringTransactionResponse> response = recurringTransactionService.findAllForCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> findById(@PathVariable UUID id) {
        RecurringTransactionResponse recurring = recurringTransactionService.findCompleteRecurringById(id);
        return ResponseEntity.ok(ApiResponse.success(recurring));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> update(@PathVariable UUID id, @RequestBody UpdateRecurringTransactionRequest request) {
        RecurringTransaction updated = recurringTransactionService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(recurringTransactionMapper.toDto(updated)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        recurringTransactionService.delete(id);
        return new ResponseEntity<>(ApiResponse.success(null), HttpStatus.NO_CONTENT);
    }
}