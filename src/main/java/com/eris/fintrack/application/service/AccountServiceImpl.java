package com.eris.fintrack.application.service;

import com.eris.fintrack.api.account.dto.CreateUpdateAccountRequest;
import com.eris.fintrack.domain.Account;
import com.eris.fintrack.domain.User;
import com.eris.fintrack.infrastructure.persistence.AccountRepository;
import com.eris.fintrack.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserContextService userContextService;

    @Override
    @Transactional
    public Account createAccount(CreateUpdateAccountRequest request) {
        User currentUser = userContextService.getCurrentUser();

        Account account = Account.builder()
                .user(currentUser)
                .name(request.getName())
                .type(request.getType())
                .balance(request.getBalance())
                .build();

        return accountRepository.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAllAccountsForCurrentUser() {
        User currentUser = userContextService.getCurrentUser();
        return accountRepository.findByUserId(currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccountById(UUID accountId) {
        User currentUser = userContextService.getCurrentUser();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Access Denied: You do not own this account");
        }
        return account;
    }

    @Override
    @Transactional
    public void deleteAccountById(UUID accountId) {
        Account accountToDelete = getAccountById(accountId);
        accountRepository.delete(accountToDelete);
    }
}