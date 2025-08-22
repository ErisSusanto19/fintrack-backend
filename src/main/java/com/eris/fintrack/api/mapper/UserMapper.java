package com.eris.fintrack.api.mapper;

import com.eris.fintrack.api.profile.dto.UserProfileResponse;
import com.eris.fintrack.domain.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserProfileResponse toDto(User user);
}
