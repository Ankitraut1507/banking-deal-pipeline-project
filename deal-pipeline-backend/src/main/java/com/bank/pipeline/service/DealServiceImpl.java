package com.bank.pipeline.service;


import com.bank.pipeline.dto.DealUpdateRequest;
import com.bank.pipeline.dto.DealCreateRequest;
import com.bank.pipeline.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import com.bank.pipeline.model.Deal;
import com.bank.pipeline.model.DealNote;
import com.bank.pipeline.model.DealStage;
import com.bank.pipeline.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {

    private final DealRepository dealRepository;

    // ---------------- USER ----------------

    @Override
    public Deal createDeal(DealCreateRequest request, String userId) {

        boolean isAdmin =
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Deal.DealBuilder builder = Deal.builder()
                .title(request.getTitle())
                .sector(request.getSector())
                .dealType(request.getDealType())
                .notes(new ArrayList<>())
                .stage(DealStage.LEAD)
                .ownerId(userId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now());


        // ✅ ONLY ADMIN can set dealValue at creation
        if (isAdmin) {
            builder.dealValue(request.getDealValue());
        } else {
            builder.dealValue(null);
        }

        return dealRepository.save(builder.build());
    }




    @Override
    public Deal updateDeal(String dealId, DealUpdateRequest request, String userId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new ResourceNotFoundException("Deal not found: " + dealId));

        boolean isOwner = deal.getOwnerId().equals(userId);
        boolean isAdmin = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Not allowed to update this deal");
        }

        // ✅ UPDATE ONLY IF VALUE IS PROVIDED
        if (request.getTitle() != null) {
            deal.setTitle(request.getTitle());
        }
        if (request.getSector() != null) {
            deal.setSector(request.getSector());
        }
        if (request.getDealType() != null) {
            deal.setDealType(request.getDealType());
        }
        if (request.getStage() != null) {
            deal.setStage(request.getStage());
        }

        deal.setUpdatedAt(Instant.now());
        return dealRepository.save(deal);
    }




    @Override
    public Page<Deal> getDealsByOwner(
            String userId,
            Pageable pageable) {

        return dealRepository.findByOwnerId(userId, pageable);
    }

    // ---------------- ADMIN ----------------

    @Override
    public Deal updateDealValue(
            String dealId,
            DealUpdateRequest request) {

        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Deal not found: " + dealId));

        deal.setDealValue(request.getDealValue());
        deal.setUpdatedAt(Instant.now());

        return dealRepository.save(deal);
    }

    @Override
    public void deleteDeal(String dealId) {

        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Deal not found: " + dealId));

        dealRepository.delete(deal);
    }

    @Override
    public Page<Deal> getDealsFiltered(
            DealStage stage,
            String sector,
            Pageable pageable) {

        if (stage != null && sector != null) {
            return dealRepository.findByStageAndSector(stage, sector, pageable);
        }

        if (stage != null) {
            return dealRepository.findByStage(stage, pageable);
        }

        if (sector != null) {
            return dealRepository.findBySector(sector, pageable);
        }

        return dealRepository.findAll(pageable);
    }

    @Override
    public Deal getDealById(String dealId, String userId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new ResourceNotFoundException("Deal not found: " + dealId));

        if (!deal.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("You do not own this deal");
        }

        return deal;
    }

    @Override
    public Deal addNote(String dealId, String userId, String noteText) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new ResourceNotFoundException("Deal not found"));

        DealNote note = new DealNote(userId, noteText);
        deal.getNotes().add(note);

        deal.setUpdatedAt(Instant.now());
        return dealRepository.save(deal);
    }

    @Override
    public Deal deleteNote(String dealId, String noteId, String userId) {

        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new ResourceNotFoundException("Deal not found"));

        boolean isAdmin = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean removed = deal.getNotes().removeIf(note ->
                note.getNoteId().equals(noteId) &&
                        (isAdmin || note.getUserId().equals(userId))
        );

        if (!removed) {
            throw new AccessDeniedException("Not allowed to delete this note");
        }

        deal.setUpdatedAt(Instant.now());
        return dealRepository.save(deal);
    }

    @Override
    public Page<Deal> searchDeals(String query, DealStage stage, String sector, Pageable pageable) {
        // For now, implement basic text search on title and sector
        // In a real implementation, you might use MongoDB text indexes
        if (query == null || query.trim().isEmpty()) {
            return dealRepository.findAll(pageable);
        }

        // Simple case-insensitive search on title and sector
        String searchQuery = query.toLowerCase().trim();
        Page<Deal> allDeals = dealRepository.findAll(pageable);
        
        List<Deal> filteredDeals = allDeals.getContent().stream()
                .filter(deal -> {
                    boolean matchesTitle = deal.getTitle() != null && 
                            deal.getTitle().toLowerCase().contains(searchQuery);
                    boolean matchesSector = deal.getSector() != null && 
                            deal.getSector().toLowerCase().contains(searchQuery);
                    
                    return (matchesTitle || matchesSector);
                })
                .filter(deal -> stage == null || deal.getStage() == stage)
                .filter(deal -> sector == null || sector.isEmpty() || deal.getSector().equals(sector))
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(
                filteredDeals, 
                pageable, 
                filteredDeals.size()
        );
    }


}
