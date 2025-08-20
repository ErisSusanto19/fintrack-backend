package com.eris.fintrack.application.service.implementation;

import com.eris.fintrack.api.report.dto.CashFlowTrendItem;
import com.eris.fintrack.api.report.dto.CategoryBreakdownResponse;
import com.eris.fintrack.api.report.dto.CategoryBreakdownRow;
import com.eris.fintrack.api.report.dto.ReportOverviewResponse;
import com.eris.fintrack.application.service.ReportService;
import com.eris.fintrack.domain.User;
import com.eris.fintrack.domain.enums.TransactionType;
import com.eris.fintrack.infrastructure.persistence.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TransactionRepository transactionRepository;
    private final UserContextService userContextService;
    private final CacheManager cacheManager;

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

        String cacheKey = currentUser.getId().toString() + "_" + year + "-" + month;

        Cache cache = cacheManager.getCache("categoryBreakdown");

        if (cache != null && cache.get(cacheKey) != null) {
            System.out.println("--- FETCHING CATEGORY BREAKDOWN FROM CACHE ---");
            return (List<CategoryBreakdownResponse>) cache.get(cacheKey).get();
        }

        System.out.println("--- EXECUTING HEAVY DATABASE QUERY FOR CATEGORY BREAKDOWN ---");

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

        List<CategoryBreakdownResponse> response = rows.stream().map(row -> {
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

        if (cache != null) {
            cache.put(cacheKey, response);
        }

        return response;
    }

    @Override
    public List<CashFlowTrendItem> getCashFlowTrend(LocalDate startDate, LocalDate endDate) {
        User currentUser = userContextService.getCurrentUser();

        List<CashFlowTrendItem> resultsFromDb = transactionRepository.getCashFlowTrend(
                currentUser.getId(), startDate, endDate);

        Map<LocalDate, CashFlowTrendItem> resultsMap = resultsFromDb.stream()
                .collect(Collectors.toMap(CashFlowTrendItem::date, item -> item));

        return startDate.datesUntil(endDate.plusDays(1))
                .map(date -> resultsMap.getOrDefault(date,
                        new CashFlowTrendItem(date, BigDecimal.ZERO, BigDecimal.ZERO)))
                .collect(Collectors.toList());
    }

}