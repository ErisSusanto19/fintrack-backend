package com.eris.fintrack.infrastructure.persistence;

import com.eris.fintrack.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findByUserIdAndYearAndMonth(UUID userId, int year, int month);

    Optional<Budget> findByUserIdAndCategoryIdAndYearAndMonth(UUID userId, UUID categoryId, int year, int month);
}