package com.bank.pipeline.security;

import com.bank.pipeline.model.Role;
import com.bank.pipeline.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User testUser;
    private String secretKey = "test-secret-key-for-jwt-signing-12345678901234567890";
    private long expirationMillis = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(secretKey, expirationMillis);
        
        testUser = new User();
        testUser.setId("123");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);
    }

    @Test
    void generateToken_validUser_shouldReturnValidToken() {
        // Act
        String token = jwtUtil.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // Verify token can be parsed
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        assertEquals("testuser", claims.getSubject());
        assertEquals("USER", claims.get("role"));
    }

    @Test
    void generateToken_adminUser_shouldIncludeAdminRole() {
        // Arrange
        testUser.setRole(Role.ADMIN);

        // Act
        String token = jwtUtil.generateToken(testUser);

        // Assert
        assertNotNull(token);
        
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        assertEquals("ADMIN", claims.get("role"));
    }

    @Test
    void extractUsername_validToken_shouldReturnUsername() {
        // Arrange
        String token = jwtUtil.generateToken(testUser);

        // Act
        String username = jwtUtil.extractUsername(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void extractUsername_invalidToken_shouldThrowException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtUtil.extractUsername(invalidToken));
    }

    @Test
    void extractUsername_nullToken_shouldThrowException() {
        // Act & Assert
        assertThrows(Exception.class, () -> jwtUtil.extractUsername(null));
    }

    @Test
    void extractUsername_emptyToken_shouldThrowException() {
        // Act & Assert
        assertThrows(Exception.class, () -> jwtUtil.extractUsername(""));
    }

    @Test
    void validateToken_validToken_shouldReturnTrue() {
        // Arrange
        String token = jwtUtil.generateToken(testUser);

        // Act
        boolean isValid = jwtUtil.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_invalidToken_shouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_expiredToken_shouldReturnFalse() {
        // Arrange - Create token with very short expiration
        JwtUtil shortLivedJwtUtil = new JwtUtil(secretKey, 1); // 1 millisecond
        
        String token = shortLivedJwtUtil.generateToken(testUser);
        
        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isValid = shortLivedJwtUtil.validateToken(token);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void generateToken_userWithNullRole_shouldThrowException() {
        // Arrange
        testUser.setRole(null);

        // Act & Assert
        assertThrows(Exception.class, () -> jwtUtil.generateToken(testUser));
    }

    @Test
    void generateToken_userWithNullUsername_shouldStillGenerateToken() {
        // Arrange
        testUser.setUsername(null);

        // Act
        String token = jwtUtil.generateToken(testUser);

        // Assert
        assertNotNull(token);
        
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        assertNull(claims.getSubject());
        assertEquals("USER", claims.get("role"));
    }

    @Test
    void extractRole_validToken_shouldReturnRole() {
        // Arrange
        String token = jwtUtil.generateToken(testUser);

        // Act
        String role = jwtUtil.extractRole(token);

        // Assert
        assertEquals("USER", role);
    }

    @Test
    void extractRole_adminToken_shouldReturnAdminRole() {
        // Arrange
        testUser.setRole(Role.ADMIN);
        String token = jwtUtil.generateToken(testUser);

        // Act
        String role = jwtUtil.extractRole(token);

        // Assert
        assertEquals("ADMIN", role);
    }

    @Test
    void extractRole_invalidToken_shouldThrowException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtUtil.extractRole(invalidToken));
    }

    @Test
    void validateToken_nullToken_shouldReturnFalse() {
        // Act
        boolean isValid = jwtUtil.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_emptyToken_shouldReturnFalse() {
        // Act
        boolean isValid = jwtUtil.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void generateToken_shouldIncludeExpirationDate() {
        // Act
        String token = jwtUtil.generateToken(testUser);

        // Assert
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void generateToken_shouldIncludeIssuedDate() {
        // Act
        String token = jwtUtil.generateToken(testUser);

        // Assert
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        assertNotNull(claims.getIssuedAt());
        assertTrue(claims.getIssuedAt().before(new Date()));
    }
}
