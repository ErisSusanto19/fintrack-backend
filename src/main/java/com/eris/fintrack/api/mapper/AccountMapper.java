package com.eris.fintrack.api.mapper;

import com.eris.fintrack.api.account.dto.AccountResponse;
import com.eris.fintrack.domain.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "type", expression = "java(account.getType().name())")
    AccountResponse toDto(Account account);

}