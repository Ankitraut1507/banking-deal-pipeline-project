package com.bank.pipeline.controller;

import com.bank.pipeline.dto.*;
import com.bank.pipeline.mapper.DealMapper;
import com.bank.pipeline.model.Deal;
import com.bank.pipeline.model.DealStage;
import com.bank.pipeline.service.DealService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;

    // Helper method to parse DealStage enum from String
    private DealStage parseStageEnum(String stage) {
        if (stage == null || stage.trim().isEmpty()) {
            return null;
        }
        
        try {
            return DealStage.valueOf(stage.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Helper method to check if user is admin
    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // CREATE DEAL
    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> createDeal(
            @RequestBody DealCreateRequest request,
            Authentication authentication) {

        Deal deal = dealService.createDeal(request, authentication.getName());

        boolean isAdmin = isAdmin(authentication);

        return isAdmin
                ? ResponseEntity.ok(DealMapper.toAdminResponse(deal))
                : ResponseEntity.ok(DealMapper.toUserResponse(deal));
    }

    // GET DEALS (ROLE AWARE)
    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Page<?>> getDeals(
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String sector,
            Pageable pageable,
            Authentication authentication) {

        DealStage stageEnum = parseStageEnum(stage);
        if (stage != null && stageEnum == null) {
            return ResponseEntity.badRequest().build();
        }

        boolean isAdmin = isAdmin(authentication);

        Page<?> response = isAdmin
                ? dealService.getDealsFiltered(stageEnum, sector, pageable)
                .map(DealMapper::toAdminResponse)
                : dealService.getDealsFiltered(stageEnum, sector, pageable)
                .map(DealMapper::toUserResponse);

        return ResponseEntity.ok(response);
    }

    // GET MY DEALS
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Page<DealUserResponse>> getMyDeals(
            Pageable pageable,
            Authentication authentication) {

        return ResponseEntity.ok(
                dealService.getDealsByOwner(authentication.getName(), pageable)
                        .map(DealMapper::toUserResponse)
        );
    }

    // PATCH DEAL (USER + ADMIN, NON-SENSITIVE)
    @PatchMapping("/{dealId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<DealUserResponse> patchDeal(
            @PathVariable String dealId,
            @RequestBody DealUpdateRequest request,
            Authentication authentication) {

        Deal deal = dealService.updateDeal(dealId, request, authentication.getName());
        return ResponseEntity.ok(DealMapper.toUserResponse(deal));
    }

    // PATCH DEAL VALUE (ADMIN ONLY)
    @PatchMapping("/{dealId}/value")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DealAdminResponse> updateDealValue(
            @PathVariable String dealId,
            @RequestBody DealUpdateRequest request) {

        Deal deal = dealService.updateDealValue(dealId, request);
        return ResponseEntity.ok(DealMapper.toAdminResponse(deal));
    }

    //Delete Deal
    @DeleteMapping("/{dealId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDeal(@PathVariable String dealId) {
        dealService.deleteDeal(dealId);
        return ResponseEntity.noContent().build();
    }

    // ---------------- ADMIN: VIEW ALL DEALS ----------------
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<DealAdminResponse>> getAllDealsAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String sector) {

        DealStage stageEnum = parseStageEnum(stage);
        if (stage != null && stageEnum == null) {
            return ResponseEntity.badRequest().build();
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<DealAdminResponse> response =
                dealService.getDealsFiltered(stageEnum, sector, pageable)
                        .map(DealMapper::toAdminResponse);

        return ResponseEntity.ok(response);
    }

    // GET SINGLE DEAL BY ID
    @GetMapping("/{dealId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> getDealById(
            @PathVariable String dealId,
            Authentication authentication) {

        String username = authentication.getName();
        Deal deal = dealService.getDealById(dealId, username);

        boolean isAdmin = isAdmin(authentication);

        return isAdmin
                ? ResponseEntity.ok(DealMapper.toAdminResponse(deal))
                : ResponseEntity.ok(DealMapper.toUserResponse(deal));
    }

    // SEARCH DEALS BY TEXT
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Page<?>> searchDeals(
            @RequestParam String query,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String sector,
            Pageable pageable,
            Authentication authentication) {

        DealStage stageEnum = parseStageEnum(stage);
        if (stage != null && stageEnum == null) {
            return ResponseEntity.badRequest().build();
        }

        boolean isAdmin = isAdmin(authentication);

        Page<?> response = isAdmin
                ? dealService.searchDeals(query, stageEnum, sector, pageable)
                .map(DealMapper::toAdminResponse)
                : dealService.searchDeals(query, stageEnum, sector, pageable)
                .map(DealMapper::toUserResponse);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{dealId}/notes")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> addNote(
            @PathVariable String dealId,
            @RequestBody DealNoteCreateRequest request,
            Authentication authentication
    ) {
        Deal deal = dealService.addNote(
                dealId,
                authentication.getName(),
                request.getNote()
        );

        boolean isAdmin = isAdmin(authentication);

        return isAdmin
                ? ResponseEntity.ok(DealMapper.toAdminResponse(deal))
                : ResponseEntity.ok(DealMapper.toUserResponse(deal));
    }

    @DeleteMapping("/{dealId}/notes/{noteId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> deleteNote(
            @PathVariable String dealId,
            @PathVariable String noteId,
            Authentication authentication) {

        Deal deal = dealService.deleteNote(
                dealId,
                noteId,
                authentication.getName()
        );

        boolean isAdmin = isAdmin(authentication);

        return ResponseEntity.ok(
                isAdmin
                        ? DealMapper.toAdminResponse(deal)
                        : DealMapper.toUserResponse(deal)
        );
    }




}
