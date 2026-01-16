package com.bank.pipeline.util;

import com.bank.pipeline.dto.DealCreateRequest;
import com.bank.pipeline.dto.DealUpdateRequest;
import com.bank.pipeline.dto.LoginRequest;
import com.bank.pipeline.model.*;
import com.bank.pipeline.security.JwtUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Utility class for creating test data objects
 */
public class TestDataBuilder {

    public static User createTestUser() {
        User user = new User();
        user.setId("user123");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    public static User createTestAdmin() {
        User admin = createTestUser();
        admin.setId("admin123");
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);
        return admin;
    }

    public static Deal createTestDeal() {
        Deal deal = Deal.builder()
                .id("deal123")
                .title("Test Deal")
                .sector("Technology")
                .dealType(DealType.MERGER_ACQUISITION)
                .stage(DealStage.LEAD)
                .ownerId("user123")
                .dealValue(1000000.0)
                .notes(new ArrayList<>())
                .createdAt(java.time.Instant.now())
                .updatedAt(java.time.Instant.now())
                .build();
        return deal;
    }

    public static DealCreateRequest createDealCreateRequest() {
        DealCreateRequest request = new DealCreateRequest();
        request.setTitle("New Deal");
        request.setSector("Finance");
        request.setDealType(DealType.IPO);
        request.setDealValue(500000.0);
        return request;
    }

    public static DealUpdateRequest createDealUpdateRequest() {
        DealUpdateRequest request = new DealUpdateRequest();
        request.setTitle("Updated Deal");
        request.setSector("Updated Sector");
        request.setDealType(DealType.MERGER_ACQUISITION);
        request.setStage(DealStage.PROSPECTING);
        request.setDealValue(2000000.0);
        return request;
    }

    public static LoginRequest createLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        return request;
    }

    public static RefreshToken createTestRefreshToken() {
        RefreshToken token = RefreshToken.builder()
                .token("refresh-token-123")
                .userId("user123")
                .expiryDate(java.time.Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000))
                .revoked(false)
                .build();
        return token;
    }

    public static DealNote createTestDealNote() {
        return new DealNote("user123", "Test note content");
    }

    public static JwtUtil createTestJwtUtil() {
        return new JwtUtil(
                "this-is-a-very-secure-secret-key-1234567890",
                3600000
        );
    }
}
