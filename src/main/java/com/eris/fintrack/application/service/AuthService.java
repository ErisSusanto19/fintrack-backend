package com.eris.fintrack.application.service;

import com.eris.fintrack.api.auth.dto.AuthResponse;
import com.eris.fintrack.api.auth.dto.LoginRequest;
import com.eris.fintrack.api.auth.dto.RegisterRequest;
import com.eris.fintrack.domain.User;

public interface AuthService {
    User registerUser(RegisterRequest registerRequest);
    AuthResponse login(LoginRequest loginRequest);
}