package com.bank.pipeline.repository;

import com.bank.pipeline.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User,String> {

    //it will find user by mail
    Optional<User> findByUsername(String username);

    //it will find user by mail
    Optional<User> findByEmail(String email);

    //it will check username already exist or not
    boolean existsByUsername(String username);

    //it will check mail already exist or not
    boolean existsByEmail(String email);

}
