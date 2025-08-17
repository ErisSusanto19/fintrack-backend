package com.eris.fintrack.api.transaction;

import com.eris.fintrack.api.common.ApiResponse;
import com.eris.fintrack.api.common.PaginatedResponse;
import com.eris.fintrack.api.mapper.TransactionMapper;
import com.eris.fintrack.api.transaction.dto.CreateTransactionRequest;
import com.eris.fintrack.api.transaction.dto.TransactionResponse;
import com.eris.fintrack.application.service.TransactionService;
import com.eris.fintrack.domain.Transaction;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        Transaction transaction = transactionService.createTransaction(request);
        TransactionResponse responseDto = transactionMapper.toDto(transaction);
        return new ResponseEntity<>(ApiResponse.success(responseDto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<TransactionResponse>>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate,desc") String[] sort
    ) {
        Sort.Direction direction = sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sorting = Sort.by(direction, sort[0]);

        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Transaction> transactionPage = transactionService.getTransactionsForCurrentUser(pageable);

        List<TransactionResponse> responseContent = transactionPage.getContent().stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());

        PaginatedResponse<TransactionResponse> paginatedResponse = new PaginatedResponse<>(
                responseContent,
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages(),
                transactionPage.isLast()
        );

        return ResponseEntity.ok(ApiResponse.success(paginatedResponse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(@PathVariable UUID id) {
        transactionService.deleteTransaction(id);
        return new ResponseEntity<>(ApiResponse.success(null), HttpStatus.NO_CONTENT);
    }
}