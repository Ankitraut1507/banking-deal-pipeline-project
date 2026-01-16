package com.bank.pipeline.mapper;

import com.bank.pipeline.dto.DealAdminResponse;
import com.bank.pipeline.dto.DealUserResponse;
import com.bank.pipeline.model.Deal;

public final class DealMapper {

    private DealMapper() {
        // utility class - prevent instantiation
    }

    // -------- USER VIEW --------
    public static DealUserResponse toUserResponse(Deal deal) {
        return DealUserResponse.builder()
                .id(deal.getId())
                .title(deal.getTitle())
                .sector(deal.getSector())
                .dealType(deal.getDealType())
                .stage(deal.getStage())
                .notes(deal.getNotes())
                .ownerId(deal.getOwnerId())
                .createdAt(deal.getCreatedAt())
                .updatedAt(deal.getUpdatedAt())
                .build();
    }

    // -------- ADMIN VIEW --------
    public static DealAdminResponse toAdminResponse(Deal deal) {
        return DealAdminResponse.builder()
                .id(deal.getId())
                .title(deal.getTitle())
                .sector(deal.getSector())
                .dealType(deal.getDealType())
                .stage(deal.getStage())
                .dealValue(deal.getDealValue()) // ✅ sensitive
                .notes(deal.getNotes())
                .ownerId(deal.getOwnerId())
                .createdAt(deal.getCreatedAt())
                .updatedAt(deal.getUpdatedAt())
                .build();
    }
}
