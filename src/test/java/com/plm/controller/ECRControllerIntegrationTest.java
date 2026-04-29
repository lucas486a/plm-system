package com.plm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plm.entity.ECR;
import com.plm.entity.Part;
import com.plm.repository.ECRPartRepository;
import com.plm.repository.ECRRepository;
import com.plm.repository.PartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ECRControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ECRRepository ecrRepository;

    @Autowired
    private ECRPartRepository ecrPartRepository;

    @Autowired
    private PartRepository partRepository;

    private ECR savedECR;

    @BeforeEach
    void setUp() {
        ecrPartRepository.deleteAll();
        ecrRepository.deleteAll();
        partRepository.deleteAll();

        savedECR = ecrRepository.save(ECR.builder()
                .ecrNumber("ECR-001")
                .title("Test ECR")
                .description("A test engineering change request")
                .status("DRAFT")
                .priority("MEDIUM")
                .build());
    }

    @Test
    void createECR_shouldReturn201() throws Exception {
        String ecrJson = """
                {
                    "ecrNumber": "ECR-002",
                    "title": "New ECR",
                    "description": "A new ECR",
                    "priority": "HIGH"
                }
                """;

        mockMvc.perform(post("/api/ecrs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ecrJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ecrNumber").value("ECR-002"))
                .andExpect(jsonPath("$.title").value("New ECR"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void getECRById_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/ecrs/{id}", savedECR.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedECR.getId()))
                .andExpect(jsonPath("$.ecrNumber").value("ECR-001"))
                .andExpect(jsonPath("$.title").value("Test ECR"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void getECRByNumber_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/ecrs/number/{ecrNumber}", "ECR-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ecrNumber").value("ECR-001"))
                .andExpect(jsonPath("$.title").value("Test ECR"));
    }

    @Test
    void updateECR_shouldReturn200() throws Exception {
        String updateJson = """
                {
                    "ecrNumber": "ECR-001",
                    "title": "Updated ECR",
                    "description": "Updated description",
                    "priority": "HIGH"
                }
                """;

        mockMvc.perform(put("/api/ecrs/{id}", savedECR.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated ECR"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void deleteECR_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/ecrs/{id}", savedECR.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void listECRs_shouldReturnPaginatedResults() throws Exception {
        ecrRepository.save(ECR.builder()
                .ecrNumber("ECR-002").title("ECR 2").status("DRAFT").priority("LOW").build());
        ecrRepository.save(ECR.builder()
                .ecrNumber("ECR-003").title("ECR 3").status("DRAFT").priority("HIGH").build());

        mockMvc.perform(get("/api/ecrs")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void getAffectedParts_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/ecrs/{id}/affected-parts", savedECR.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void addAffectedPart_shouldReturn201() throws Exception {
        Part part = partRepository.save(Part.builder()
                .partNumber("PN-001")
                .name("Affected Part")
                .build());

        mockMvc.perform(post("/api/ecrs/{id}/affected-parts", savedECR.getId())
                        .param("partId", part.getId().toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.partNumber").value("PN-001"));
    }

    @Test
    void removeAffectedPart_shouldReturn204() throws Exception {
        Part part = partRepository.save(Part.builder()
                .partNumber("PN-001")
                .name("Affected Part")
                .build());

        // Add first
        mockMvc.perform(post("/api/ecrs/{id}/affected-parts", savedECR.getId())
                        .param("partId", part.getId().toString()))
                .andExpect(status().isCreated());

        // Remove
        mockMvc.perform(delete("/api/ecrs/{id}/affected-parts/{partId}",
                        savedECR.getId(), part.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAffectedParts_afterAdding_shouldReturnParts() throws Exception {
        Part part = partRepository.save(Part.builder()
                .partNumber("PN-001")
                .name("Affected Part")
                .build());

        mockMvc.perform(post("/api/ecrs/{id}/affected-parts", savedECR.getId())
                        .param("partId", part.getId().toString()))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/ecrs/{id}/affected-parts", savedECR.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].partNumber").value("PN-001"));
    }
}
