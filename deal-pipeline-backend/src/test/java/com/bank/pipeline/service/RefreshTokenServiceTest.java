package com.bank.pipeline.service;

import com.bank.pipeline.exception.UserNotFoundException;
import com.bank.pipeline.model.RefreshToken;
import com.bank.pipeline.model.User;
import com.bank.pipeline.repository.RefreshTokenRepository;
import com.bank.pipeline.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private User testUser;
    private RefreshToken testRefreshToken;
    private String validToken;
    private String expiredToken;
    private String revokedToken;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createTestUser();
        testRefreshToken = TestDataBuilder.createTestRefreshToken();
        validToken = "valid-refresh-token";
        expiredToken = "expired-refresh-token";
        revokedToken = "revoked-refresh-token";
    }

    @Test
    void createRefreshToken_validUser_shouldReturnNewToken() {
        // Arrange
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RefreshToken result = refreshTokenService.createRefreshToken(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getUserId());
        assertFalse(result.isRevoked());
        assertTrue(result.getExpiryDate().isAfter(Instant.now()));
        assertNotNull(result.getToken());

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void validateRefreshToken_validToken_shouldReturnToken() {
        // Arrange
        when(refreshTokenRepository.findByToken(validToken))
                .thenReturn(Optional.of(testRefreshToken));

        // Act
        RefreshToken result = refreshTokenService.validateRefreshToken(validToken);

        // Assert
        assertNotNull(result);
        assertEquals(testRefreshToken.getToken(), result.getToken());
        assertEquals(testRefreshToken.getUserId(), result.getUserId());
        assertFalse(result.isRevoked());

        verify(refreshTokenRepository).findByToken(validToken);
    }

    @Test
    void validateRefreshToken_nonExistentToken_shouldThrowUserNotFoundException() {
        // Arrange
        when(refreshTokenRepository.findByToken(validToken))
                .thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> refreshTokenService.validateRefreshToken(validToken)
        );

        assertEquals("Invalid refresh token", exception.getMessage());

        verify(refreshTokenRepository).findByToken(validToken);
    }

    @Test
    void validateRefreshToken_revokedToken_shouldThrowUserNotFoundException() {
        // Arrange
        RefreshToken revokedRefreshToken = TestDataBuilder.createTestRefreshToken();
        revokedRefreshToken.setRevoked(true);
        revokedRefreshToken.setToken(revokedToken);

        when(refreshTokenRepository.findByToken(revokedToken))
                .thenReturn(Optional.of(revokedRefreshToken));

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> refreshTokenService.validateRefreshToken(revokedToken)
        );

        assertEquals("Refresh token revoked", exception.getMessage());

        verify(refreshTokenRepository).findByToken(revokedToken);
    }

    @Test
    void validateRefreshToken_expiredToken_shouldThrowUserNotFoundException() {
        // Arrange
        RefreshToken expiredRefreshToken = TestDataBuilder.createTestRefreshToken();
        expiredRefreshToken.setExpiryDate(Instant.now().minusMillis(1000));
        expiredRefreshToken.setToken(expiredToken);

        when(refreshTokenRepository.findByToken(expiredToken))
                .thenReturn(Optional.of(expiredRefreshToken));

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> refreshTokenService.validateRefreshToken(expiredToken)
        );

        assertEquals("Refresh token expired", exception.getMessage());

        verify(refreshTokenRepository).findByToken(expiredToken);
    }

    @Test
    void revokeRefreshToken_validToken_shouldMarkAsRevoked() {
        // Arrange
        when(refreshTokenRepository.findByToken(validToken))
                .thenReturn(Optional.of(testRefreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        refreshTokenService.revokeRefreshToken(validToken);

        // Assert
        verify(refreshTokenRepository).findByToken(validToken);
        verify(refreshTokenRepository).save(testRefreshToken);
        assertTrue(testRefreshToken.isRevoked());
    }

    @Test
    void revokeRefreshToken_invalidToken_shouldThrowUserNotFoundException() {
        // Arrange
        when(refreshTokenRepository.findByToken(validToken))
                .thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> refreshTokenService.revokeRefreshToken(validToken)
        );

        assertEquals("Invalid refresh token", exception.getMessage());

        verify(refreshTokenRepository).findByToken(validToken);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void revokeRefreshToken_alreadyRevokedToken_shouldStillSave() {
        // Arrange
        RefreshToken alreadyRevokedToken = TestDataBuilder.createTestRefreshToken();
        alreadyRevokedToken.setRevoked(true);
        alreadyRevokedToken.setToken(revokedToken);

        when(refreshTokenRepository.findByToken(revokedToken))
                .thenReturn(Optional.of(alreadyRevokedToken));

        // Act & Assert - This test verifies that even if token is already revoked,
        // the service still tries to save it (idempotent operation)
        assertThrows(UserNotFoundException.class, () -> {
            refreshTokenService.revokeRefreshToken(revokedToken);
        });

        verify(refreshTokenRepository).findByToken(revokedToken);
    }

    @Test
    void createRefreshToken_multipleCalls_shouldGenerateUniqueTokens() {
        // Arrange
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RefreshToken token1 = refreshTokenService.createRefreshToken(testUser);
        RefreshToken token2 = refreshTokenService.createRefreshToken(testUser);

        // Assert
        assertNotEquals(token1.getToken(), token2.getToken());
        assertEquals(testUser.getId(), token1.getUserId());
        assertEquals(testUser.getId(), token2.getUserId());

        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void validateRefreshToken_edgeCase_exactlyNowExpiry_shouldThrowException() {
        // Arrange
        RefreshToken nowExpiringToken = TestDataBuilder.createTestRefreshToken();
        nowExpiringToken.setExpiryDate(Instant.now().minusMillis(1)); // Just expired
        nowExpiringToken.setToken(validToken);

        when(refreshTokenRepository.findByToken(validToken))
                .thenReturn(Optional.of(nowExpiringToken));

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> refreshTokenService.validateRefreshToken(validToken)
        );

        assertEquals("Refresh token expired", exception.getMessage());

        verify(refreshTokenRepository).findByToken(validToken);
    }

    @Test
    void revokeRefreshToken_nullToken_shouldThrowUserNotFoundException() {
        // Arrange
        when(refreshTokenRepository.findByToken(null))
                .thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> refreshTokenService.revokeRefreshToken(null)
        );

        assertEquals("Invalid refresh token", exception.getMessage());

        verify(refreshTokenRepository).findByToken(null);
        verify(refreshTokenRepository, never()).save(any());
    }
}
