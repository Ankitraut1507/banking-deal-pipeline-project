package com.bank.pipeline.service;

import com.bank.pipeline.dto.AuthResponse;
import com.bank.pipeline.dto.LoginRequest;
import com.bank.pipeline.exception.UserNotFoundException;
import com.bank.pipeline.model.RefreshToken;
import com.bank.pipeline.model.User;
import com.bank.pipeline.repository.UserRepository;
import com.bank.pipeline.security.JwtUtil;
import com.bank.pipeline.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private LoginRequest loginRequest;
    private RefreshToken refreshToken;
    private String accessToken;
    private String refreshTokenString;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createTestUser();
        loginRequest = TestDataBuilder.createLoginRequest();
        refreshToken = TestDataBuilder.createTestRefreshToken();
        accessToken = "access-token-123";
        refreshTokenString = "refresh-token-123";
    }

    @Test
    void login_validCredentials_shouldReturnAuthResponse() {
        // Arrange
        when(userRepository.findByUsername(loginRequest.getUsername()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword()))
                .thenReturn(true);
        when(jwtUtil.generateToken(any(User.class))).thenReturn(accessToken);
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(refreshToken);

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshTokenString, response.getRefreshToken());
        assertEquals("Bearer", response.getType());

        verify(userRepository).findByUsername(loginRequest.getUsername());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPassword());
        verify(jwtUtil).generateToken(testUser);
        verify(refreshTokenService).createRefreshToken(testUser);
    }

    @Test
    void login_invalidUsername_shouldThrowUserNotFoundException() {
        // Arrange
        when(userRepository.findByUsername(loginRequest.getUsername()))
                .thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Invalid username or password", exception.getMessage());

        verify(userRepository).findByUsername(loginRequest.getUsername());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(any());
        verify(refreshTokenService, never()).createRefreshToken(any());
    }

    @Test
    void login_invalidPassword_shouldThrowUserNotFoundException() {
        // Arrange
        when(userRepository.findByUsername(loginRequest.getUsername()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword()))
                .thenReturn(false);

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Invalid username or password", exception.getMessage());

        verify(userRepository).findByUsername(loginRequest.getUsername());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPassword());
        verify(jwtUtil, never()).generateToken(any());
        verify(refreshTokenService, never()).createRefreshToken(any());
    }

    @Test
    void refresh_validToken_shouldReturnNewAuthResponse() {
        // Arrange
        when(refreshTokenService.validateRefreshToken(refreshTokenString))
                .thenReturn(refreshToken);
        when(userRepository.findById(refreshToken.getUserId()))
                .thenReturn(Optional.of(testUser));
        RefreshToken newRefreshToken = TestDataBuilder.createTestRefreshToken();
        newRefreshToken.setToken("new-refresh-token");
        when(refreshTokenService.createRefreshToken(any(User.class)))
                .thenReturn(newRefreshToken);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("new-access-token");

        // Act
        AuthResponse response = authService.refresh(refreshTokenString);

        // Assert
        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getType());

        verify(refreshTokenService).validateRefreshToken(refreshTokenString);
        verify(userRepository).findById(refreshToken.getUserId());
        verify(refreshTokenService).revokeRefreshToken(refreshTokenString);
        verify(refreshTokenService).createRefreshToken(testUser);
        verify(jwtUtil).generateToken(testUser);
    }

    @Test
    void refresh_invalidToken_shouldThrowUserNotFoundException() {
        // Arrange
        when(refreshTokenService.validateRefreshToken(refreshTokenString))
                .thenThrow(new UserNotFoundException("Invalid refresh token"));

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> authService.refresh(refreshTokenString)
        );

        assertEquals("Invalid refresh token", exception.getMessage());

        verify(refreshTokenService).validateRefreshToken(refreshTokenString);
        verify(userRepository, never()).findById(any());
        verify(refreshTokenService, never()).revokeRefreshToken(any());
        verify(refreshTokenService, never()).createRefreshToken(any());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void refresh_userNotFound_shouldThrowUserNotFoundException() {
        // Arrange
        when(refreshTokenService.validateRefreshToken(refreshTokenString))
                .thenReturn(refreshToken);
        when(userRepository.findById(refreshToken.getUserId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> authService.refresh(refreshTokenString)
        );

        assertEquals("User not found", exception.getMessage());

        verify(refreshTokenService).validateRefreshToken(refreshTokenString);
        verify(userRepository).findById(refreshToken.getUserId());
    }

    @Test
    void logout_validToken_shouldRevokeToken() {
        // Arrange
        doNothing().when(refreshTokenService).revokeRefreshToken(refreshTokenString);

        // Act
        authService.logout(refreshTokenString);

        // Assert
        verify(refreshTokenService).revokeRefreshToken(refreshTokenString);
    }

    @Test
    void logout_invalidToken_shouldThrowUserNotFoundException() {
        // Arrange
        doThrow(new UserNotFoundException("Invalid refresh token"))
                .when(refreshTokenService).revokeRefreshToken(refreshTokenString);

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> authService.logout(refreshTokenString)
        );

        assertEquals("Invalid refresh token", exception.getMessage());

        verify(refreshTokenService).revokeRefreshToken(refreshTokenString);
    }

    @Test
    void login_nullPassword_shouldThrowUserNotFoundException() {
        // Arrange
        LoginRequest nullPasswordRequest = new LoginRequest();
        nullPasswordRequest.setUsername(loginRequest.getUsername());
        nullPasswordRequest.setPassword(null);
        
        when(userRepository.findByUsername(nullPasswordRequest.getUsername()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(nullPasswordRequest.getPassword(), testUser.getPassword()))
                .thenReturn(false);

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> authService.login(nullPasswordRequest)
        );

        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void refresh_tokenRotation_shouldRevokeOldToken() {
        // Arrange
        when(refreshTokenService.validateRefreshToken(refreshTokenString))
                .thenReturn(refreshToken);
        when(userRepository.findById(refreshToken.getUserId()))
                .thenReturn(Optional.of(testUser));
        RefreshToken newRefreshToken = TestDataBuilder.createTestRefreshToken();
        newRefreshToken.setToken("new-refresh-token");
        when(refreshTokenService.createRefreshToken(any(User.class)))
                .thenReturn(newRefreshToken);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("new-access-token");

        // Act
        authService.refresh(refreshTokenString);

        // Assert - Verify token rotation sequence
        verify(refreshTokenService).validateRefreshToken(refreshTokenString);
        verify(refreshTokenService).revokeRefreshToken(refreshTokenString);
        verify(refreshTokenService).createRefreshToken(testUser);
    }
}
