package com.eris.fintrack.api.recurring.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateRecurringTransactionRequest {
    private BigDecimal amount;
    private String cronExpression;
    private LocalDate endDate;
    private String description;
    private Boolean isActive;
}