package com.eris.fintrack.api.account;

import com.eris.fintrack.api.account.dto.AccountResponse;
import com.eris.fintrack.api.account.dto.CreateAccountRequest;
import com.eris.fintrack.api.account.dto.UpdateAccountRequest;
import com.eris.fintrack.api.common.ApiResponse;
import com.eris.fintrack.api.mapper.AccountMapper;
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
    private final AccountMapper accountMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Account createdAccount = accountService.createAccount(request);
        AccountResponse responseDto = accountMapper.toDto(createdAccount);
        return new ResponseEntity<>(ApiResponse.success(responseDto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts() {
        List<Account> accounts = accountService.getAllAccountsForCurrentUser();
        List<AccountResponse> response = accounts.stream()
                .map(accountMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(@PathVariable UUID id) {
        Account account = accountService.getAccountById(id);
        return ResponseEntity.ok(ApiResponse.success(accountMapper.toDto(account)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable UUID id) {
        accountService.deleteAccountById(id);
        return new ResponseEntity<>(ApiResponse.success(null), HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountRequest request) {

        Account updatedAccount = accountService.updateAccount(id, request);
        return ResponseEntity.ok(ApiResponse.success(accountMapper.toDto(updatedAccount)));
    }
}