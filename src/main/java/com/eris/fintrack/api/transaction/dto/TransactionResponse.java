package com.eris.fintrack.api.transaction.dto;

import com.eris.fintrack.api.attachment.dto.AttachmentResponse;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
    private List<AttachmentResponse> attachments;
}