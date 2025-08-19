package com.eris.fintrack.api.report;

import com.eris.fintrack.api.common.ApiResponse;
import com.eris.fintrack.api.report.dto.CashFlowTrendItem;
import com.eris.fintrack.api.report.dto.CategoryBreakdownResponse;
import com.eris.fintrack.api.report.dto.ReportOverviewResponse;
import com.eris.fintrack.application.service.ReportService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Validated
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<ReportOverviewResponse>> getMonthlyOverview(
            @RequestParam int year,
            @RequestParam @Min(1) @Max(12) int month) {

        ReportOverviewResponse overview = reportService.getMonthlyOverview(year, month);
        return ResponseEntity.ok(ApiResponse.success(overview));
    }

    @GetMapping("/category-breakdown")
    public ResponseEntity<ApiResponse<List<CategoryBreakdownResponse>>> getCategoryBreakdown(
            @RequestParam int year,
            @RequestParam @Min(1) @Max(12) int month) {

        List<CategoryBreakdownResponse> breakdown = reportService.getCategoryBreakdown(year, month);
        return ResponseEntity.ok(ApiResponse.success(breakdown));
    }

    @GetMapping("/cashflow-trend")
    public ResponseEntity<ApiResponse<List<CashFlowTrendItem>>> getCashFlowTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<CashFlowTrendItem> trendData = reportService.getCashFlowTrend(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(trendData));
    }
}