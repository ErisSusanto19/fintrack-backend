package com.eris.fintrack.api.report.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryBreakdownRow(
        UUID categoryId,
        String categoryName,
        BigDecimal totalAmount
) {}