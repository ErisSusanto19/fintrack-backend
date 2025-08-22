package com.eris.fintrack.application.service.implementation;

import com.eris.fintrack.api.exception.BadRequestException;
import com.eris.fintrack.api.exception.ForbiddenException;
import com.eris.fintrack.api.exception.ResourceNotFoundException;
import com.eris.fintrack.api.recurring.dto.CreateRecurringTransactionRequest;
import com.eris.fintrack.api.recurring.dto.UpdateRecurringTransactionRequest;
import com.eris.fintrack.domain.*;
import com.eris.fintrack.infrastructure.persistence.AccountRepository;
import com.eris.fintrack.infrastructure.persistence.CategoryRepository;
import com.eris.fintrack.infrastructure.persistence.RecurringTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringTransactionServiceImplTest {

    @Mock
    private RecurringTransactionRepository recurringTransactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserContextService userContextService;

    @InjectMocks
    private RecurringTransactionServiceImpl recurringTransactionService;

    private User testUser;
    private Account testAccount;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());

        testAccount = new Account();
        testAccount.setId(UUID.randomUUID());
        testAccount.setUser(testUser);

        testCategory = new Category();
        testCategory.setId(UUID.randomUUID());
        testCategory.setUser(testUser);

        when(userContextService.getCurrentUser()).thenReturn(testUser);
    }

    @Test
    @DisplayName("create should succeed with valid data and ownership")
    void create_shouldSucceed() {

        CreateRecurringTransactionRequest request = new CreateRecurringTransactionRequest();
        request.setAccountId(testAccount.getId());
        request.setCategoryId(testCategory.getId());
        request.setType("INCOME");
        request.setCronExpression("0 0 5 * * *");
        request.setAmount(new BigDecimal(500000));
        request.setDescription("INCOME Recurring Test");
        request.setStartDate(LocalDate.parse("2025-01-01"));

        when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));
        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));

        when(recurringTransactionRepository.save(any(RecurringTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        RecurringTransaction result = recurringTransactionService.create(request);

        assertNotNull(result);
        assertTrue(result.isActive());
        assertEquals(testUser, result.getUser());
        verify(recurringTransactionRepository, times(1)).save(any(RecurringTransaction.class));
    }

    @Test
    @DisplayName("create should fail if account belongs to another user")
    void create_shouldFail_whenAccountOwnershipIsInvalid() {
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        testAccount.setUser(anotherUser);

        CreateRecurringTransactionRequest request = new CreateRecurringTransactionRequest();
        request.setAccountId(testAccount.getId());
        request.setCategoryId(testCategory.getId());
        request.setType("INCOME");
        request.setCronExpression("0 0 5 * * *");
        request.setAmount(new BigDecimal(500000));
        request.setDescription("INCOME Recurring Test");
        request.setStartDate(LocalDate.parse("2025-01-01"));

        when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));
        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));

        assertThrows(ForbiddenException.class, () -> {
            recurringTransactionService.create(request);
        });
    }

    @Test
    @DisplayName("findById should return entity if user is owner")
    void findById_shouldSucceed_whenUserIsOwner() {

        UUID recurringId = UUID.randomUUID();
        RecurringTransaction recurring = new RecurringTransaction();
        recurring.setId(recurringId);
        recurring.setUser(testUser);

        when(recurringTransactionRepository.findById(recurringId)).thenReturn(Optional.of(recurring));

        RecurringTransaction result = recurringTransactionService.findById(recurringId);

        assertNotNull(result);
        assertEquals(recurringId, result.getId());
    }

    @Test
    @DisplayName("findById should fail if user is not owner")
    void findById_shouldFail_whenUserIsNotOwner() {

        UUID recurringId = UUID.randomUUID();
        RecurringTransaction recurring = new RecurringTransaction();
        recurring.setId(recurringId);

        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());

        recurring.setUser(anotherUser);

        when(recurringTransactionRepository.findById(recurringId)).thenReturn(Optional.of(recurring));

        assertThrows(ForbiddenException.class, () -> {
            recurringTransactionService.findById(recurringId);
        });
    }

    @Test
    @DisplayName("delete should call repository delete if user is owner")
    void delete_shouldSucceed() {

        UUID recurringId = UUID.randomUUID();
        RecurringTransaction recurring = new RecurringTransaction();
        recurring.setId(recurringId);
        recurring.setUser(testUser);

        when(recurringTransactionRepository.findById(recurringId)).thenReturn(Optional.of(recurring));
        doNothing().when(recurringTransactionRepository).delete(recurring);

        recurringTransactionService.delete(recurringId);

        verify(recurringTransactionRepository, times(1)).findById(recurringId);
        verify(recurringTransactionRepository, times(1)).delete(recurring);
    }

    @Test
    @DisplayName("update should modify and save the entity with valid data")
    void update_shouldSucceedWithValidData() {

        UUID recurringId = UUID.randomUUID();

        RecurringTransaction originalRecurring = RecurringTransaction.builder()
                .id(recurringId)
                .user(testUser)
                .amount(new BigDecimal("100.00"))
                .description("Original Description")
                .isActive(true)
                .cronExpression("0 0 5 * * *")
                .build();

        UpdateRecurringTransactionRequest updateRequest = new UpdateRecurringTransactionRequest();
        updateRequest.setAmount(new BigDecimal("150.00"));
        updateRequest.setDescription("Updated Description");
        updateRequest.setIsActive(false);

        when(recurringTransactionRepository.findById(recurringId)).thenReturn(Optional.of(originalRecurring));
        when(recurringTransactionRepository.save(any(RecurringTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RecurringTransaction result = recurringTransactionService.update(recurringId, updateRequest);


        assertNotNull(result);

        assertEquals(0, new BigDecimal("150.00").compareTo(result.getAmount()));
        assertEquals("Updated Description", result.getDescription());
        assertFalse(result.isActive());

        assertEquals("0 0 5 * * *", result.getCronExpression());

        verify(recurringTransactionRepository, times(1)).save(originalRecurring);
    }

    @Test
    @DisplayName("update should fail when provided with an invalid CRON expression")
    void update_shouldFailWithInvalidCronExpression() {

        UUID recurringId = UUID.randomUUID();
        RecurringTransaction originalRecurring = RecurringTransaction.builder()
                .id(recurringId)
                .user(testUser)
                .build();

        UpdateRecurringTransactionRequest updateRequest = new UpdateRecurringTransactionRequest();
        updateRequest.setCronExpression("ini pasti salah");

        when(recurringTransactionRepository.findById(recurringId)).thenReturn(Optional.of(originalRecurring));


        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            recurringTransactionService.update(recurringId, updateRequest);
        });

        assertTrue(exception.getMessage().contains("Invalid CRON expression format"));

        verify(recurringTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("findAllForCurrentUser should return a list of recurring transactions")
    void findAllForCurrentUser_shouldSucceed() {

        List<RecurringTransaction> mockList = List.of(new RecurringTransaction(), new RecurringTransaction());

        when(recurringTransactionRepository.findByUserId(testUser.getId())).thenReturn(mockList);

        List<RecurringTransaction> result = recurringTransactionService.findAllForCurrentUser();

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(recurringTransactionRepository, times(1)).findByUserId(testUser.getId());
    }
}