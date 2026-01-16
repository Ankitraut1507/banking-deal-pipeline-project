package com.bank.pipeline.model;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Getter
@Setter
public class User extends BaseDocument{

    @Indexed(unique = true,background = true)
    private String username;

    @Indexed(unique = true,background = true)
    private String email;

    private String password;

    private Role role;

    private boolean active=true;
}
