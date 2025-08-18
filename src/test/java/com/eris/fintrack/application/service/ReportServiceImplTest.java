package com.eris.fintrack.application.service;

import com.eris.fintrack.api.report.dto.CategoryBreakdownResponse;
import com.eris.fintrack.api.report.dto.CategoryBreakdownRow;
import com.eris.fintrack.api.report.dto.ReportOverviewResponse;
import com.eris.fintrack.application.service.implementation.ReportServiceImpl;
import com.eris.fintrack.application.service.implementation.UserContextService;
import com.eris.fintrack.domain.User;
import com.eris.fintrack.domain.enums.TransactionType;
import com.eris.fintrack.infrastructure.persistence.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserContextService userContextService;

    @InjectMocks
    private ReportServiceImpl reportService;

    private User testUser;
    private int year;
    private int month;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());

        year = 2025;
        month = 8;
        startDate = YearMonth.of(year, month).atDay(1);
        endDate = YearMonth.of(year, month).atEndOfMonth();

        when(userContextService.getCurrentUser()).thenReturn(testUser);
    }

    @Test
    @DisplayName("getMonthlyOverview should return correct totals and cashflow")
    void getMonthlyOverview_ShouldReturnCorrectCalculations() {

        BigDecimal totalIncome = new BigDecimal("5000.00");
        BigDecimal totalExpense = new BigDecimal("1500.00");

        when(transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                eq(testUser.getId()), eq(TransactionType.INCOME), eq(startDate), eq(endDate)))
                .thenReturn(totalIncome);

        when(transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                eq(testUser.getId()), eq(TransactionType.EXPENSE), eq(startDate), eq(endDate)))
                .thenReturn(totalExpense);

        ReportOverviewResponse result = reportService.getMonthlyOverview(year, month);

        assertNotNull(result);
        assertEquals(0, totalIncome.compareTo(result.getTotalIncome()));
        assertEquals(0, totalExpense.compareTo(result.getTotalExpense()));

        BigDecimal expectedCashFlow = new BigDecimal("3500.00");
        assertEquals(0, expectedCashFlow.compareTo(result.getNetCashFlow()));
    }

    @Test
    @DisplayName("getMonthlyOverview should return all zeros when no transactions exist")
    void getMonthlyOverview_ShouldReturnZerosWhenNoData() {
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                any(UUID.class), any(TransactionType.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

        ReportOverviewResponse result = reportService.getMonthlyOverview(year, month);

        assertNotNull(result);
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalIncome()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalExpense()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getNetCashFlow()));
    }

    @Test
    void getCategoryBreakdown_ShouldReturnCorrectlyCalculatedPercentages() {
        List<CategoryBreakdownRow> mockRows = List.of(
                new CategoryBreakdownRow(UUID.randomUUID(), "Makanan", new BigDecimal("800.00")),
                new CategoryBreakdownRow(UUID.randomUUID(), "Transportasi", new BigDecimal("200.00"))
        );

        when(transactionRepository.getCategoryBreakdown(eq(testUser.getId()), eq(startDate), eq(endDate)))
                .thenReturn(mockRows);

        List<CategoryBreakdownResponse> result = reportService.getCategoryBreakdown(year, month);

        assertNotNull(result);
        assertEquals(2, result.size());

        CategoryBreakdownResponse foodCategory = result.get(0);
        assertEquals("Makanan", foodCategory.getCategoryName());
        assertEquals(0, new BigDecimal("800.00").compareTo(foodCategory.getTotalAmount())); // Cara aman membandingkan BigDecimal
        assertEquals(80.0, foodCategory.getPercentage());

        CategoryBreakdownResponse transportCategory = result.get(1);
        assertEquals("Transportasi", transportCategory.getCategoryName());
        assertEquals(0, new BigDecimal("200.00").compareTo(transportCategory.getTotalAmount()));
        assertEquals(20.0, transportCategory.getPercentage());
    }

    @Test
    void getCategoryBreakdown_ShouldReturnEmptyList_WhenNoTransactionsExist() {
        when(transactionRepository.getCategoryBreakdown(any(UUID.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        List<CategoryBreakdownResponse> result = reportService.getCategoryBreakdown(year, month);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}