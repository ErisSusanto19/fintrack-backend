package com.eris.fintrack.application.service;

import com.eris.fintrack.api.auth.dto.AuthResponse;
import com.eris.fintrack.api.auth.dto.LoginRequest;
import com.eris.fintrack.api.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse registerUser(RegisterRequest registerRequest);
    AuthResponse login(LoginRequest loginRequest);
}