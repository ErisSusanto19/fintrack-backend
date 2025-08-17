package com.eris.fintrack.application.service;

import com.eris.fintrack.api.report.dto.ReportOverviewResponse;

public interface ReportService {
    ReportOverviewResponse getMonthlyOverview(int year, int month);
}