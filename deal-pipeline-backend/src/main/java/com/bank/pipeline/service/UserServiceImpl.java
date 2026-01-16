package com.bank.pipeline.service;

import com.bank.pipeline.exception.BusinessException;
import com.bank.pipeline.exception.UserNotFoundException;
import com.bank.pipeline.model.Role;
import com.bank.pipeline.model.User;
import com.bank.pipeline.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(User user) {

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException(
                    "Username already exists: " + user.getUsername());
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessException(
                    "Email already exists: " + user.getEmail());
        }


        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Defaults
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }

        if (!user.isActive()) {
            user.setActive(true);
        }

        return userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with username not found: " + username));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with email not found: " + email));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }
    @Override
    public User promoteToAdmin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found: " + username));

        user.setRole(Role.ADMIN);
        return userRepository.save(user);
    }

    @Override
    public User updateUserStatus(String username, boolean active) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        user.setActive(active);
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        
        userRepository.delete(user);
    }

    @Override
    public User resetPassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

}
