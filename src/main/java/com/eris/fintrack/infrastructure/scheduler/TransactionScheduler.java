package com.eris.fintrack.infrastructure.scheduler;

import com.eris.fintrack.api.transaction.dto.CreateTransactionRequest;
import com.eris.fintrack.application.service.TransactionService;
import com.eris.fintrack.domain.RecurringTransaction;
import com.eris.fintrack.domain.User;
import com.eris.fintrack.infrastructure.persistence.RecurringTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionScheduler {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final TransactionService transactionService;

    // Format CRON: detik menit jam hari(bulan) bulan hari(minggu)
    @Scheduled(cron = "0 0 2 * * *")
    public void processRecurringTransactions() {
        log.info("Starting recurring transaction processing job...");
        LocalDate today = LocalDate.now();

        List<RecurringTransaction> candidates = recurringTransactionRepository
                .findAllActiveWithDetails(today);

        for (RecurringTransaction recurring : candidates) {
            processSingleRecurring(recurring, today);
        }
        log.info("Recurring transaction processing job finished.");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleRecurring(RecurringTransaction recurring, LocalDate today) {
        try {
            if (recurring.getEndDate() != null && today.isAfter(recurring.getEndDate())) {
                recurring.setActive(false);
                recurringTransactionRepository.save(recurring);
                log.info("Deactivating expired recurring transaction: {}", recurring.getId());
                return;
            }

            if (recurring.getLastExecutionDate() != null && recurring.getLastExecutionDate().isEqual(today)) {
                log.debug("Skipping recurring transaction {} as it has already run today.", recurring.getId());
                return;
            }

            CronExpression cron = CronExpression.parse(recurring.getCronExpression());

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime referencePoint = (recurring.getLastExecutionDate() != null)
                    ? recurring.getLastExecutionDate().atStartOfDay()
                    : recurring.getStartDate().minusDays(1).atStartOfDay();

            LocalDateTime nextExecutionTime = cron.next(referencePoint);

            if (nextExecutionTime == null || now.isBefore(nextExecutionTime)) {
                log.debug("Skipping recurring transaction {}. Next run is at {}", recurring.getId(), nextExecutionTime);
                return;
            }

            log.info("Processing recurring transaction: {}", recurring.getId());

            CreateTransactionRequest newTransaction = new CreateTransactionRequest();
            newTransaction.setAccountId(recurring.getAccount().getId());
            newTransaction.setCategoryId(recurring.getCategory().getId());
            newTransaction.setType(recurring.getType().name());
            newTransaction.setAmount(recurring.getAmount());
            newTransaction.setTransactionDate(today);
            newTransaction.setDescription(recurring.getDescription());

            User user = recurring.getUser();

            transactionService.createTransactionFromScheduler(newTransaction, user);

            recurring.setLastExecutionDate(now.toLocalDate());
            recurringTransactionRepository.save(recurring);

            log.info("Successfully created transaction for recurring ID: {}", recurring.getId());

        } catch (Exception e) {
            log.error("Failed to process recurring ID: {}. Error: {}", recurring.getId(), e.getMessage());
        }
    }
}