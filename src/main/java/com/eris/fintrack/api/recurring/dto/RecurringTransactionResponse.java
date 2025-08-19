package com.eris.fintrack.api.recurring.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class RecurringTransactionResponse {
    private UUID id;
    private UUID accountId;
    private String accountName;
    private UUID categoryId;
    private String categoryName;
    private String type;
    private BigDecimal amount;
    private String cronExpression;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private boolean isActive;
}