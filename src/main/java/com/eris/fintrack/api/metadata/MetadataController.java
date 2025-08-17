package com.eris.fintrack.api.metadata;

import com.eris.fintrack.domain.enums.AccountType;
import com.eris.fintrack.domain.enums.TransactionType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/metadata")
public class MetadataController {

    @GetMapping("/transaction-types")
    public ResponseEntity<List<String>> getTransactionTypes() {
        List<String> types = Arrays.stream(TransactionType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(types);
    }

    @GetMapping("/account-types")
    public ResponseEntity<List<String>> getAccountTypes() {
        List<String> types = Arrays.stream(AccountType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(types);
    }
}