package com.eris.fintrack.application.service;

import com.eris.fintrack.api.transaction.dto.CreateTransactionRequest;
import com.eris.fintrack.api.transaction.dto.CreateTransferRequest;
import com.eris.fintrack.api.transaction.dto.UpdateTransactionRequest;
import com.eris.fintrack.domain.Transaction;
import com.eris.fintrack.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TransactionService {
    Transaction createTransaction(CreateTransactionRequest request);
    Transaction createTransactionFromScheduler(CreateTransactionRequest request, User user);
    Transaction updateTransaction(UUID transactionId, UpdateTransactionRequest request);
    Page<Transaction> getTransactionsForCurrentUser(Pageable pageable);
    void deleteTransaction(UUID transactionId);
    void createTransfer(CreateTransferRequest request);
}