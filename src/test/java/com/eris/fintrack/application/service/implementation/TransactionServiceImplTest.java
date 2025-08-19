package com.eris.fintrack.application.service.implementation;

import com.eris.fintrack.api.exception.BadRequestException;
import com.eris.fintrack.api.exception.ForbiddenException;
import com.eris.fintrack.api.exception.ResourceNotFoundException;
import com.eris.fintrack.api.transaction.dto.CreateTransactionRequest;
import com.eris.fintrack.api.transaction.dto.UpdateTransactionRequest;
import com.eris.fintrack.domain.Account;
import com.eris.fintrack.domain.Category;
import com.eris.fintrack.domain.Transaction;
import com.eris.fintrack.domain.User;
import com.eris.fintrack.domain.enums.TransactionType;
import com.eris.fintrack.infrastructure.persistence.AccountRepository;
import com.eris.fintrack.infrastructure.persistence.BudgetRepository;
import com.eris.fintrack.infrastructure.persistence.CategoryRepository;
import com.eris.fintrack.infrastructure.persistence.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BudgetRepository budgetRepository;
    @Mock
    private UserContextService userContextService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User testUser;
    private Account testAccount;
    private Category testCategory;
    private CreateTransactionRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());

        testAccount = new Account();
        testAccount.setId(UUID.randomUUID());
        testAccount.setUser(testUser);
        testAccount.setBalance(new BigDecimal("1000.00"));

        testCategory = new Category();
        testCategory.setId(UUID.randomUUID());
        testCategory.setUser(testUser);

        createRequest = new CreateTransactionRequest();
        createRequest.setAccountId(testAccount.getId());
        createRequest.setCategoryId(testCategory.getId());
        createRequest.setAmount(new BigDecimal("100.00"));
        createRequest.setTransactionDate(LocalDate.now());
        createRequest.setType("EXPENSE");

        when(userContextService.getCurrentUser()).thenReturn(testUser);
    }

    @Test
    @DisplayName("createTransaction should create an expense and decrease account balance")
    void createTransaction_shouldSucceedForExpense() {
        when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));
        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
        when(budgetRepository.findByUserIdAndCategoryIdAndYearAndMonth(any(), any(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Transaction result = transactionService.createTransaction(createRequest);

        assertNotNull(result);
        assertEquals(testAccount, result.getAccount());
        assertEquals(testCategory, result.getCategory());

        assertEquals(0, new BigDecimal("900.00").compareTo(testAccount.getBalance()));

        verify(accountRepository, times(1)).save(testAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("createTransaction should fail if budget is exceeded")
    void createTransaction_shouldFailWhenBudgetExceeded() {

        when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));
        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));

        com.eris.fintrack.domain.Budget mockBudget = new com.eris.fintrack.domain.Budget();
        mockBudget.setAmountLimit(new BigDecimal("50.00")); // Limit 50
        mockBudget.setCategory(testCategory);
        when(budgetRepository.findByUserIdAndCategoryIdAndYearAndMonth(any(), any(), anyInt(), anyInt()))
                .thenReturn(Optional.of(mockBudget));

        when(transactionRepository.sumExpensesByUserIdAndCategoryAndDateRange(any(), any(), any(), any()))
                .thenReturn(new BigDecimal("10.00"));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            transactionService.createTransaction(createRequest);
        });

        assertTrue(exception.getMessage().contains("exceeds your budget"));

        assertEquals(0, new BigDecimal("1000.00").compareTo(testAccount.getBalance()));
        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTransaction should fail if account does not belong to user")
    void createTransaction_shouldFailForMismatchedOwnership() {
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        testAccount.setUser(anotherUser);

        when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));

        assertThrows(ForbiddenException.class, () -> {
            transactionService.createTransaction(createRequest);
        });
    }

    @Test
    @DisplayName("deleteTransaction should succeed and restore account balance")
    void deleteTransaction_shouldSucceedAndRestoreBalance() {

        Transaction expenseTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .account(testAccount) // Saldo awal 1000
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("100.00"))
                .build();

        testAccount.setBalance(new BigDecimal("900.00"));

        when(transactionRepository.findById(expenseTransaction.getId())).thenReturn(Optional.of(expenseTransaction));

        transactionService.deleteTransaction(expenseTransaction.getId());

        assertEquals(0, new BigDecimal("1000.00").compareTo(testAccount.getBalance()));

        verify(transactionRepository, times(1)).delete(expenseTransaction);
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    @DisplayName("deleteTransaction should fail for non-existent transaction")
    void deleteTransaction_shouldFailForNonExistentTransaction() {

        UUID nonExistentId = UUID.randomUUID();
        when(transactionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.deleteTransaction(nonExistentId);
        });
    }

    @Test
    @DisplayName("updateTransaction should succeed for amount change in the same account")
    void updateTransaction_shouldSucceedForAmountChange() {
        Transaction originalTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .account(testAccount)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDate.now())
                .build();

        when(transactionRepository.findById(originalTransaction.getId())).thenReturn(Optional.of(originalTransaction));

        UpdateTransactionRequest updateRequest = new UpdateTransactionRequest();
        updateRequest.setAccountId(testAccount.getId());
        updateRequest.setAmount(new BigDecimal("150.00"));
        updateRequest.setType("EXPENSE");
        updateRequest.setTransactionDate(LocalDate.now());

        transactionService.updateTransaction(originalTransaction.getId(), updateRequest);

        assertEquals(0, new BigDecimal("950.00").compareTo(testAccount.getBalance()));

        assertEquals(0, new BigDecimal("150.00").compareTo(originalTransaction.getAmount()));

        verify(transactionRepository, times(1)).save(originalTransaction);
        verify(accountRepository, times(1)).save(testAccount);
    }
}