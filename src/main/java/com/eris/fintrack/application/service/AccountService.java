package com.eris.fintrack.application.service;

import com.eris.fintrack.api.account.dto.AccountResponse;
import com.eris.fintrack.api.account.dto.CreateUpdateAccountRequest;
import com.eris.fintrack.domain.Account;

import java.util.List;
import java.util.UUID;

public interface AccountService {
    Account createAccount(CreateUpdateAccountRequest request);
    List<Account> getAllAccountsForCurrentUser();
    Account getAccountById(UUID accountId);
    void deleteAccountById(UUID accountId);
}