package com.bank.pipeline.util;

import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * Utility class for security-related test setups
 */
public class SecurityTestUtils {

    public static void setupSecurityContext(String username, String role) {
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
        );
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities
        );

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        
        SecurityContextHolder.setContext(securityContext);
    }

    public static void setupUserSecurityContext() {
        setupSecurityContext("user123", "USER");
    }

    public static void setupAdminSecurityContext() {
        setupSecurityContext("admin123", "ADMIN");
    }

    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    public static Authentication createMockAuthentication(String username, String role) {
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
        );
        
        return new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities
        );
    }
}
