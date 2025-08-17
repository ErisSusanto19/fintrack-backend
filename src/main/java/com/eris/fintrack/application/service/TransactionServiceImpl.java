package com.eris.fintrack.application.service;

import com.eris.fintrack.api.transaction.dto.CreateTransactionRequest;
import com.eris.fintrack.domain.Account;
import com.eris.fintrack.domain.Category;
import com.eris.fintrack.domain.Transaction;
import com.eris.fintrack.domain.User;
import com.eris.fintrack.infrastructure.persistence.AccountRepository;
import com.eris.fintrack.infrastructure.persistence.CategoryRepository;
import com.eris.fintrack.infrastructure.persistence.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserContextService userContextService;

    @Override
    @Transactional
    public Transaction createTransaction(CreateTransactionRequest request) {
        User currentUser = userContextService.getCurrentUser();
        String type = request.getType().toUpperCase();

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));
        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Access Denied: Account does not belong to user");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            if (!category.getUser().getId().equals(currentUser.getId())) {
                throw new SecurityException("Access Denied: Category does not belong to user");
            }
        }

        if (Objects.equals(type, "EXPENSE")) {
            account.setBalance(account.getBalance().subtract(request.getAmount()));
        } else if (Objects.equals(type, "INCOME")) {
            account.setBalance(account.getBalance().add(request.getAmount()));
        } else {
            throw new IllegalArgumentException("Invalid transaction type: " + type);
        }
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .user(currentUser)
                .account(account)
                .category(category)
                .type(type)
                .amount(request.getAmount())
                .transactionDate(request.getTransactionDate())
                .description(request.getDescription())
                .build();

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsForCurrentUser() {
        User currentUser = userContextService.getCurrentUser();
        return transactionRepository.findByUserId(currentUser.getId());
    }

    @Override
    @Transactional
    public void deleteTransaction(UUID transactionId) {
        User currentUser = userContextService.getCurrentUser();
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Access Denied to delete this transaction");
        }

        Account account = transaction.getAccount();
        if (Objects.equals(transaction.getType(), "EXPENSE")) {
            account.setBalance(account.getBalance().add(transaction.getAmount()));
        } else if (Objects.equals(transaction.getType(), "INCOME")) {
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        }
        accountRepository.save(account);

        transactionRepository.delete(transaction);
    }
}