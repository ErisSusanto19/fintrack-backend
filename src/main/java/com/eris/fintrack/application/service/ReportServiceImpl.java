package com.eris.fintrack.application.service;

import com.eris.fintrack.api.report.dto.ReportOverviewResponse;
import com.eris.fintrack.domain.User;
import com.eris.fintrack.domain.enums.TransactionType;
import com.eris.fintrack.infrastructure.persistence.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

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
}