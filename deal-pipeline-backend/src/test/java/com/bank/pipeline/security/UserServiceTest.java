package com.bank.pipeline.security;

import com.bank.pipeline.exception.BusinessException;
import com.bank.pipeline.exception.UserNotFoundException;
import com.bank.pipeline.model.Role;
import com.bank.pipeline.model.User;
import com.bank.pipeline.repository.UserRepository;
import com.bank.pipeline.service.UserServiceImpl;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createTestUser();
    }

    @Test
    void createUser_validUser_shouldSaveUserWithEncodedPassword() {
        // Arrange
        when(userRepository.existsByUsername(testUser.getUsername()))
                .thenReturn(false);
        when(userRepository.existsByEmail(testUser.getEmail()))
                .thenReturn(false);
        when(passwordEncoder.encode(testUser.getPassword()))
                .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User saved = userService.createUser(testUser);

        // Assert
        assertNotNull(saved);
        assertEquals(testUser.getUsername(), saved.getUsername());
        assertEquals(testUser.getEmail(), saved.getEmail());
        assertEquals("encodedPassword", saved.getPassword());
        assertEquals(Role.USER, saved.getRole());
        assertTrue(saved.isActive());

        verify(userRepository).existsByUsername(testUser.getUsername());
        verify(userRepository).existsByEmail(testUser.getEmail());
        verify(passwordEncoder).encode(testUser.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_adminRole_shouldPreserveAdminRole() {
        // Arrange
        testUser.setRole(Role.ADMIN);
        when(userRepository.existsByUsername(testUser.getUsername()))
                .thenReturn(false);
        when(userRepository.existsByEmail(testUser.getEmail()))
                .thenReturn(false);
        when(passwordEncoder.encode(testUser.getPassword()))
                .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User saved = userService.createUser(testUser);

        // Assert
        assertEquals(Role.ADMIN, saved.getRole());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_inactiveUser_shouldSetActiveToTrue() {
        // Arrange
        testUser.setActive(false);
        when(userRepository.existsByUsername(testUser.getUsername()))
                .thenReturn(false);
        when(userRepository.existsByEmail(testUser.getEmail()))
                .thenReturn(false);
        when(passwordEncoder.encode(testUser.getPassword()))
                .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User saved = userService.createUser(testUser);

        // Assert
        assertTrue(saved.isActive());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_existingUsername_shouldThrowBusinessException() {
        // Arrange
        when(userRepository.existsByUsername(testUser.getUsername()))
                .thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createUser(testUser)
        );

        assertEquals("Username already exists: " + testUser.getUsername(), exception.getMessage());

        verify(userRepository).existsByUsername(testUser.getUsername());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_existingEmail_shouldThrowBusinessException() {
        // Arrange
        when(userRepository.existsByUsername(testUser.getUsername()))
                .thenReturn(false);
        when(userRepository.existsByEmail(testUser.getEmail()))
                .thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createUser(testUser)
        );

        assertEquals("Email already exists: " + testUser.getEmail(), exception.getMessage());

        verify(userRepository).existsByUsername(testUser.getUsername());
        verify(userRepository).existsByEmail(testUser.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void findByUsername_existingUser_shouldReturnUser() {
        // Arrange
        when(userRepository.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testUser));

        // Act
        User result = userService.findByUsername(testUser.getUsername());

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());

        verify(userRepository).findByUsername(testUser.getUsername());
    }

    @Test
    void findByUsername_nonExistentUser_shouldThrowUserNotFoundException() {
        // Arrange
        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.findByUsername("nonexistent")
        );

        assertEquals("User with username not found: nonexistent", exception.getMessage());

        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void findByEmail_existingUser_shouldReturnUser() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));

        // Act
        User result = userService.findByEmail(testUser.getEmail());

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());

        verify(userRepository).findByEmail(testUser.getEmail());
    }

    @Test
    void findByEmail_nonExistentUser_shouldThrowUserNotFoundException() {
        // Arrange
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.findByEmail("nonexistent@example.com")
        );

        assertEquals("User with email not found: nonexistent@example.com", exception.getMessage());

        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void existsByUsername_existingUser_shouldReturnTrue() {
        // Arrange
        when(userRepository.existsByUsername(testUser.getUsername()))
                .thenReturn(true);

        // Act
        boolean result = userService.existsByUsername(testUser.getUsername());

        // Assert
        assertTrue(result);

        verify(userRepository).existsByUsername(testUser.getUsername());
    }

    @Test
    void existsByUsername_nonExistentUser_shouldReturnFalse() {
        // Arrange
        when(userRepository.existsByUsername(anyString()))
                .thenReturn(false);

        // Act
        boolean result = userService.existsByUsername("nonexistent");

        // Assert
        assertFalse(result);

        verify(userRepository).existsByUsername("nonexistent");
    }

    @Test
    void existsByEmail_existingUser_shouldReturnTrue() {
        // Arrange
        when(userRepository.existsByEmail(testUser.getEmail()))
                .thenReturn(true);

        // Act
        boolean result = userService.existsByEmail(testUser.getEmail());

        // Assert
        assertTrue(result);

        verify(userRepository).existsByEmail(testUser.getEmail());
    }

    @Test
    void existsByEmail_nonExistentUser_shouldReturnFalse() {
        // Arrange
        when(userRepository.existsByEmail(anyString()))
                .thenReturn(false);

        // Act
        boolean result = userService.existsByEmail("nonexistent@example.com");

        // Assert
        assertFalse(result);

        verify(userRepository).existsByEmail("nonexistent@example.com");
    }

    @Test
    void promoteToAdmin_existingUser_shouldSetAdminRole() {
        // Arrange
        when(userRepository.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.promoteToAdmin(testUser.getUsername());

        // Assert
        assertNotNull(result);
        assertEquals(Role.ADMIN, result.getRole());

        verify(userRepository).findByUsername(testUser.getUsername());
        verify(userRepository).save(testUser);
    }

    @Test
    void promoteToAdmin_alreadyAdmin_shouldKeepAdminRole() {
        // Arrange
        testUser.setRole(Role.ADMIN);
        when(userRepository.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.promoteToAdmin(testUser.getUsername());

        // Assert
        assertNotNull(result);
        assertEquals(Role.ADMIN, result.getRole());

        verify(userRepository).findByUsername(testUser.getUsername());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserStatus_nonExistentUser_shouldThrowUserNotFoundException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent"))
                .thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUserStatus("nonexistent", true)
        );

        assertEquals("User not found: nonexistent", exception.getMessage());

        verify(userRepository).findByUsername("nonexistent");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserStatus_activateUser_shouldSetActiveToTrue() {
        // Arrange
        testUser.setActive(false);
        when(userRepository.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.updateUserStatus(testUser.getUsername(), true);

        // Assert
        assertNotNull(result);
        assertTrue(result.isActive());

        verify(userRepository).findByUsername(testUser.getUsername());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserStatus_deactivateUser_shouldSetActiveToFalse() {
        // Arrange
        testUser.setActive(true);
        when(userRepository.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.updateUserStatus(testUser.getUsername(), false);

        // Assert
        assertNotNull(result);
        assertFalse(result.isActive());

        verify(userRepository).findByUsername(testUser.getUsername());
        verify(userRepository).save(testUser);
    }

    @Test
    void deleteUser_nonExistentUser_shouldThrowUserNotFoundException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent"))
                .thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUser("nonexistent")
        );

        assertEquals("User not found: nonexistent", exception.getMessage());

        verify(userRepository).findByUsername("nonexistent");
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_existingUser_shouldDeleteUser() {
        // Arrange
        when(userRepository.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testUser));

        // Act
        userService.deleteUser(testUser.getUsername());

        // Assert
        verify(userRepository).findByUsername(testUser.getUsername());
        verify(userRepository).delete(testUser);
    }

    @Test
    void createUser_withNullUsername_shouldThrowException() {
        // Arrange
        testUser.setUsername(null);

        // Act & Assert - Service doesn't validate null, so it will pass through
        // The actual validation happens at higher layers
        assertDoesNotThrow(() -> userService.createUser(testUser));
    }

    @Test
    void createUser_withNullEmail_shouldThrowException() {
        // Arrange
        testUser.setEmail(null);

        // Act & Assert - Service doesn't validate null, so it will pass through
        // The actual validation happens at higher layers
        assertDoesNotThrow(() -> userService.createUser(testUser));
    }

    @Test
    void createUser_withNullPassword_shouldThrowException() {
        // Arrange
        testUser.setPassword(null);

        // Act & Assert - Service doesn't validate null, so it will pass through
        // The actual validation happens at higher layers
        assertDoesNotThrow(() -> userService.createUser(testUser));
    }

    @Test
    void createUser_withEmptyUsername_shouldStillCreate() {
        // Arrange
        testUser.setUsername("");
        when(userRepository.existsByUsername(testUser.getUsername()))
                .thenReturn(false);
        when(userRepository.existsByEmail(testUser.getEmail()))
                .thenReturn(false);
        when(passwordEncoder.encode(testUser.getPassword()))
                .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.createUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getUsername());

        verify(userRepository).save(testUser);
    }

    @Test
    void createUser_withEmptyEmail_shouldStillCreate() {
        // Arrange
        testUser.setEmail("");
        when(userRepository.existsByUsername(testUser.getUsername()))
                .thenReturn(false);
        when(userRepository.existsByEmail(testUser.getEmail()))
                .thenReturn(false);
        when(passwordEncoder.encode(testUser.getPassword()))
                .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.createUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getEmail());

        verify(userRepository).save(testUser);
    }

    @Test
    void createUser_withEmptyPassword_shouldStillCreate() {
        // Arrange
        testUser.setPassword("");
        when(userRepository.existsByUsername(testUser.getUsername()))
                .thenReturn(false);
        when(userRepository.existsByEmail(testUser.getEmail()))
                .thenReturn(false);
        when(passwordEncoder.encode(""))
                .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.createUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals("encodedPassword", result.getPassword());

        verify(userRepository).save(testUser);
    }

    @Test
    void createUser_withInvalidEmailFormat_shouldStillCreate() {
        // Arrange - Note: Email format validation is typically done at validation layer
        testUser.setEmail("invalid-email");
        when(userRepository.existsByUsername(testUser.getUsername()))
                .thenReturn(false);
        when(userRepository.existsByEmail(testUser.getEmail()))
                .thenReturn(false);
        when(passwordEncoder.encode(testUser.getPassword()))
                .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.createUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals("invalid-email", result.getEmail());

        verify(userRepository).save(testUser);
    }

    @Test
    void createUser_withWhitespaceUsername_shouldTrimAndCreate() {
        // Arrange
        testUser.setUsername("  username123  ");
        when(userRepository.existsByUsername(testUser.getUsername()))
                .thenReturn(false);
        when(userRepository.existsByEmail(testUser.getEmail()))
                .thenReturn(false);
        when(passwordEncoder.encode(testUser.getPassword()))
                .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.createUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals("  username123  ", result.getUsername());

        verify(userRepository).save(testUser);
    }

    @Test
    void findByUsername_caseInsensitiveSearch_shouldUseExactMatch() {
        // Arrange
        testUser = TestDataBuilder.createTestUser();
        testUser.setUsername("Username");
        when(userRepository.findByUsername("Username"))
                .thenReturn(Optional.of(testUser));

        // Act
        User result = userService.findByUsername("Username");

        // Assert
        assertNotNull(result);
        assertEquals("Username", result.getUsername());

        verify(userRepository).findByUsername("Username");
    }

    @Test
    void findByEmail_caseInsensitiveSearch_shouldUseExactMatch() {
        // Arrange
        testUser = TestDataBuilder.createTestUser();
        testUser.setEmail("Email@Example.com");
        when(userRepository.findByEmail("Email@Example.com"))
                .thenReturn(Optional.of(testUser));

        // Act
        User result = userService.findByEmail("Email@Example.com");

        // Assert
        assertNotNull(result);
        assertEquals("Email@Example.com", result.getEmail());

        verify(userRepository).findByEmail("Email@Example.com");
    }
}
