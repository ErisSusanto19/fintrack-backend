package com.eris.fintrack.api.mapper;

import com.eris.fintrack.api.recurring.dto.RecurringTransactionResponse;
import com.eris.fintrack.domain.RecurringTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecurringTransactionMapper {

    @Mapping(source = "account.id", target = "accountId")
    @Mapping(source = "account.name", target = "accountName")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "type", target = "type")
    RecurringTransactionResponse toDto(RecurringTransaction recurringTransaction);
}