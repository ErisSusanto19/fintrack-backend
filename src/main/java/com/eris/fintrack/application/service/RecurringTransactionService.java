package com.eris.fintrack.application.service;

import com.eris.fintrack.api.recurring.dto.CreateRecurringTransactionRequest;
import com.eris.fintrack.api.recurring.dto.RecurringTransactionResponse;
import com.eris.fintrack.api.recurring.dto.UpdateRecurringTransactionRequest;
import com.eris.fintrack.domain.RecurringTransaction;
import java.util.List;
import java.util.UUID;

public interface RecurringTransactionService {
    RecurringTransaction create(CreateRecurringTransactionRequest request);
    List<RecurringTransactionResponse> findAllForCurrentUser();
    RecurringTransaction findById(UUID id);
    RecurringTransactionResponse findCompleteRecurringById(UUID id);
    RecurringTransaction update(UUID id, UpdateRecurringTransactionRequest request);
    void delete(UUID id);
}