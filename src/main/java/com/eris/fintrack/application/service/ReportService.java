package com.eris.fintrack.application.service;

import com.eris.fintrack.api.report.dto.CategoryBreakdownResponse;
import com.eris.fintrack.api.report.dto.ReportOverviewResponse;

import java.util.List;

public interface ReportService {
    ReportOverviewResponse getMonthlyOverview(int year, int month);
    List<CategoryBreakdownResponse> getCategoryBreakdown(int year, int month);
}