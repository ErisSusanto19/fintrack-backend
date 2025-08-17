package com.eris.fintrack.api.transaction.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class TransactionResponse {
    private UUID id;
    private UUID accountId;
    private String accountName;
    private UUID categoryId;
    private String categoryName;
    private String type;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String description;
}