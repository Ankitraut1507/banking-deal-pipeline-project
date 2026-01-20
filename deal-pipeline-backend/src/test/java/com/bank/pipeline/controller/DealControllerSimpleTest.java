package com.bank.pipeline.controller;

import com.bank.pipeline.dto.DealCreateRequest;
import com.bank.pipeline.dto.DealUpdateRequest;
import com.bank.pipeline.model.Deal;
import com.bank.pipeline.model.DealStage;
import com.bank.pipeline.model.DealType;
import com.bank.pipeline.service.DealService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DealControllerSimpleTest {

    @Mock
    private DealService dealService;

    @InjectMocks
    private DealController dealController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Deal testDeal;
    private DealCreateRequest createRequest;
    private DealUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dealController).build();
        objectMapper = new ObjectMapper();

        testDeal = Deal.builder()
                .id("1")
                .title("Test Deal")
                .sector("Technology")
                .dealType(DealType.MERGER_ACQUISITION)
                .stage(DealStage.LEAD)
                .dealValue(1000000.0)
                .ownerId("testuser")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        createRequest = new DealCreateRequest();
        createRequest.setTitle("Test Deal");
        createRequest.setSector("Technology");
        createRequest.setDealType(DealType.MERGER_ACQUISITION);
        createRequest.setDealValue(1000000.0);

        updateRequest = new DealUpdateRequest();
        updateRequest.setTitle("Updated Deal");
        updateRequest.setSector("Healthcare");
        updateRequest.setStage(DealStage.PROSPECTING);
        updateRequest.setDealValue(2000000.0);
    }

    @Test
    void createDeal_invalidRequest_shouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/deals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createDeal_invalidContentType_shouldReturnUnsupportedMediaType() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/deals")
                .contentType(MediaType.TEXT_PLAIN)
                .content("invalid"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void createDeal_malformedJson_shouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/deals")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteDeal_shouldReturnNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/deals/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateDealValue_invalidRequest_shouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/deals/1/value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateDealValue_invalidContentType_shouldReturnUnsupportedMediaType() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/deals/1/value")
                .contentType(MediaType.TEXT_PLAIN)
                .content("invalid"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void updateDealValue_malformedJson_shouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/deals/1/value")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }
}
