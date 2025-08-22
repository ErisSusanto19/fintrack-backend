package com.eris.fintrack.api.auth.dto;

import com.eris.fintrack.api.profile.dto.UserProfileResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UserProfileResponse user;
}