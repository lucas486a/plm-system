package com.plm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plm.dto.PartDTO;
import com.plm.entity.Part;
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
class PartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PartRepository partRepository;

    private Part savedPart;

    @BeforeEach
    void setUp() {
        partRepository.deleteAll();
        savedPart = partRepository.save(Part.builder()
                .partNumber("PN-001")
                .name("Test Part")
                .description("A test part")
                .partType("COMPONENT")
                .defaultUnit("EA")
                .build());
    }

    @Test
    void createPart_shouldReturn201() throws Exception {
        PartDTO newPart = PartDTO.builder()
                .partNumber("PN-002")
                .name("New Part")
                .description("A new part")
                .partType("ASSEMBLY")
                .defaultUnit("EA")
                .build();

        mockMvc.perform(post("/api/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPart)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.partNumber").value("PN-002"))
                .andExpect(jsonPath("$.name").value("New Part"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void createPart_duplicateNumber_shouldReturn400() throws Exception {
        PartDTO duplicate = PartDTO.builder()
                .partNumber("PN-001")
                .name("Duplicate Part")
                .build();

        mockMvc.perform(post("/api/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPartById_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/parts/{id}", savedPart.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPart.getId()))
                .andExpect(jsonPath("$.partNumber").value("PN-001"))
                .andExpect(jsonPath("$.name").value("Test Part"));
    }

    @Test
    void getPartById_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/parts/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPartByNumber_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/parts/number/{partNumber}", "PN-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partNumber").value("PN-001"))
                .andExpect(jsonPath("$.name").value("Test Part"));
    }

    @Test
    void getPartByNumber_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/parts/number/{partNumber}", "NONEXISTENT"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePart_shouldReturn200() throws Exception {
        PartDTO update = PartDTO.builder()
                .partNumber("PN-001")
                .name("Updated Part")
                .description("Updated description")
                .partType("COMPONENT")
                .defaultUnit("EA")
                .build();

        mockMvc.perform(put("/api/parts/{id}", savedPart.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Part"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void updatePart_notFound_shouldReturn404() throws Exception {
        PartDTO update = PartDTO.builder()
                .partNumber("PN-001")
                .name("Updated Part")
                .build();

        mockMvc.perform(put("/api/parts/{id}", 99999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePart_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/parts/{id}", savedPart.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletePart_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/parts/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    void listParts_shouldReturnPaginatedResults() throws Exception {
        // Create additional parts
        partRepository.save(Part.builder().partNumber("PN-002").name("Part 2").build());
        partRepository.save(Part.builder().partNumber("PN-003").name("Part 3").build());

        mockMvc.perform(get("/api/parts")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void searchParts_shouldReturnMatchingResults() throws Exception {
        mockMvc.perform(get("/api/parts/search")
                        .param("query", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].name").value("Test Part"));
    }

    @Test
    void createPartRevision_shouldReturn201() throws Exception {
        String revisionJson = """
                {
                    "revision": "A",
                    "iteration": 1,
                    "lifecycleState": "DRAFT",
                    "description": "Initial revision"
                }
                """;

        mockMvc.perform(post("/api/parts/{id}/revisions", savedPart.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(revisionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.revision").value("A"))
                .andExpect(jsonPath("$.partId").value(savedPart.getId()))
                .andExpect(jsonPath("$.isLatestRevision").value(true));
    }

    @Test
    void listPartRevisions_shouldReturnPaginatedResults() throws Exception {
        // Create a revision first
        String revisionJson = """
                {
                    "revision": "A",
                    "iteration": 1,
                    "lifecycleState": "DRAFT"
                }
                """;

        mockMvc.perform(post("/api/parts/{id}/revisions", savedPart.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(revisionJson))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/parts/{id}/revisions", savedPart.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }
}
