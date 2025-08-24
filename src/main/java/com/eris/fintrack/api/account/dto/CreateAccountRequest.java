package com.eris.fintrack.api.account.dto;

import com.eris.fintrack.api.validation.EnumValidator;
import com.eris.fintrack.domain.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class CreateAccountRequest {
    @NotBlank(message = "Account name is required")
    private String name;

    @NotBlank(message = "Transaction type is required")
    @EnumValidator(enumClass = AccountType.class, message = "Invalid type. Allowed values: {enumValues}")
    private String type;

    @NotNull(message = "Initial balance is required")
    @PositiveOrZero(message = "Balance cannot be negative")
    private BigDecimal balance;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}