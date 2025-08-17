package com.eris.fintrack.api.transaction.dto;

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
public class CreateTransactionRequest {
    @NotNull(message = "Account ID is required")
    private UUID accountId;

    private UUID categoryId;

    @NotBlank(message = "Transaction type is required")
    @EnumValidator(enumClass = TransactionType.class, message = "Type must be INCOME or EXPENSE")
    private String type;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

    private String description;
}