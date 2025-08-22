package com.eris.fintrack.application.service.implementation;

import com.eris.fintrack.api.auth.dto.AuthResponse;
import com.eris.fintrack.api.auth.dto.LoginRequest;
import com.eris.fintrack.api.auth.dto.RefreshTokenRequest;
import com.eris.fintrack.api.auth.dto.RegisterRequest;
import com.eris.fintrack.api.exception.BadRequestException;
import com.eris.fintrack.api.exception.ResourceNotFoundException;
import com.eris.fintrack.api.mapper.UserMapper;
import com.eris.fintrack.application.service.AuthService;
import com.eris.fintrack.application.service.RefreshTokenService;
import com.eris.fintrack.domain.RefreshToken;
import com.eris.fintrack.domain.Role;
import com.eris.fintrack.domain.User;
import com.eris.fintrack.infrastructure.persistence.RoleRepository;
import com.eris.fintrack.infrastructure.persistence.UserRepository;
import com.eris.fintrack.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final RoleRepository roleRepository;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponse registerUser(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new BadRequestException("Email already in use");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found."));

        User user = User.builder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .roles(Set.of(userRole))
                .build();

        User savedUser = userRepository.save(user);

        var userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toList())
        );

        var jwtToken = jwtService.generateToken(userDetails);
        var refreshToken = refreshTokenService.createRefreshToken(registerRequest.getEmail());

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .user(userMapper.toDto(savedUser))
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
        var jwtToken = jwtService.generateToken(userDetails);
        var refreshToken = refreshTokenService.createRefreshToken(loginRequest.getEmail());

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .user(userMapper.toDto(user))
                .build();
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        return refreshTokenService.findByToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
                    String accessToken = jwtService.generateToken(userDetails);
                    return AuthResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(request.getRefreshToken())
                            .build();
                })
                .orElseThrow(() -> new BadRequestException("Refresh token is not in database!"));
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.deleteByToken(request.getRefreshToken());
    }
}