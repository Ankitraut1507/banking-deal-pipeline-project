package com.bank.pipeline.service;

import com.bank.pipeline.exception.UserNotFoundException;
import com.bank.pipeline.model.RefreshToken;
import com.bank.pipeline.model.User;
import com.bank.pipeline.repository.RefreshTokenRepository;
import com.bank.pipeline.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    // 7 days
    private static final long REFRESH_TOKEN_VALIDITY =
            7 * 24 * 60 * 60 * 1000;

    @Override
    public RefreshToken createRefreshToken(User user) {

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .userId(user.getId())
                .expiryDate(
                        Instant.now().plusMillis(REFRESH_TOKEN_VALIDITY)
                )
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken validateRefreshToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() ->
                        new UserNotFoundException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new UserNotFoundException("Refresh token revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new UserNotFoundException("Refresh token expired");
        }

        return refreshToken;
    }

    @Override
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = validateRefreshToken(token);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }
}
