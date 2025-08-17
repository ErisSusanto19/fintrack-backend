package com.eris.fintrack.api.mapper;

import com.eris.fintrack.api.category.dto.CategoryResponse;
import com.eris.fintrack.domain.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "type", expression = "java(category.getType().name())")
    CategoryResponse toDto(Category category);
}