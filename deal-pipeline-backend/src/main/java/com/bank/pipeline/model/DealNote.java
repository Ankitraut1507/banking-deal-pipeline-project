package com.bank.pipeline.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealNote {
    private String noteId;
    private String userId;
    private String note;
    private Instant createdAt;

    public DealNote(String userId, String note) {
        this.noteId = UUID.randomUUID().toString();
        this.userId = userId;
        this.note = note;
        this.createdAt = Instant.now();
    }
}
