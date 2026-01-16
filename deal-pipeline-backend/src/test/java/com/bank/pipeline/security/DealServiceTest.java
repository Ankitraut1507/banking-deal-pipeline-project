package com.bank.pipeline.security;

import com.bank.pipeline.dto.DealCreateRequest;
import com.bank.pipeline.dto.DealUpdateRequest;
import com.bank.pipeline.exception.ResourceNotFoundException;
import com.bank.pipeline.model.Deal;
import com.bank.pipeline.model.DealNote;
import com.bank.pipeline.model.DealStage;
import com.bank.pipeline.model.DealType;
import com.bank.pipeline.repository.DealRepository;
import com.bank.pipeline.service.DealServiceImpl;
import com.bank.pipeline.util.SecurityTestUtils;
import com.bank.pipeline.util.TestDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

    @Mock
    private DealRepository dealRepository;

    @InjectMocks
    private DealServiceImpl dealService;

    private Deal testDeal;
    private DealCreateRequest createRequest;
    private DealUpdateRequest updateRequest;
    private String userId;
    private String adminId;
    private Pageable pageable;

    @BeforeEach
    void setup() {
        testDeal = TestDataBuilder.createTestDeal();
        createRequest = TestDataBuilder.createDealCreateRequest();
        updateRequest = TestDataBuilder.createDealUpdateRequest();
        userId = "user123";
        adminId = "admin123";
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void createDeal_userRole_shouldSetOwnerDefaultStageAndNullValue() {
        // Arrange
        SecurityTestUtils.setupUserSecurityContext();
        when(dealRepository.save(any(Deal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Deal deal = dealService.createDeal(createRequest, userId);

        // Assert
        assertNotNull(deal);
        assertEquals(createRequest.getTitle(), deal.getTitle());
        assertEquals(createRequest.getSector(), deal.getSector());
        assertEquals(userId, deal.getOwnerId());
        assertEquals(DealStage.LEAD, deal.getStage());
        assertNull(deal.getDealValue()); // User cannot set deal value

        verify(dealRepository).save(any(Deal.class));
    }

    @Test
    void createDeal_adminRole_shouldSetOwnerDefaultStageAndValue() {
        // Arrange
        SecurityTestUtils.setupAdminSecurityContext();
        when(dealRepository.save(any(Deal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Deal deal = dealService.createDeal(createRequest, adminId);

        // Assert
        assertNotNull(deal);
        assertEquals(createRequest.getTitle(), deal.getTitle());
        assertEquals(createRequest.getSector(), deal.getSector());
        assertEquals(adminId, deal.getOwnerId());
        assertEquals(DealStage.LEAD, deal.getStage());
        assertEquals(createRequest.getDealValue(), deal.getDealValue()); // Admin can set deal value

        verify(dealRepository).save(any(Deal.class));
    }

    @Test
    void updateDeal_ownerUpdatingOwnDeal_shouldUpdateSuccessfully() {
        // Arrange
        SecurityTestUtils.setupUserSecurityContext();
        testDeal.setOwnerId(userId);
        when(dealRepository.findById(testDeal.getId()))
                .thenReturn(Optional.of(testDeal));
        when(dealRepository.save(any(Deal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Deal result = dealService.updateDeal(testDeal.getId(), updateRequest, userId);

        // Assert
        assertNotNull(result);
        assertEquals(updateRequest.getTitle(), result.getTitle());
        assertEquals(updateRequest.getSector(), result.getSector());
        assertEquals(updateRequest.getDealType(), result.getDealType());
        assertEquals(updateRequest.getStage(), result.getStage());

        verify(dealRepository).findById(testDeal.getId());
        verify(dealRepository).save(any(Deal.class));
    }

    @Test
    void updateDeal_adminUpdatingAnyDeal_shouldUpdateSuccessfully() {
        // Arrange
        SecurityTestUtils.setupAdminSecurityContext();
        testDeal.setOwnerId("otherUser");
        when(dealRepository.findById(testDeal.getId()))
                .thenReturn(Optional.of(testDeal));
        when(dealRepository.save(any(Deal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Deal result = dealService.updateDeal(testDeal.getId(), updateRequest, adminId);

        // Assert
        assertNotNull(result);
        assertEquals(updateRequest.getTitle(), result.getTitle());

        verify(dealRepository).findById(testDeal.getId());
        verify(dealRepository).save(any(Deal.class));
    }

    @Test
    void updateDeal_unauthorizedUser_shouldThrowAccessDeniedException() {
        // Arrange
        SecurityTestUtils.setupUserSecurityContext();
        testDeal.setOwnerId("otherUser");
        when(dealRepository.findById(testDeal.getId()))
                .thenReturn(Optional.of(testDeal));

        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> dealService.updateDeal(testDeal.getId(), updateRequest, userId)
        );

        assertEquals("Not allowed to update this deal", exception.getMessage());

        verify(dealRepository).findById(testDeal.getId());
        verify(dealRepository, never()).save(any());
    }

    @Test
    void updateDeal_nonExistentDeal_shouldThrowResourceNotFoundException() {
        // Arrange
        when(dealRepository.findById(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> dealService.updateDeal("nonexistent", updateRequest, userId)
        );

        assertEquals("Deal not found: nonexistent", exception.getMessage());

        verify(dealRepository).findById("nonexistent");
        verify(dealRepository, never()).save(any());
    }

    @Test
    void getDealsByOwner_shouldReturnUserDeals() {
        // Arrange
        Page<Deal> expectedPage = new PageImpl<>(List.of(testDeal));
        when(dealRepository.findByOwnerId(userId, pageable))
                .thenReturn(expectedPage);

        // Act
        Page<Deal> result = dealService.getDealsByOwner(userId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testDeal.getId(), result.getContent().get(0).getId());

        verify(dealRepository).findByOwnerId(userId, pageable);
    }

    @Test
    void getDealsFiltered_withStageAndSector_shouldReturnFilteredDeals() {
        // Arrange
        Page<Deal> expectedPage = new PageImpl<>(List.of(testDeal));
        when(dealRepository.findByStageAndSector(DealStage.LEAD, "Technology", pageable))
                .thenReturn(expectedPage);

        // Act
        Page<Deal> result = dealService.getDealsFiltered(DealStage.LEAD, "Technology", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(dealRepository).findByStageAndSector(DealStage.LEAD, "Technology", pageable);
    }

    @Test
    void getDealsFiltered_withStageOnly_shouldReturnFilteredDeals() {
        // Arrange
        Page<Deal> expectedPage = new PageImpl<>(List.of(testDeal));
        when(dealRepository.findByStage(DealStage.LEAD, pageable))
                .thenReturn(expectedPage);

        // Act
        Page<Deal> result = dealService.getDealsFiltered(DealStage.LEAD, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(dealRepository).findByStage(DealStage.LEAD, pageable);
    }

    @Test
    void getDealsFiltered_withSectorOnly_shouldReturnFilteredDeals() {
        // Arrange
        Page<Deal> expectedPage = new PageImpl<>(List.of(testDeal));
        when(dealRepository.findBySector("Technology", pageable))
                .thenReturn(expectedPage);

        // Act
        Page<Deal> result = dealService.getDealsFiltered(null, "Technology", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(dealRepository).findBySector("Technology", pageable);
    }

    @Test
    void getDealsFiltered_withNoFilters_shouldReturnAllDeals() {
        // Arrange
        Page<Deal> expectedPage = new PageImpl<>(List.of(testDeal));
        when(dealRepository.findAll(pageable))
                .thenReturn(expectedPage);

        // Act
        Page<Deal> result = dealService.getDealsFiltered(null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(dealRepository).findAll(pageable);
    }

    @Test
    void updateDealValue_admin_shouldUpdateValue() {
        // Arrange
        when(dealRepository.findById(testDeal.getId()))
                .thenReturn(Optional.of(testDeal));
        when(dealRepository.save(any(Deal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Deal result = dealService.updateDealValue(testDeal.getId(), updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(updateRequest.getDealValue(), result.getDealValue());

        verify(dealRepository).findById(testDeal.getId());
        verify(dealRepository).save(any(Deal.class));
    }

    @Test
    void updateDealValue_nonExistentDeal_shouldThrowResourceNotFoundException() {
        // Arrange
        when(dealRepository.findById(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> dealService.updateDealValue("nonexistent", updateRequest)
        );

        assertEquals("Deal not found: nonexistent", exception.getMessage());

        verify(dealRepository).findById("nonexistent");
        verify(dealRepository, never()).save(any());
    }

    @Test
    void deleteDeal_existingDeal_shouldDeleteSuccessfully() {
        // Arrange
        when(dealRepository.findById(testDeal.getId()))
                .thenReturn(Optional.of(testDeal));

        // Act
        dealService.deleteDeal(testDeal.getId());

        // Assert
        verify(dealRepository).findById(testDeal.getId());
        verify(dealRepository).delete(testDeal);
    }

    @Test
    void deleteDeal_nonExistentDeal_shouldThrowResourceNotFoundException() {
        // Arrange
        when(dealRepository.findById(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> dealService.deleteDeal("nonexistent")
        );

        assertEquals("Deal not found: nonexistent", exception.getMessage());

        verify(dealRepository).findById("nonexistent");
        verify(dealRepository, never()).delete(any());
    }

    @Test
    void getDealById_ownerAccessingOwnDeal_shouldReturnDeal() {
        // Arrange
        testDeal.setOwnerId(userId);
        when(dealRepository.findById(testDeal.getId()))
                .thenReturn(Optional.of(testDeal));

        // Act
        Deal result = dealService.getDealById(testDeal.getId(), userId);

        // Assert
        assertNotNull(result);
        assertEquals(testDeal.getId(), result.getId());

        verify(dealRepository).findById(testDeal.getId());
    }

    @Test
    void getDealById_userAccessingOtherUsersDeal_shouldThrowAccessDeniedException() {
        // Arrange
        testDeal.setOwnerId("otherUser");
        when(dealRepository.findById(testDeal.getId()))
                .thenReturn(Optional.of(testDeal));

        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> dealService.getDealById(testDeal.getId(), userId)
        );

        assertEquals("You do not own this deal", exception.getMessage());

        verify(dealRepository).findById(testDeal.getId());
    }

    @Test
    void getDealById_nonExistentDeal_shouldThrowResourceNotFoundException() {
        // Arrange
        when(dealRepository.findById(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> dealService.getDealById("nonexistent", userId)
        );

        assertEquals("Deal not found: nonexistent", exception.getMessage());

        verify(dealRepository).findById("nonexistent");
    }

    @Test
    void addNote_existingDeal_shouldAddNoteSuccessfully() {
        // Arrange
        testDeal.setNotes(new ArrayList<>());
        when(dealRepository.findById(testDeal.getId()))
                .thenReturn(Optional.of(testDeal));
        when(dealRepository.save(any(Deal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String noteText = "Test note content";

        // Act
        Deal result = dealService.addNote(testDeal.getId(), userId, noteText);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getNotes().size());
        assertEquals(noteText, result.getNotes().get(0).getNote());
        assertEquals(userId, result.getNotes().get(0).getUserId());

        verify(dealRepository).findById(testDeal.getId());
        verify(dealRepository).save(any(Deal.class));
    }

    @Test
    void addNote_nonExistentDeal_shouldThrowResourceNotFoundException() {
        // Arrange
        when(dealRepository.findById(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> dealService.addNote("nonexistent", userId, "note")
        );

        assertEquals("Deal not found", exception.getMessage());

        verify(dealRepository).findById("nonexistent");
        verify(dealRepository, never()).save(any());
    }

    @Test
    void deleteNote_ownerDeletingOwnNote_shouldDeleteSuccessfully() {
        // Arrange
        SecurityTestUtils.setupUserSecurityContext();
        DealNote note = new DealNote(userId, "Test note");
        testDeal.setNotes(new ArrayList<>(List.of(note)));
        when(dealRepository.findById(testDeal.getId()))
                .thenReturn(Optional.of(testDeal));
        when(dealRepository.save(any(Deal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Deal result = dealService.deleteNote(testDeal.getId(), note.getNoteId(), userId);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getNotes().size());

        verify(dealRepository).findById(testDeal.getId());
        verify(dealRepository).save(any(Deal.class));
    }

    @Test
    void deleteNote_adminDeletingAnyNote_shouldDeleteSuccessfully() {
        // Arrange
        SecurityTestUtils.setupAdminSecurityContext();
        DealNote note = new DealNote("otherUser", "Test note");
        testDeal.setNotes(new ArrayList<>(List.of(note)));
        when(dealRepository.findById(testDeal.getId()))
                .thenReturn(Optional.of(testDeal));
        when(dealRepository.save(any(Deal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Deal result = dealService.deleteNote(testDeal.getId(), note.getNoteId(), adminId);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getNotes().size());

        verify(dealRepository).findById(testDeal.getId());
        verify(dealRepository).save(any(Deal.class));
    }

    @Test
    void deleteNote_unauthorizedUser_shouldThrowAccessDeniedException() {
        // Arrange
        SecurityTestUtils.setupUserSecurityContext();
        DealNote note = new DealNote("otherUser", "Test note");
        testDeal.setNotes(new ArrayList<>(List.of(note)));
        when(dealRepository.findById(testDeal.getId()))
                .thenReturn(Optional.of(testDeal));

        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> dealService.deleteNote(testDeal.getId(), note.getNoteId(), userId)
        );

        assertEquals("Not allowed to delete this note", exception.getMessage());

        verify(dealRepository).findById(testDeal.getId());
        verify(dealRepository, never()).save(any());
    }

    @Test
    void searchDeals_withQuery_shouldReturnMatchingDeals() {
        // Arrange
        testDeal.setTitle("Technology Deal");
        List<Deal> allDeals = List.of(testDeal);
        Page<Deal> allDealsPage = new PageImpl<>(allDeals);
        when(dealRepository.findAll(pageable))
                .thenReturn(allDealsPage);

        // Act
        Page<Deal> result = dealService.searchDeals("Technology", null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testDeal.getId(), result.getContent().get(0).getId());

        verify(dealRepository).findAll(pageable);
    }

    @Test
    void searchDeals_withEmptyQuery_shouldReturnAllDeals() {
        // Arrange
        List<Deal> allDeals = List.of(testDeal);
        Page<Deal> allDealsPage = new PageImpl<>(allDeals);
        when(dealRepository.findAll(pageable))
                .thenReturn(allDealsPage);

        // Act
        Page<Deal> result = dealService.searchDeals("", null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(dealRepository).findAll(pageable);
    }

    @Test
    void searchDeals_withNullQuery_shouldReturnAllDeals() {
        // Arrange
        List<Deal> allDeals = List.of(testDeal);
        Page<Deal> allDealsPage = new PageImpl<>(allDeals);
        when(dealRepository.findAll(pageable))
                .thenReturn(allDealsPage);

        // Act
        Page<Deal> result = dealService.searchDeals(null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(dealRepository).findAll(pageable);
    }

    @Test
    void searchDeals_withWhitespaceQuery_shouldReturnAllDeals() {
        // Arrange
        List<Deal> allDeals = List.of(testDeal);
        Page<Deal> allDealsPage = new PageImpl<>(allDeals);
        when(dealRepository.findAll(pageable))
                .thenReturn(allDealsPage);

        // Act
        Page<Deal> result = dealService.searchDeals("   ", null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(dealRepository).findAll(pageable);
    }

    @Test
    void searchDeals_withQueryAndStage_shouldReturnFilteredDeals() {
        // Arrange
        testDeal.setTitle("Technology Deal");
        testDeal.setStage(DealStage.LEAD);
        List<Deal> allDeals = List.of(testDeal);
        Page<Deal> allDealsPage = new PageImpl<>(allDeals);
        when(dealRepository.findAll(pageable))
                .thenReturn(allDealsPage);

        // Act
        Page<Deal> result = dealService.searchDeals("Technology", DealStage.LEAD, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testDeal.getId(), result.getContent().get(0).getId());

        verify(dealRepository).findAll(pageable);
    }

    @Test
    void searchDeals_withQueryAndSector_shouldReturnFilteredDeals() {
        // Arrange
        testDeal.setTitle("Technology Deal");
        testDeal.setSector("Technology");
        List<Deal> allDeals = List.of(testDeal);
        Page<Deal> allDealsPage = new PageImpl<>(allDeals);
        when(dealRepository.findAll(pageable))
                .thenReturn(allDealsPage);

        // Act
        Page<Deal> result = dealService.searchDeals("Technology", null, "Technology", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testDeal.getId(), result.getContent().get(0).getId());

        verify(dealRepository).findAll(pageable);
    }

    @Test
    void searchDeals_caseInsensitiveSearch_shouldMatchDifferentCases() {
        // Arrange
        testDeal.setTitle("Technology Deal");
        testDeal.setSector("FINANCE");
        List<Deal> allDeals = List.of(testDeal);
        Page<Deal> allDealsPage = new PageImpl<>(allDeals);
        when(dealRepository.findAll(pageable))
                .thenReturn(allDealsPage);

        // Act
        Page<Deal> result1 = dealService.searchDeals("technology", null, null, pageable);
        Page<Deal> result2 = dealService.searchDeals("finance", null, null, pageable);

        // Assert
        assertEquals(1, result1.getContent().size());
        assertEquals(1, result2.getContent().size());

        verify(dealRepository, times(2)).findAll(pageable);
    }

    @Test
    void searchDeals_noMatchingDeals_shouldReturnEmptyList() {
        // Arrange
        testDeal.setTitle("Technology Deal");
        List<Deal> allDeals = List.of(testDeal);
        Page<Deal> allDealsPage = new PageImpl<>(allDeals);
        when(dealRepository.findAll(pageable))
                .thenReturn(allDealsPage);

        // Act
        Page<Deal> result = dealService.searchDeals("Banking", null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getContent().size());

        verify(dealRepository).findAll(pageable);
    }

    @Test
    void searchDeals_withPartialMatch_shouldReturnMatchingDeals() {
        // Arrange
        testDeal.setTitle("Advanced Technology Solutions");
        List<Deal> allDeals = List.of(testDeal);
        Page<Deal> allDealsPage = new PageImpl<>(allDeals);
        when(dealRepository.findAll(pageable))
                .thenReturn(allDealsPage);

        // Act
        Page<Deal> result = dealService.searchDeals("Technology", null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testDeal.getId(), result.getContent().get(0).getId());

        verify(dealRepository).findAll(pageable);
    }

    @Test
    void deleteNote_adminCanDeleteAnyNote_shouldDeleteSuccessfully() {
        // Arrange
        SecurityTestUtils.setupAdminSecurityContext();
        DealNote note = new DealNote("otherUser", "Test note");
        testDeal.getNotes().add(note);
        
        when(dealRepository.findById(testDeal.getId()))
                .thenReturn(Optional.of(testDeal));
        when(dealRepository.save(any(Deal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Deal result = dealService.deleteNote(testDeal.getId(), note.getNoteId(), userId);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getNotes().size());

        verify(dealRepository).findById(testDeal.getId());
        verify(dealRepository).save(testDeal);
    }

    @Test
    void deleteNote_userCannotDeleteOthersNote_shouldThrowAccessDenied() {
        // Arrange
        SecurityTestUtils.setupUserSecurityContext();
        DealNote note = new DealNote("otherUser", "Test note");
        testDeal.getNotes().add(note);
        
        when(dealRepository.findById(testDeal.getId()))
                .thenReturn(Optional.of(testDeal));

        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> dealService.deleteNote(testDeal.getId(), note.getNoteId(), userId)
        );

        assertEquals("Not allowed to delete this note", exception.getMessage());

        verify(dealRepository).findById(testDeal.getId());
        verify(dealRepository, never()).save(any());
    }

    @Test
    void deleteNote_nonExistentNote_shouldThrowAccessDenied() {
        // Arrange
        SecurityTestUtils.setupUserSecurityContext();
        DealNote note = new DealNote(userId, "Test note");
        testDeal.getNotes().add(note);
        
        when(dealRepository.findById(testDeal.getId()))
                .thenReturn(Optional.of(testDeal));

        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> dealService.deleteNote(testDeal.getId(), "nonexistent", userId)
        );

        assertEquals("Not allowed to delete this note", exception.getMessage());

        verify(dealRepository).findById(testDeal.getId());
        verify(dealRepository, never()).save(any());
    }

    @Test
    void getDealsFiltered_withBothStageAndSector_shouldReturnFilteredDeals() {
        // Arrange
        testDeal.setStage(DealStage.LEAD);
        testDeal.setSector("Technology");
        List<Deal> filteredDeals = List.of(testDeal);
        Page<Deal> filteredPage = new PageImpl<>(filteredDeals);
        
        when(dealRepository.findByStageAndSector(DealStage.LEAD, "Technology", pageable))
                .thenReturn(filteredPage);

        // Act
        Page<Deal> result = dealService.getDealsFiltered(DealStage.LEAD, "Technology", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testDeal.getId(), result.getContent().get(0).getId());

        verify(dealRepository).findByStageAndSector(DealStage.LEAD, "Technology", pageable);
    }

    @Test
    void getDealsFiltered_withOnlyStage_shouldReturnFilteredDeals() {
        // Arrange
        testDeal.setStage(DealStage.LEAD);
        List<Deal> filteredDeals = List.of(testDeal);
        Page<Deal> filteredPage = new PageImpl<>(filteredDeals);
        
        when(dealRepository.findByStage(DealStage.LEAD, pageable))
                .thenReturn(filteredPage);

        // Act
        Page<Deal> result = dealService.getDealsFiltered(DealStage.LEAD, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testDeal.getId(), result.getContent().get(0).getId());

        verify(dealRepository).findByStage(DealStage.LEAD, pageable);
    }

    @Test
    void getDealsFiltered_withOnlySector_shouldReturnFilteredDeals() {
        // Arrange
        testDeal.setSector("Technology");
        List<Deal> filteredDeals = List.of(testDeal);
        Page<Deal> filteredPage = new PageImpl<>(filteredDeals);
        
        when(dealRepository.findBySector("Technology", pageable))
                .thenReturn(filteredPage);

        // Act
        Page<Deal> result = dealService.getDealsFiltered(null, "Technology", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testDeal.getId(), result.getContent().get(0).getId());

        verify(dealRepository).findBySector("Technology", pageable);
    }

    @Test
    void getDealsFiltered_withNullParameters_shouldReturnAllDeals() {
        // Arrange
        List<Deal> allDeals = List.of(testDeal);
        Page<Deal> allDealsPage = new PageImpl<>(allDeals);
        
        when(dealRepository.findAll(pageable))
                .thenReturn(allDealsPage);

        // Act
        Page<Deal> result = dealService.getDealsFiltered(null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testDeal.getId(), result.getContent().get(0).getId());

        verify(dealRepository).findAll(pageable);
    }

    @Test
    void searchDeals_withStageFilter_shouldReturnFilteredDeals() {
        // Arrange
        testDeal.setStage(DealStage.LEAD);
        List<Deal> allDeals = List.of(testDeal);
        Page<Deal> allDealsPage = new PageImpl<>(allDeals);
        when(dealRepository.findAll(pageable))
                .thenReturn(allDealsPage);

        // Act
        Page<Deal> result = dealService.searchDeals("Deal", DealStage.LEAD, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(dealRepository).findAll(pageable);
    }

    @Test
    void searchDeals_withSectorFilter_shouldReturnFilteredDeals() {
        // Arrange
        List<Deal> allDeals = List.of(testDeal);
        Page<Deal> allDealsPage = new PageImpl<>(allDeals);
        when(dealRepository.findAll(pageable))
                .thenReturn(allDealsPage);

        // Act
        Page<Deal> result = dealService.searchDeals("Deal", null, "Technology", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(dealRepository).findAll(pageable);
    }

    @AfterEach
    void cleanup() {
        SecurityTestUtils.clearSecurityContext();
    }
}
