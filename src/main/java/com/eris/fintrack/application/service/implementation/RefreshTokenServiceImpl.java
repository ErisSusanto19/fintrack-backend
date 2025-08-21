package com.eris.fintrack.application.service.implementation;

import com.eris.fintrack.api.exception.BadRequestException;
import com.eris.fintrack.api.exception.ResourceNotFoundException;
import com.eris.fintrack.application.service.RefreshTokenService;
import com.eris.fintrack.domain.RefreshToken;
import com.eris.fintrack.infrastructure.persistence.RefreshTokenRepository;
import com.eris.fintrack.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    @Override
    public RefreshToken createRefreshToken(String email) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(userRepository.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found")))
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new BadRequestException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }
}