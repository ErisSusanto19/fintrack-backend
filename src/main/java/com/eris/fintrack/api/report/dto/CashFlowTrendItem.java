package com.eris.fintrack.api.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CashFlowTrendItem(
        LocalDate date,
        BigDecimal income,
        BigDecimal expense
) {}