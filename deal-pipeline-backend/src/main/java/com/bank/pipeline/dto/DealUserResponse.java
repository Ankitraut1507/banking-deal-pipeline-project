package com.bank.pipeline.dto;

import com.bank.pipeline.model.DealNote;
import com.bank.pipeline.model.DealStage;
import com.bank.pipeline.model.DealType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class DealUserResponse {

    private String id;
    private String title;
    private String sector;
    private DealType dealType;
    private DealStage stage;
    private List<DealNote> notes;
    private String ownerId;
    private Instant createdAt;
    private Instant updatedAt;

    // dealValue NOT exposed
}
