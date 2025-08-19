package com.eris.fintrack.api.recurring.dto;

import com.eris.fintrack.api.validation.EnumValidator;
import com.eris.fintrack.domain.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateRecurringTransactionRequest {
    @NotNull(message = "Account ID is required")
    private UUID accountId;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotBlank(message = "Type is required")
    @EnumValidator(enumClass = TransactionType.class, message = "Type must be INCOME or EXPENSE")
    private String type;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "CRON expression for schedule is required")
    private String cronExpression;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;
    private String description;
}