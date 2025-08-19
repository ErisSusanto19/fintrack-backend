package com.eris.fintrack.application.service;

import com.eris.fintrack.api.report.dto.CashFlowTrendItem;
import com.eris.fintrack.api.report.dto.CategoryBreakdownResponse;
import com.eris.fintrack.api.report.dto.ReportOverviewResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    ReportOverviewResponse getMonthlyOverview(int year, int month);
    List<CategoryBreakdownResponse> getCategoryBreakdown(int year, int month);
    List<CashFlowTrendItem> getCashFlowTrend(LocalDate startDate, LocalDate endDate);
}