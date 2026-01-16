package com.bank.pipeline.mapper;

import com.bank.pipeline.dto.CreateUserRequest;
import com.bank.pipeline.dto.UserResponse;
import com.bank.pipeline.model.User;

/**
 * Mapper for User entity and DTOs.
 * Pure transformation logic only.
 */
public class UserMapper {

    private UserMapper() {
        // prevent instantiation
    }

    // DTO → Entity
    public static User toEntity(CreateUserRequest request) {

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        return user;
    }

    // Entity → DTO
    public static UserResponse toResponse(User user){
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}