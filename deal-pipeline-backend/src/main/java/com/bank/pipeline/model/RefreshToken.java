package com.bank.pipeline.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "refresh_tokens")
public class RefreshToken {

    @Id
    private String id;

    // Random string (UUID)
    private String token;

    // Reference to User (store userId, not entire object)
    private String userId;

    // Expiration timestamp
    private Instant expiryDate;

    // For logout / security
    private boolean revoked;
}
