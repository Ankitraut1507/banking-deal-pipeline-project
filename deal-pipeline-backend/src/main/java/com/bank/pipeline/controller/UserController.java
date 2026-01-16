package com.bank.pipeline.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.bank.pipeline.dto.CreateUserRequest;
import com.bank.pipeline.dto.UserResponse;
import com.bank.pipeline.mapper.UserMapper;
import com.bank.pipeline.model.User;
import com.bank.pipeline.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // CREATE INITIAL ADMIN (NO AUTH REQUIRED - ONLY FOR SETUP)
    @PostMapping("/init-admin")
    public ResponseEntity<UserResponse> createInitialAdmin(
            @Valid @RequestBody CreateUserRequest request) {
        
        // Temporarily allow admin creation without check
        // In production, you should implement proper admin check
        
        // Force role to ADMIN for this endpoint
        User user = UserMapper.toEntity(request);
        user.setRole(com.bank.pipeline.model.Role.ADMIN);
        User createdUser = userService.createUser(user);
        UserResponse response = UserMapper.toResponse(createdUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Create User (ADMIN ONLY)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        User user = UserMapper.toEntity(request);
        User createdUser = userService.createUser(user);
        UserResponse response = UserMapper.toResponse(createdUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    //  Get User by Username
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(
            @PathVariable String username) {

        User user = userService.findByUsername(username);
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }

    // Get User by Email
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(
            @PathVariable String email) {

        User user = userService.findByEmail(email);
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }
    //  ADMIN ONLY — Get all users
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .toList();

        return ResponseEntity.ok(users);
    }

    // Get Current Logged-in User
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {

        String username = authentication.getName();
        User user = userService.findByUsername(username);

        return ResponseEntity.ok(UserMapper.toResponse(user));
    }

    // ---------------- ADMIN: USER MANAGEMENT ----------------
    
    // ADMIN ONLY — Promote user to ADMIN
    @PatchMapping("/{username}/make-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> promoteUserToAdmin(
            @PathVariable String username) {

        User updatedUser = userService.promoteToAdmin(username);
        return ResponseEntity.ok(UserMapper.toResponse(updatedUser));
    }

    @PatchMapping("/{username}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable String username,
            @RequestParam boolean active) {

        User updatedUser = userService.updateUserStatus(username, active);
        return ResponseEntity.ok(UserMapper.toResponse(updatedUser));
    }

    // DELETE USER (ADMIN ONLY)
    @DeleteMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }

    // RESET USER PASSWORD (ADMIN ONLY)
    @PutMapping("/{username}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> resetUserPassword(
            @PathVariable String username,
            @RequestBody Map<String, String> request) {
        
        String newPassword = request.get("newPassword");
        User updatedUser = userService.resetPassword(username, newPassword);
        return ResponseEntity.ok(UserMapper.toResponse(updatedUser));
    }


}
