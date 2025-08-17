package com.eris.fintrack.application.service;

import com.eris.fintrack.api.exception.ForbiddenException;
import com.eris.fintrack.api.exception.ResourceNotFoundException;
import com.eris.fintrack.api.transaction.dto.CreateTransactionRequest;
import com.eris.fintrack.domain.Account;
import com.eris.fintrack.domain.Category;
import com.eris.fintrack.domain.Transaction;
import com.eris.fintrack.domain.User;
import com.eris.fintrack.domain.enums.TransactionType;
import com.eris.fintrack.infrastructure.persistence.AccountRepository;
import com.eris.fintrack.infrastructure.persistence.CategoryRepository;
import com.eris.fintrack.infrastructure.persistence.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        TransactionType type = TransactionType.valueOf(request.getType().toUpperCase());

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account with id " + request.getAccountId() + " not found"));
        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access Denied: Account does not belong to user");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category with id " + request.getCategoryId() + " not found"));
            if (!category.getUser().getId().equals(currentUser.getId())) {
                throw new ForbiddenException("Access Denied: Category does not belong to user");
            }
        }

        if (type == TransactionType.EXPENSE) {
            account.setBalance(account.getBalance().subtract(request.getAmount()));
        } else if (type == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(request.getAmount()));
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
    public Page<Transaction> getTransactionsForCurrentUser(Pageable pageable) {
        User currentUser = userContextService.getCurrentUser();
        return transactionRepository.findByUserId(currentUser.getId(), pageable);
    }

    @Override
    @Transactional
    public void deleteTransaction(UUID transactionId) {
        User currentUser = userContextService.getCurrentUser();
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction with id " + transactionId + " not found"));

        if (!transaction.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access Denied to delete this transaction");
        }

        Account account = transaction.getAccount();
        if (transaction.getType() == TransactionType.EXPENSE) {
            account.setBalance(account.getBalance().add(transaction.getAmount()));
        } else if (transaction.getType() == TransactionType.INCOME) {
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        }
        accountRepository.save(account);

        transactionRepository.delete(transaction);
    }
}