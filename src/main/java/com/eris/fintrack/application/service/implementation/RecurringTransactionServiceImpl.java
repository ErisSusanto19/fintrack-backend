package com.eris.fintrack.application.service.implementation;

import com.eris.fintrack.api.exception.BadRequestException;
import com.eris.fintrack.api.exception.ForbiddenException;
import com.eris.fintrack.api.exception.ResourceNotFoundException;
import com.eris.fintrack.api.recurring.dto.CreateRecurringTransactionRequest;
import com.eris.fintrack.api.recurring.dto.UpdateRecurringTransactionRequest;
import com.eris.fintrack.application.service.RecurringTransactionService;
import com.eris.fintrack.domain.*;
import com.eris.fintrack.domain.enums.TransactionType;
import com.eris.fintrack.infrastructure.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecurringTransactionServiceImpl implements RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserContextService userContextService;

    @Override
    @Transactional
    public RecurringTransaction create(CreateRecurringTransactionRequest request) {
        User currentUser = userContextService.getCurrentUser();

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!account.getUser().getId().equals(currentUser.getId()) || !category.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Account or Category does not belong to the user");
        }

        RecurringTransaction recurring = RecurringTransaction.builder()
                .user(currentUser)
                .account(account)
                .category(category)
                .type(TransactionType.valueOf(request.getType().toUpperCase()))
                .amount(request.getAmount())
                .cronExpression(request.getCronExpression())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .description(request.getDescription())
                .isActive(true)
                .build();

        return recurringTransactionRepository.save(recurring);
    }

    @Override
    public List<RecurringTransaction> findAllForCurrentUser() {
        User currentUser = userContextService.getCurrentUser();
        return recurringTransactionRepository.findByUserId(currentUser.getId());
    }

    @Override
    public RecurringTransaction findById(UUID id) {
        User currentUser = userContextService.getCurrentUser();
        RecurringTransaction recurring = recurringTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));
        if (!recurring.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access Denied");
        }
        return recurring;
    }

    @Override
    @Transactional
    public RecurringTransaction update(UUID id, UpdateRecurringTransactionRequest request) {
        RecurringTransaction recurring = findById(id);

        if (request.getAmount() != null) recurring.setAmount(request.getAmount());
        if (request.getCronExpression() != null) {
            try {
                CronExpression.parse(request.getCronExpression());
                recurring.setCronExpression(request.getCronExpression());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid CRON expression format: " + e.getMessage());
            }
        }
        if (request.getEndDate() != null) recurring.setEndDate(request.getEndDate());
        if (request.getDescription() != null) recurring.setDescription(request.getDescription());
        if (request.getIsActive() != null) recurring.setActive(request.getIsActive());

        return recurringTransactionRepository.save(recurring);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        RecurringTransaction recurring = findById(id);
        recurringTransactionRepository.delete(recurring);
    }
}