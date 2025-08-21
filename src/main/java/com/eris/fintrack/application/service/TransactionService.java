package com.eris.fintrack.application.service;

import com.eris.fintrack.api.transaction.dto.CreateTransactionRequest;
import com.eris.fintrack.api.transaction.dto.CreateTransferRequest;
import com.eris.fintrack.api.transaction.dto.TransactionResponse;
import com.eris.fintrack.api.transaction.dto.UpdateTransactionRequest;
import com.eris.fintrack.domain.Attachment;
import com.eris.fintrack.domain.Transaction;
import com.eris.fintrack.domain.User;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface TransactionService {
    Transaction createTransaction(CreateTransactionRequest request);
    Transaction createTransactionFromScheduler(CreateTransactionRequest request, User user);
    Transaction updateTransaction(UUID transactionId, UpdateTransactionRequest request);
    Page<Transaction> getTransactionsForCurrentUser(Pageable pageable);
    Transaction findById(UUID transactionId);
    void deleteTransaction(UUID transactionId);
    void createTransfer(CreateTransferRequest request);
    Attachment addAttachmentToTransaction(UUID transactionId, MultipartFile file);
    Resource getAttachmentResource(UUID transactionId, UUID attachmentId);
    Attachment getAttachmentMetadata(UUID transactionId, UUID attachmentId);
    void deleteAttachment(UUID transactionId, UUID attachmentId);
}