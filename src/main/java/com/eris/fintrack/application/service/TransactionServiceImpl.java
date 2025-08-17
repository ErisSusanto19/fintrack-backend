package com.eris.fintrack.application.service;

import com.eris.fintrack.api.exception.BadRequestException;
import com.eris.fintrack.api.exception.ForbiddenException;
import com.eris.fintrack.api.exception.ResourceNotFoundException;
import com.eris.fintrack.api.transaction.dto.CreateTransactionRequest;
import com.eris.fintrack.api.transaction.dto.CreateTransferRequest;
import com.eris.fintrack.api.transaction.dto.UpdateTransactionRequest;
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

import java.math.BigDecimal;
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

    @Override
    @Transactional
    public Transaction updateTransaction(UUID transactionId, UpdateTransactionRequest request) {
        User currentUser = userContextService.getCurrentUser();

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction with id " + transactionId + " not found"));

        if (!transaction.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access Denied to update this transaction");
        }

        Account originalAccount = transaction.getAccount();
        BigDecimal originalAmount = transaction.getAmount();
        TransactionType originalType = transaction.getType();

        if (originalType == TransactionType.EXPENSE) {
            originalAccount.setBalance(originalAccount.getBalance().add(originalAmount));
        } else {
            originalAccount.setBalance(originalAccount.getBalance().subtract(originalAmount));
        }

        Account destinationAccount;
        if (originalAccount.getId().equals(request.getAccountId())) {
            destinationAccount = originalAccount;
        } else {
            destinationAccount = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Destination account with id " + request.getAccountId() + " not found"));
            if (!destinationAccount.getUser().getId().equals(currentUser.getId())) {
                throw new ForbiddenException("Access Denied: Destination account does not belong to user");
            }

            accountRepository.save(originalAccount);
        }

        TransactionType newType = TransactionType.valueOf(request.getType().toUpperCase());
        if (newType == TransactionType.EXPENSE) {
            destinationAccount.setBalance(destinationAccount.getBalance().subtract(request.getAmount()));
        } else {
            destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));
        }
        accountRepository.save(destinationAccount);

        Category newCategory = null;
        if (request.getCategoryId() != null) {
            newCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category with id " + request.getCategoryId() + " not found"));
            if (!newCategory.getUser().getId().equals(currentUser.getId())) {
                throw new ForbiddenException("Access Denied: Category does not belong to user");
            }
        }

        transaction.setAccount(destinationAccount);
        transaction.setCategory(newCategory);
        transaction.setType(newType);
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setDescription(request.getDescription());

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void createTransfer(CreateTransferRequest request) {
        User currentUser = userContextService.getCurrentUser();

        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new BadRequestException("Source and destination accounts cannot be the same.");
        }

        Account fromAccount = accountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found"));
        if (!fromAccount.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access Denied: Source account does not belong to user");
        }

        Account toAccount = accountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));
        if (!toAccount.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access Denied: Destination account does not belong to user");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        UUID transferId = UUID.randomUUID();

        Transaction expenseTransaction = Transaction.builder()
                .user(currentUser)
                .account(fromAccount)
                .type(TransactionType.EXPENSE)
                .amount(request.getAmount())
                .transactionDate(request.getTransactionDate())
                .description(request.getDescription())
                .transferId(transferId)
                .category(null)
                .build();

        Transaction incomeTransaction = Transaction.builder()
                .user(currentUser)
                .account(toAccount)
                .type(TransactionType.INCOME)
                .amount(request.getAmount())
                .transactionDate(request.getTransactionDate())
                .description(request.getDescription())
                .transferId(transferId)
                .category(null)
                .build();

        transactionRepository.save(expenseTransaction);
        transactionRepository.save(incomeTransaction);
    }
}