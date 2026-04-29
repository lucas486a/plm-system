package com.plm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plm.entity.*;
import com.plm.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ECOControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ECORepository ecoRepository;

    @Autowired
    private ECRRepository ecrRepository;

    @Autowired
    private ECOApprovalRepository ecoApprovalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private PartRevisionRepository partRevisionRepository;

    @Autowired
    private AssemblyRepository assemblyRepository;

    @Autowired
    private BOMRepository bomRepository;

    @Autowired
    private BOMItemRepository bomItemRepository;

    private ECO savedECO;
    private User savedUser;

    @BeforeEach
    void setUp() {
        bomItemRepository.deleteAll();
        bomRepository.deleteAll();
        ecoApprovalRepository.deleteAll();
        ecoRepository.deleteAll();
        ecrRepository.deleteAll();
        partRevisionRepository.deleteAll();
        partRepository.deleteAll();
        assemblyRepository.deleteAll();

        savedECO = ecoRepository.save(ECO.builder()
                .ecoNumber("ECO-001")
                .title("Test ECO")
                .description("A test engineering change order")
                .status("DRAFT")
                .type("ECO")
                .build());

        savedUser = userRepository.save(User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .fullName("Test User")
                .isActive(true)
                .build());
    }

    @Test
    void createECO_shouldReturn201() throws Exception {
        String ecoJson = """
                {
                    "ecoNumber": "ECO-002",
                    "title": "New ECO",
                    "description": "A new ECO",
                    "type": "ECO"
                }
                """;

        mockMvc.perform(post("/api/ecos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ecoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ecoNumber").value("ECO-002"))
                .andExpect(jsonPath("$.title").value("New ECO"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void createECO_duplicateNumber_shouldReturn400() throws Exception {
        String ecoJson = """
                {
                    "ecoNumber": "ECO-001",
                    "title": "Duplicate ECO"
                }
                """;

        mockMvc.perform(post("/api/ecos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ecoJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getECOById_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/ecos/{id}", savedECO.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedECO.getId()))
                .andExpect(jsonPath("$.ecoNumber").value("ECO-001"))
                .andExpect(jsonPath("$.title").value("Test ECO"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void getECOById_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/ecos/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    void getECOByNumber_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/ecos/number/{ecoNumber}", "ECO-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ecoNumber").value("ECO-001"))
                .andExpect(jsonPath("$.title").value("Test ECO"));
    }

    @Test
    void getECOByNumber_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/ecos/number/{ecoNumber}", "NONEXISTENT"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateECO_shouldReturn200() throws Exception {
        String updateJson = """
                {
                    "ecoNumber": "ECO-001",
                    "title": "Updated ECO",
                    "description": "Updated description",
                    "type": "ECO"
                }
                """;

        mockMvc.perform(put("/api/ecos/{id}", savedECO.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated ECO"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void updateECO_notDraft_shouldReturn409() throws Exception {
        // Change status to IN_PROGRESS
        savedECO.setStatus("IN_PROGRESS");
        ecoRepository.save(savedECO);

        String updateJson = """
                {
                    "ecoNumber": "ECO-001",
                    "title": "Updated ECO"
                }
                """;

        mockMvc.perform(put("/api/ecos/{id}", savedECO.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteECO_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/ecos/{id}", savedECO.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteECO_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/ecos/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteECO_notDraft_shouldReturn409() throws Exception {
        savedECO.setStatus("IN_PROGRESS");
        ecoRepository.save(savedECO);

        mockMvc.perform(delete("/api/ecos/{id}", savedECO.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    void listECOs_shouldReturnPaginatedResults() throws Exception {
        ecoRepository.save(ECO.builder()
                .ecoNumber("ECO-002").title("ECO 2").status("DRAFT").type("ECO").build());
        ecoRepository.save(ECO.builder()
                .ecoNumber("ECO-003").title("ECO 3").status("DRAFT").type("ECO").build());

        mockMvc.perform(get("/api/ecos")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void convertECRToECO_shouldReturn201() throws Exception {
        ECR ecr = ecrRepository.save(ECR.builder()
                .ecrNumber("ECR-001")
                .title("Test ECR")
                .description("Test ECR for conversion")
                .status("APPROVED")
                .priority("HIGH")
                .build());

        mockMvc.perform(post("/api/ecrs/{ecrId}/convert-to-eco", ecr.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ecoNumber").value("ECO-ECR-001"))
                .andExpect(jsonPath("$.title").value("Test ECR"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.ecrId").value(ecr.getId()));
    }

    @Test
    void convertECRToECO_ecrNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(post("/api/ecrs/{ecrId}/convert-to-eco", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    void convertECRToECO_alreadyConverted_shouldReturn400() throws Exception {
        ECR ecr = ecrRepository.save(ECR.builder()
                .ecrNumber("ECR-002")
                .title("Test ECR 2")
                .status("APPROVED")
                .priority("MEDIUM")
                .build());

        // Convert once
        mockMvc.perform(post("/api/ecrs/{ecrId}/convert-to-eco", ecr.getId()))
                .andExpect(status().isCreated());

        // Convert again - should fail
        mockMvc.perform(post("/api/ecrs/{ecrId}/convert-to-eco", ecr.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComponentDraft_shouldReturn201() throws Exception {
        // Setup: create assembly, part, part revision, BOM
        Assembly assembly = assemblyRepository.save(Assembly.builder()
                .partNumber("ASM-001").name("Test Assembly").build());

        Part part = partRepository.save(Part.builder()
                .partNumber("PN-001").name("Test Part").build());

        PartRevision partRevision = partRevisionRepository.save(PartRevision.builder()
                .part(part).revision("A").iteration(1).isLatestRevision(true).build());

        BOM bom = bomRepository.save(BOM.builder()
                .name("Test BOM").assembly(assembly).status("DRAFT").build());

        String draftJson = String.format("""
                {
                    "bomId": %d,
                    "partRevisionId": %d,
                    "quantity": 5,
                    "designator": "R1",
                    "action": "ADD"
                }
                """, bom.getId(), partRevision.getId());

        mockMvc.perform(post("/api/ecos/{id}/component-drafts", savedECO.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(draftJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bomId").value(bom.getId()))
                .andExpect(jsonPath("$.partRevisionId").value(partRevision.getId()))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.action").value("ADD"));
    }

    @Test
    void listComponentDrafts_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/ecos/{id}/component-drafts", savedECO.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
