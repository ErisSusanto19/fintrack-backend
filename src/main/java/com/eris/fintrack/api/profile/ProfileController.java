package com.eris.fintrack.api.profile;

import com.eris.fintrack.api.common.ApiResponse;
import com.eris.fintrack.api.mapper.UserMapper;
import com.eris.fintrack.api.profile.dto.UserProfileResponse;
import com.eris.fintrack.application.service.ProfileService;
import com.eris.fintrack.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(){
        User currentUser = profileService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success(userMapper.toDto(currentUser)));
    }
}
