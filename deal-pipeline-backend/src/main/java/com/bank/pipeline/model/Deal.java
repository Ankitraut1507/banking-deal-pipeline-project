
package com.bank.pipeline.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "deals")
public class Deal{

    @Id
    private String id;

    // Basic info
    private String title;          // Deal summary/title
    private String sector;         // Technology, Healthcare, etc.
    private DealType dealType;

    // Pipeline
    private DealStage stage;

    // Sensitive (ADMIN only)
    private Double dealValue;

    // Collaboration
    private List<DealNote> notes;


    // Ownership
    private String ownerId;        // User who created/owns deal

    // Audit
    private Instant createdAt;
    private Instant updatedAt;
}
