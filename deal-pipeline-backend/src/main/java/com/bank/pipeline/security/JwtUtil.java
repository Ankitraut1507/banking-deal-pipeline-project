package com.bank.pipeline.security;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.bank.pipeline.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

/** JWT Utility class.*/
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationMillis;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMillis) {

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMillis = expirationMillis;
    }

    // Generate JWT Token
    public String generateToken(User user) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setSubject(user.getUsername())              // sub
                .addClaims(Map.of(
                        "role", user.getRole().name()       // role
                ))
                .setIssuedAt(now)                            // iat
                .setExpiration(expiryDate)                  // exp
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Validate JWT Token
    public boolean validateToken(String token) {
        try {
            getAllClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    // Extract Username (sub)
    public String extractUsername(String token) {
        return getAllClaims(token).getSubject();
    }

    // Extract Role
    public String extractRole(String token) {
        return getAllClaims(token).get("role", String.class);
    }

    // Internal helper
    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
