package com.eris.fintrack.api.account.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class AccountResponse {
    private UUID id;
    private String name;
    private String type;
    private BigDecimal balance;
}