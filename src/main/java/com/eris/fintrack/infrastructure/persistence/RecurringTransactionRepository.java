package com.eris.fintrack.infrastructure.persistence;

import com.eris.fintrack.domain.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, UUID> {
    List<RecurringTransaction> findByUserId(UUID userId);
    List<RecurringTransaction> findAllByIsActiveTrueAndStartDateLessThanEqual(LocalDate date);
    @Query("SELECT rt FROM RecurringTransaction rt " +
            "JOIN FETCH rt.user " +
            "JOIN FETCH rt.account " +
            "JOIN FETCH rt.category " +
            "WHERE rt.isActive = true AND rt.startDate <= :today")
    List<RecurringTransaction> findAllActiveWithDetails(LocalDate today);
}