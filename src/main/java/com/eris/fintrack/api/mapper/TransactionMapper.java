package com.eris.fintrack.api.mapper;

import com.eris.fintrack.api.transaction.dto.TransactionResponse;
import com.eris.fintrack.domain.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {AttachmentMapper.class})
public interface TransactionMapper {

    @Mapping(source = "account.id", target = "accountId")
    @Mapping(source = "account.name", target = "accountName")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(target = "type", expression = "java(transaction.getType().name())")

    TransactionResponse toDto(Transaction transaction);
}