package com.bank.pipeline.service;

import com.bank.pipeline.model.User;

import java.util.List;


public interface UserService {
    //will create new user
    User createUser(User user);

    //find user by username
    User findByUsername(String username);

    //find user by email
    User findByEmail(String email);

    //check if username exist
    boolean existsByUsername(String username);

    //check if email exist
    boolean existsByEmail(String email);

    //ADMIN use‑case
    List<User> findAll();

    //Promote User
    User promoteToAdmin(String username);

    User updateUserStatus(String username, boolean active);

    void deleteUser(String username);

    User resetPassword(String username, String newPassword);

}
