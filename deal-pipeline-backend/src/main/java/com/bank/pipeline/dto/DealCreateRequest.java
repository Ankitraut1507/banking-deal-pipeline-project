package com.bank.pipeline.dto;

import com.bank.pipeline.model.DealType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DealCreateRequest {

    private String title;
    private String sector;
    private DealType dealType;
    private String notes;
    private Double dealValue;

    // dealValue NOT allowed here
    // stage NOT allowed (defaulted to LEAD)
}
