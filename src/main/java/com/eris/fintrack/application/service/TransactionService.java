package com.eris.fintrack.application.service;

import com.eris.fintrack.api.transaction.dto.CreateTransactionRequest;
import com.eris.fintrack.domain.Transaction;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    Transaction createTransaction(CreateTransactionRequest request);
    List<Transaction> getTransactionsForCurrentUser();
    void deleteTransaction(UUID transactionId);
}