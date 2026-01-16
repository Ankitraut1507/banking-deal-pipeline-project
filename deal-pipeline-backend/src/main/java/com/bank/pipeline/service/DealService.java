package com.bank.pipeline.service;

import com.bank.pipeline.dto.DealUpdateRequest;
import com.bank.pipeline.dto.DealCreateRequest;
import com.bank.pipeline.model.Deal;
import com.bank.pipeline.model.DealStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DealService {

    Deal createDeal(DealCreateRequest request, String userId);

    Deal updateDeal(String dealId, DealUpdateRequest request, String userId);


    Page<Deal> getDealsByOwner(String userId, Pageable pageable);

    // NEW (FILTERING)
    Page<Deal> getDealsFiltered(
            DealStage stage,
            String sector,
            Pageable pageable
    );

    Deal updateDealValue(String dealId, DealUpdateRequest request);

    void deleteDeal(String dealId);

    Deal getDealById(String dealId, String userId);


    Deal addNote(String dealId, String userId, String note);

    Deal deleteNote(String dealId, String noteId, String userId);

    Page<Deal> searchDeals(String query, DealStage stage, String sector, Pageable pageable);



}
