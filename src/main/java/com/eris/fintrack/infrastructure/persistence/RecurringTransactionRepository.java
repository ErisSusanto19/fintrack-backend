package com.eris.fintrack.infrastructure.persistence;

import com.eris.fintrack.domain.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, UUID> {
    List<RecurringTransaction> findByUserId(UUID userId);
    List<RecurringTransaction> findAllByIsActiveTrueAndStartDateLessThanEqual(LocalDate date);
}