package com.eris.fintrack.api.transaction;

import com.eris.fintrack.api.attachment.dto.AttachmentResponse;
import com.eris.fintrack.api.common.ApiResponse;
import com.eris.fintrack.api.common.PaginatedResponse;
import com.eris.fintrack.api.mapper.AttachmentMapper;
import com.eris.fintrack.api.mapper.TransactionMapper;
import com.eris.fintrack.api.transaction.dto.CreateTransactionRequest;
import com.eris.fintrack.api.transaction.dto.CreateTransferRequest;
import com.eris.fintrack.api.transaction.dto.TransactionResponse;
import com.eris.fintrack.api.transaction.dto.UpdateTransactionRequest;
import com.eris.fintrack.application.service.TransactionService;
import com.eris.fintrack.domain.Attachment;
import com.eris.fintrack.domain.Transaction;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;
    private final AttachmentMapper attachmentMapper;

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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> findTransactionById(@PathVariable UUID id) {
        Transaction transaction = transactionService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(transactionMapper.toDto(transaction)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(@PathVariable UUID id) {
        transactionService.deleteTransaction(id);
        return new ResponseEntity<>(ApiResponse.success(null), HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequest request) {

        Transaction updatedTransaction = transactionService.updateTransaction(id, request);
        return ResponseEntity.ok(ApiResponse.success(transactionMapper.toDto(updatedTransaction)));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<Void>> createTransfer(@Valid @RequestBody CreateTransferRequest request) {
        transactionService.createTransfer(request);

        return new ResponseEntity<>(ApiResponse.success(null), HttpStatus.NO_CONTENT);
    }

    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadAttachment(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {

        Attachment attachment = transactionService.addAttachmentToTransaction(id, file);

        return new ResponseEntity<>(
                ApiResponse.success(attachmentMapper.toDto(attachment)),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/{transactionId}/attachments/{attachmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @PathVariable UUID transactionId,
            @PathVariable UUID attachmentId) {

        transactionService.deleteAttachment(transactionId, attachmentId);
        return new ResponseEntity<>(ApiResponse.success(null), HttpStatus.NO_CONTENT);
    }
}