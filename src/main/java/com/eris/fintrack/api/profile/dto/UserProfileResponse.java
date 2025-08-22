package com.eris.fintrack.api.profile.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserProfileResponse {
    private UUID id;
    private String fullName;
    private String email;
}
