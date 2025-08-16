package com.eris.fintrack.api.account;

import com.eris.fintrack.api.account.dto.AccountResponse;
import com.eris.fintrack.api.account.dto.CreateUpdateAccountRequest;
import com.eris.fintrack.application.service.AccountService;
import com.eris.fintrack.domain.Account;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateUpdateAccountRequest request) {
        Account createdAccount = accountService.createAccount(request);
        return new ResponseEntity<>(mapToResponse(createdAccount), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        List<Account> accounts = accountService.getAllAccountsForCurrentUser();
        List<AccountResponse> response = accounts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable UUID id) {
        Account account = accountService.getAccountById(id);
        return ResponseEntity.ok(mapToResponse(account));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID id) {
        accountService.deleteAccountById(id);
        return ResponseEntity.noContent().build();
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .balance(account.getBalance())
                .build();
    }
}