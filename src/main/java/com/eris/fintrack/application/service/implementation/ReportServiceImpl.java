package com.eris.fintrack.application.service.implementation;

import com.eris.fintrack.api.report.dto.CategoryBreakdownResponse;
import com.eris.fintrack.api.report.dto.CategoryBreakdownRow;
import com.eris.fintrack.api.report.dto.ReportOverviewResponse;
import com.eris.fintrack.application.service.ReportService;
import com.eris.fintrack.domain.User;
import com.eris.fintrack.domain.enums.TransactionType;
import com.eris.fintrack.infrastructure.persistence.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TransactionRepository transactionRepository;
    private final UserContextService userContextService;

    @Override
    public ReportOverviewResponse getMonthlyOverview(int year, int month) {
        User currentUser = userContextService.getCurrentUser();

        LocalDate startDate = YearMonth.of(year, month).atDay(1);
        LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();

        BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                currentUser.getId(),
                TransactionType.INCOME,
                startDate,
                endDate
        );

        BigDecimal totalExpense = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                currentUser.getId(),
                TransactionType.EXPENSE,
                startDate,
                endDate
        );

        BigDecimal netCashFlow = totalIncome.subtract(totalExpense);

        return ReportOverviewResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netCashFlow(netCashFlow)
                .build();
    }

    @Override
    public List<CategoryBreakdownResponse> getCategoryBreakdown(int year, int month) {
        User currentUser = userContextService.getCurrentUser();
        LocalDate startDate = YearMonth.of(year, month).atDay(1);
        LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();

        List<CategoryBreakdownRow> rows = transactionRepository.getCategoryBreakdown(
                currentUser.getId(), startDate, endDate);

        if (rows.isEmpty()) {
            return Collections.emptyList();
        }

        BigDecimal totalExpense = rows.stream()
                .map(CategoryBreakdownRow::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalExpense.compareTo(BigDecimal.ZERO) == 0) {
            return Collections.emptyList();
        }

        return rows.stream().map(row -> {
            BigDecimal percentage = row.totalAmount()
                    .multiply(new BigDecimal("100"))
                    .divide(totalExpense, 2, RoundingMode.HALF_UP);

            return CategoryBreakdownResponse.builder()
                    .categoryId(row.categoryId())
                    .categoryName(row.categoryName())
                    .totalAmount(row.totalAmount())
                    .percentage(percentage.doubleValue())
                    .build();
        }).collect(Collectors.toList());
    }

}