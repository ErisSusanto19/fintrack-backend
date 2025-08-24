package com.eris.fintrack.application.service;

import com.eris.fintrack.api.account.dto.CreateAccountRequest;
import com.eris.fintrack.api.account.dto.UpdateAccountRequest;
import com.eris.fintrack.domain.Account;

import java.util.List;
import java.util.UUID;

public interface AccountService {
    Account createAccount(CreateAccountRequest request);
    List<Account> getAllAccountsForCurrentUser();
    Account getAccountById(UUID accountId);
    void deleteAccountById(UUID accountId);
    Account updateAccount(UUID accountId, UpdateAccountRequest request);
}