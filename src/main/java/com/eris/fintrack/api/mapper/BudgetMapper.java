package com.eris.fintrack.api.mapper;

import com.eris.fintrack.api.budget.dto.BudgetResponse;
import com.eris.fintrack.domain.Budget;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BudgetMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(target = "amountSpent", ignore = true)
    @Mapping(target = "remainingAmount", ignore = true)
    @Mapping(target = "percentageSpent", ignore = true)
    BudgetResponse toDto(Budget budget);
}