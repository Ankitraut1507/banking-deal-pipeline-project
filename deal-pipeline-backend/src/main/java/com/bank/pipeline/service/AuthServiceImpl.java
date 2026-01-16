package com.bank.pipeline.service;

import com.bank.pipeline.dto.AuthResponse;
import com.bank.pipeline.dto.LoginRequest;
import com.bank.pipeline.exception.UserNotFoundException;
import com.bank.pipeline.model.RefreshToken;
import com.bank.pipeline.model.User;
import com.bank.pipeline.repository.UserRepository;
import com.bank.pipeline.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Invalid username or password"));

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            throw new UserNotFoundException(
                    "Invalid username or password");
        }

        String accessToken = jwtUtil.generateToken(user);
        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer"
        );
    }

    @Override
    public AuthResponse refresh(String refreshToken) {

        // Validate old refresh token
        RefreshToken oldToken =
                refreshTokenService.validateRefreshToken(
                        refreshToken
                );

        // Load user
        User user = userRepository.findById(oldToken.getUserId())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        // Revoke OLD refresh token
        refreshTokenService.revokeRefreshToken(oldToken.getToken());

        // Create NEW refresh token (ROTATION)
        RefreshToken newRefreshToken =
                refreshTokenService.createRefreshToken(user);

        // Generate new access token
        String newAccessToken = jwtUtil.generateToken(user);

        // Return BOTH tokens
        return new AuthResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                "Bearer"
        );
    }


    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(
                refreshToken
        );
    }
}
