package com.bank.pipeline.dto;

import com.bank.pipeline.model.DealNote;
import com.bank.pipeline.model.DealStage;
import com.bank.pipeline.model.DealType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DealUpdateRequest {

    private String title;
    private String sector;
    private DealType dealType;
    private DealStage stage;
    private Double dealValue; // Optional, admin-only field
}
