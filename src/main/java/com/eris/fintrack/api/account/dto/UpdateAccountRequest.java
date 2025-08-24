package com.eris.fintrack.api.account.dto;

import com.eris.fintrack.api.validation.EnumValidator;
import com.eris.fintrack.domain.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAccountRequest {
    @NotBlank(message = "Account name is required")
    private String name;

    @NotBlank(message = "Account type is required")
    @EnumValidator(enumClass = AccountType.class, message = "Type must be CASH, BANK, EWALLET, etc.")
    private String type;
}