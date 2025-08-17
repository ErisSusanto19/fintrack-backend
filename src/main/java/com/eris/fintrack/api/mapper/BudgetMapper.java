package com.eris.fintrack.api.mapper;

import com.eris.fintrack.api.budget.dto.BudgetResponse;
import com.eris.fintrack.domain.Budget;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BudgetMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    BudgetResponse toDto(Budget budget);
}