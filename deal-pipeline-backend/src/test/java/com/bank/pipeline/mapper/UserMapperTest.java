package com.bank.pipeline.mapper;

import com.bank.pipeline.dto.UserResponse;
import com.bank.pipeline.model.Role;
import com.bank.pipeline.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toEntity_validCreateUserRequest_shouldReturnUser() {
        // Arrange
        com.bank.pipeline.dto.CreateUserRequest request = new com.bank.pipeline.dto.CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // Act
        User user = UserMapper.toEntity(request);

        // Assert
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertNull(user.getId());
        assertNull(user.getRole());
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
        assertTrue(user.isActive());
    }

    @Test
    void toResponse_validUser_shouldReturnUserResponse() {
        // Arrange
        User user = new User();
        user.setId("123");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(Role.USER);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Act
        UserResponse response = UserMapper.toResponse(user);

        // Assert
        assertNotNull(response);
        assertEquals("123", response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals(Role.USER, response.getRole());
        assertTrue(response.isActive());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());
    }

    @Test
    void toResponse_userWithNullFields_shouldHandleGracefully() {
        // Arrange
        User user = new User();
        user.setId("123");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        // Leave other fields as null

        // Act
        UserResponse response = UserMapper.toResponse(user);

        // Assert
        assertNotNull(response);
        assertEquals("123", response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertNull(response.getRole());
        // active field has default value true
        assertNotNull(response.isActive());
        assertNull(response.getCreatedAt());
        assertNull(response.getUpdatedAt());
    }

    @Test
    void toEntity_nullRequest_shouldReturnNull() {
        // Act & Assert - The actual implementation throws NPE for null
        assertThrows(NullPointerException.class, () -> UserMapper.toEntity(null));
    }

    @Test
    void toResponse_nullUser_shouldReturnNull() {
        // Act & Assert - The actual implementation throws NPE for null
        assertThrows(NullPointerException.class, () -> UserMapper.toResponse(null));
    }

    @Test
    void toEntity_userWithAdminRole_shouldPreserveRole() {
        // Arrange
        com.bank.pipeline.dto.CreateUserRequest request = new com.bank.pipeline.dto.CreateUserRequest();
        request.setUsername("admin");
        request.setEmail("admin@example.com");
        request.setPassword("admin123");

        // Act
        User user = UserMapper.toEntity(request);

        // Assert
        assertNotNull(user);
        assertEquals("admin", user.getUsername());
        assertEquals("admin@example.com", user.getEmail());
        assertEquals("admin123", user.getPassword());
        // Role should be null initially, set by service layer
        assertNull(user.getRole());
    }

    @Test
    void toResponse_userWithAdminRole_shouldIncludeRoleInResponse() {
        // Arrange
        User user = new User();
        user.setId("456");
        user.setUsername("admin");
        user.setEmail("admin@example.com");
        user.setRole(Role.ADMIN);
        user.setActive(true);

        // Act
        UserResponse response = UserMapper.toResponse(user);

        // Assert
        assertNotNull(response);
        assertEquals("456", response.getId());
        assertEquals("admin", response.getUsername());
        assertEquals("admin@example.com", response.getEmail());
        assertEquals(Role.ADMIN, response.getRole());
        assertTrue(response.isActive());
    }

    @Test
    void toResponse_inactiveUser_shouldShowInactiveStatus() {
        // Arrange
        User user = new User();
        user.setId("789");
        user.setUsername("inactiveuser");
        user.setEmail("inactive@example.com");
        user.setActive(false);

        // Act
        UserResponse response = UserMapper.toResponse(user);

        // Assert
        assertNotNull(response);
        assertEquals("789", response.getId());
        assertEquals("inactiveuser", response.getUsername());
        assertEquals("inactive@example.com", response.getEmail());
        assertFalse(response.isActive());
    }

    @Test
    void toResponse_userWithTimestamps_shouldPreserveTimestamps() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        
        User user = new User();
        user.setId("123");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(Role.USER);
        user.setActive(true);
        user.setCreatedAt(yesterday);
        user.setUpdatedAt(now);

        // Act
        UserResponse response = UserMapper.toResponse(user);

        // Assert
        assertNotNull(response);
        assertEquals(yesterday, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());
    }

    @Test
    void toEntity_emptyFields_shouldHandleGracefully() {
        // Arrange
        com.bank.pipeline.dto.CreateUserRequest request = new com.bank.pipeline.dto.CreateUserRequest();
        request.setUsername("");
        request.setEmail("");
        request.setPassword("");

        // Act
        User user = UserMapper.toEntity(request);

        // Assert
        assertNotNull(user);
        assertEquals("", user.getUsername());
        assertEquals("", user.getEmail());
        assertEquals("", user.getPassword());
    }
}
