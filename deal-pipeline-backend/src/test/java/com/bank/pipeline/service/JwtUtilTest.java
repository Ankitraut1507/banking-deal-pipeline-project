package com.bank.pipeline.service;

import com.bank.pipeline.model.Role;
import com.bank.pipeline.model.User;
import com.bank.pipeline.security.JwtUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private final JwtUtil jwtUtil =
            new JwtUtil(
                    "this-is-a-very-secure-secret-key-1234567890",
                    3600000
            );

    @Test
    void generateAndValidateToken() {
        User user = new User();
        user.setUsername("ankit");
        user.setRole(Role.ADMIN);

        String token = jwtUtil.generateToken(user);

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void extractUsernameAndRole() {
        User user = new User();
        user.setUsername("ankit");
        user.setRole(Role.USER);

        String token = jwtUtil.generateToken(user);

        assertEquals("ankit", jwtUtil.extractUsername(token));
        assertEquals("USER", jwtUtil.extractRole(token));
    }

    @Test
    void invalidToken_shouldFailValidation() {
        assertFalse(jwtUtil.validateToken("invalid.token.value"));
    }
}
