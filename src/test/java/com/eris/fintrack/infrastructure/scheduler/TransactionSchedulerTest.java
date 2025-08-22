package com.eris.fintrack.infrastructure.scheduler;

import com.eris.fintrack.api.transaction.dto.CreateTransactionRequest;
import com.eris.fintrack.application.service.TransactionService;
import com.eris.fintrack.domain.Account;
import com.eris.fintrack.domain.Category;
import com.eris.fintrack.domain.RecurringTransaction;
import com.eris.fintrack.domain.User;
import com.eris.fintrack.domain.enums.TransactionType;
import com.eris.fintrack.infrastructure.persistence.RecurringTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionSchedulerTest {

    @Mock
    private RecurringTransactionRepository recurringTransactionRepository;
    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionScheduler transactionScheduler;

    private RecurringTransaction recurring;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.of(2025, 8, 25);

        User testUser = new User();
        Account testAccount = new Account();
        Category testCategory = new Category();

        recurring = RecurringTransaction.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .account(testAccount)
                .category(testCategory)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("100"))
                .cronExpression("0 0 5 25 * *")
                .startDate(LocalDate.of(2025, 1, 1))
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Scheduler should create transaction on a valid scheduled day")
    void processSingleRecurring_shouldCreateTransaction_onValidDay() {

        transactionScheduler.processSingleRecurring(recurring, today);
        ArgumentCaptor<CreateTransactionRequest> requestCaptor = ArgumentCaptor.forClass(CreateTransactionRequest.class);

        verify(transactionService, times(1)).createTransactionFromScheduler(any(), any());
        verify(transactionService, times(1)).createTransactionFromScheduler(requestCaptor.capture(), any(User.class));

        CreateTransactionRequest capturedRequest = requestCaptor.getValue();

        assertNotNull(capturedRequest);
        assertEquals(recurring.getAccount().getId(), capturedRequest.getAccountId());
        assertEquals(recurring.getAmount(), capturedRequest.getAmount());
        assertEquals(today, capturedRequest.getTransactionDate());

        assertEquals(today, recurring.getLastExecutionDate());
        verify(recurringTransactionRepository, times(1)).save(recurring);
    }

    @Test
    @DisplayName("Scheduler should skip if today is not the scheduled day")
    void processSingleRecurring_shouldSkip_whenNotScheduledDay() {

        LocalDate notScheduledDay = LocalDate.of(2025, 8, 26);

        transactionScheduler.processSingleRecurring(recurring, notScheduledDay);

        verify(transactionService, never()).createTransactionFromScheduler(any(), any());
        verify(recurringTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Scheduler should skip if transaction has already been created today")
    void processSingleRecurring_shouldSkip_whenAlreadyRunToday() {

        recurring.setLastExecutionDate(today);

        transactionScheduler.processSingleRecurring(recurring, today);

        verify(transactionService, never()).createTransactionFromScheduler(any(), any());
        verify(recurringTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Scheduler should deactivate recurring transaction if it is past its end date")
    void processSingleRecurring_shouldDeactivate_whenPastEndDate() {

        recurring.setEndDate(today.minusDays(1));

        transactionScheduler.processSingleRecurring(recurring, today);

        assertFalse(recurring.isActive());

        verify(recurringTransactionRepository, times(1)).save(recurring);

        verify(transactionService, never()).createTransactionFromScheduler(any(), any());
    }

}