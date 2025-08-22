package com.eris.fintrack.application.service.implementation;

import com.eris.fintrack.api.exception.BadRequestException;
import com.eris.fintrack.api.exception.ForbiddenException;
import com.eris.fintrack.api.exception.ResourceNotFoundException;
import com.eris.fintrack.api.mapper.TransactionMapper;
import com.eris.fintrack.api.transaction.dto.CreateTransactionRequest;
import com.eris.fintrack.api.transaction.dto.CreateTransferRequest;
import com.eris.fintrack.api.transaction.dto.TransactionResponse;
import com.eris.fintrack.api.transaction.dto.UpdateTransactionRequest;
import com.eris.fintrack.application.service.TransactionService;
import com.eris.fintrack.application.service.storage.FileStorageService;
import com.eris.fintrack.domain.*;
import com.eris.fintrack.domain.enums.TransactionType;
import com.eris.fintrack.infrastructure.persistence.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserContextService userContextService;
    private final BudgetRepository budgetRepository;
    private final CacheManager cacheManager;
    private final FileStorageService fileStorageService;
    private final AttachmentRepository attachmentRepository;

    @Override
    @Transactional
    public Transaction createTransaction(CreateTransactionRequest request) {
        clearReportCaches();
        User currentUser = userContextService.getCurrentUser();
        return createTransactionForUser(request, currentUser);
    }

    @Override
    @Transactional
    public Transaction createTransactionFromScheduler(CreateTransactionRequest request, User user) {
        clearReportCaches();
        return createTransactionForUser(request, user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionsForCurrentUser(Pageable pageable) {
        User currentUser = userContextService.getCurrentUser();
        return transactionRepository.findByUserId(currentUser.getId(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Transaction findById(UUID transactionId) {
        User currentUser = userContextService.getCurrentUser();
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access Denied to this transaction");
        }

        return transaction;
    }

    @Override
    @Transactional
    public void deleteTransaction(UUID transactionId) {
        clearReportCaches();
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
        clearReportCaches();
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
        clearReportCaches();
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

    @Override
    @Transactional
    public Attachment addAttachmentToTransaction(UUID transactionId, MultipartFile file) {
        User currentUser = userContextService.getCurrentUser();

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        if (!transaction.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access Denied");
        }

        String subDirectory = currentUser.getId().toString();
        String storageKey = fileStorageService.save(file, subDirectory);

        Attachment attachment = Attachment.builder()
                .transaction(transaction)
                .fileName(StringUtils.cleanPath(file.getOriginalFilename()))
                .mimeType(file.getContentType())
                .size(file.getSize())
                .storageKey(storageKey)
                .build();

        transaction.getAttachments().add(attachment);

        transactionRepository.save(transaction);

        return attachmentRepository.save(attachment);
    }

    @Override
    public Attachment getAttachmentMetadata(UUID transactionId, UUID attachmentId) {
        User currentUser = userContextService.getCurrentUser();

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        if (!transaction.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access Denied to this transaction");
        }

        return transaction.getAttachments().stream()
                .filter(att -> att.getId().equals(attachmentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));
    }

    @Override
    public Resource getAttachmentResource(UUID transactionId, UUID attachmentId) {
        Attachment attachment = getAttachmentMetadata(transactionId, attachmentId);
        return fileStorageService.load(attachment.getStorageKey());
    }

    @Override
    @Transactional
    public void deleteAttachment(UUID transactionId, UUID attachmentId) {
        clearReportCaches();
        User currentUser = userContextService.getCurrentUser();

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        if (!transaction.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access Denied to this transaction");
        }

        Attachment attachment = transaction.getAttachments().stream()
                .filter(att -> att.getId().equals(attachmentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));

        fileStorageService.delete(attachment.getStorageKey());

        transaction.getAttachments().remove(attachment);

        attachmentRepository.delete(attachment);
    }

    private Transaction createTransactionForUser(CreateTransactionRequest request, User user){
        TransactionType type = TransactionType.valueOf(request.getType().toUpperCase());

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account with id " + request.getAccountId() + " not found"));
        if (!account.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access Denied: Account does not belong to user");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category with id " + request.getCategoryId() + " not found"));
            if (!category.getUser().getId().equals(user.getId())) {
                throw new ForbiddenException("Access Denied: Category does not belong to user");
            }
        }

        if (type == TransactionType.EXPENSE) {
            validateBudget(user, category, request.getTransactionDate(), request.getAmount());
            account.setBalance(account.getBalance().subtract(request.getAmount()));
        } else if (type == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(request.getAmount()));
        }

        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .user(user)
                .account(account)
                .category(category)
                .type(type)
                .amount(request.getAmount())
                .transactionDate(request.getTransactionDate())
                .description(request.getDescription())
                .build();

        return transactionRepository.save(transaction);
    }

    private void validateBudget(User user, Category category, LocalDate transactionDate, BigDecimal newExpenseAmount) {
        int year = transactionDate.getYear();
        int month = transactionDate.getMonthValue();

        if (category != null) {
            budgetRepository.findByUserIdAndCategoryIdAndYearAndMonth(user.getId(), category.getId(), year, month)
                    .ifPresent(budget -> checkBudgetExceeded(user.getId(), category.getId(), transactionDate, newExpenseAmount, budget));
        }

        budgetRepository.findByUserIdAndCategoryIdAndYearAndMonth(user.getId(), null, year, month)
                .ifPresent(budget -> checkBudgetExceeded(user.getId(), null, transactionDate, newExpenseAmount, budget));
    }

    private void checkBudgetExceeded(UUID userId, UUID categoryId, LocalDate transactionDate, BigDecimal newExpenseAmount, Budget budget) {
        LocalDate startDate = YearMonth.from(transactionDate).atDay(1);
        LocalDate endDate = YearMonth.from(transactionDate).atEndOfMonth();

        BigDecimal currentExpenses = transactionRepository.sumExpensesByUserIdAndCategoryAndDateRange(
                userId, categoryId, startDate, endDate);

        BigDecimal projectedExpenses = currentExpenses.add(newExpenseAmount);

        if (projectedExpenses.compareTo(budget.getAmountLimit()) > 0) {
            String categoryName = (categoryId != null) ? budget.getCategory().getName() : "Overall";
            throw new BadRequestException(
                    "This transaction exceeds your budget for '" + categoryName + "'. " +
                            "Limit: " + budget.getAmountLimit() + ", Current Spent: " + currentExpenses +
                            ", After this transaction: " + projectedExpenses
            );
        }
    }

    private void clearReportCaches() {
        Cache cache = cacheManager.getCache("categoryBreakdown");
        if (cache != null) {
            cache.clear();
            System.out.println("--- CLEARED CATEGORY BREAKDOWN CACHE ---");
        }
    }
}