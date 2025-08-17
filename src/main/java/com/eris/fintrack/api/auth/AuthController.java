package com.eris.fintrack.api.auth;

import com.eris.fintrack.api.auth.dto.AuthResponse;
import com.eris.fintrack.api.auth.dto.LoginRequest;
import com.eris.fintrack.api.auth.dto.RegisterRequest;
import com.eris.fintrack.api.common.ApiResponse;
import com.eris.fintrack.application.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse authResponse = authService.registerUser(registerRequest);
        return new ResponseEntity<>(ApiResponse.success(authResponse), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }
}