package com.eris.fintrack.application.service;

import com.eris.fintrack.api.transaction.dto.CreateTransactionRequest;
import com.eris.fintrack.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    Transaction createTransaction(CreateTransactionRequest request);
    Page<Transaction> getTransactionsForCurrentUser(Pageable pageable);
    void deleteTransaction(UUID transactionId);
}