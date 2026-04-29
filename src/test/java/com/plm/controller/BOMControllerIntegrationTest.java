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
class BOMControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BOMRepository bomRepository;

    @Autowired
    private BOMItemRepository bomItemRepository;

    @Autowired
    private AssemblyRepository assemblyRepository;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private PartRevisionRepository partRevisionRepository;

    private Assembly savedAssembly;
    private Part savedPart;
    private PartRevision savedPartRevision;
    private BOM savedBOM;

    @BeforeEach
    void setUp() {
        bomItemRepository.deleteAll();
        bomRepository.deleteAll();
        partRevisionRepository.deleteAll();
        partRepository.deleteAll();
        assemblyRepository.deleteAll();

        savedAssembly = assemblyRepository.save(Assembly.builder()
                .partNumber("ASM-001")
                .name("Test Assembly")
                .description("A test assembly")
                .build());

        savedPart = partRepository.save(Part.builder()
                .partNumber("PN-001")
                .name("Test Part")
                .build());

        savedPartRevision = partRevisionRepository.save(PartRevision.builder()
                .part(savedPart)
                .revision("A")
                .iteration(1)
                .isLatestRevision(true)
                .lifecycleState("DRAFT")
                .build());

        savedBOM = bomRepository.save(BOM.builder()
                .name("Test BOM")
                .assembly(savedAssembly)
                .status("DRAFT")
                .versionNumber(1)
                .build());
    }

    @Test
    void createBOM_shouldReturn201() throws Exception {
        String bomJson = String.format("""
                {
                    "name": "New BOM",
                    "assemblyId": %d,
                    "status": "DRAFT",
                    "versionNumber": 1
                }
                """, savedAssembly.getId());

        mockMvc.perform(post("/api/boms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bomJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New BOM"))
                .andExpect(jsonPath("$.assemblyId").value(savedAssembly.getId()))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void createBOM_assemblyNotFound_shouldReturn404() throws Exception {
        String bomJson = """
                {
                    "name": "Bad BOM",
                    "assemblyId": 99999,
                    "status": "DRAFT"
                }
                """;

        mockMvc.perform(post("/api/boms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bomJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBOM_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/boms/{id}", savedBOM.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedBOM.getId()))
                .andExpect(jsonPath("$.name").value("Test BOM"))
                .andExpect(jsonPath("$.assemblyPartNumber").value("ASM-001"));
    }

    @Test
    void getBOM_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/boms/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBOM_shouldReturn200() throws Exception {
        String updateJson = """
                {
                    "name": "Updated BOM",
                    "comments": "Updated comments"
                }
                """;

        mockMvc.perform(put("/api/boms/{id}", savedBOM.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated BOM"))
                .andExpect(jsonPath("$.comments").value("Updated comments"));
    }

    @Test
    void deleteBOM_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/boms/{id}", savedBOM.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteBOM_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/boms/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    void listBOMs_shouldReturnPaginatedResults() throws Exception {
        Assembly asm2 = assemblyRepository.save(Assembly.builder()
                .partNumber("ASM-002").name("Assembly 2").build());
        bomRepository.save(BOM.builder()
                .name("BOM 2").assembly(asm2).status("DRAFT").build());

        mockMvc.perform(get("/api/boms")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void addBOMItem_shouldReturn201() throws Exception {
        String itemJson = String.format("""
                {
                    "partRevisionId": %d,
                    "quantity": 5,
                    "designator": "R1",
                    "findNumber": 1,
                    "isMounted": true,
                    "scrapFactor": 0
                }
                """, savedPartRevision.getId());

        mockMvc.perform(post("/api/boms/{bomId}/items", savedBOM.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.partRevisionId").value(savedPartRevision.getId()))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.designator").value("R1"));
    }

    @Test
    void addBOMItem_partRevisionNotFound_shouldReturn404() throws Exception {
        String itemJson = """
                {
                    "partRevisionId": 99999,
                    "quantity": 1
                }
                """;

        mockMvc.perform(post("/api/boms/{bomId}/items", savedBOM.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBOMItems_shouldReturnItems() throws Exception {
        // Add an item first
        bomItemRepository.save(BOMItem.builder()
                .bom(savedBOM)
                .partRevision(savedPartRevision)
                .quantity(BigDecimal.TEN)
                .designator("C1")
                .isMounted(true)
                .build());

        mockMvc.perform(get("/api/boms/{bomId}/items", savedBOM.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].designator").value("C1"));
    }

    @Test
    void updateBOMItem_shouldReturn200() throws Exception {
        BOMItem item = bomItemRepository.save(BOMItem.builder()
                .bom(savedBOM)
                .partRevision(savedPartRevision)
                .quantity(BigDecimal.ONE)
                .designator("R1")
                .isMounted(true)
                .build());

        String updateJson = """
                {
                    "quantity": 10,
                    "designator": "R2"
                }
                """;

        mockMvc.perform(put("/api/boms/{bomId}/items/{itemId}", savedBOM.getId(), item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.designator").value("R2"));
    }

    @Test
    void removeBOMItem_shouldReturn204() throws Exception {
        BOMItem item = bomItemRepository.save(BOMItem.builder()
                .bom(savedBOM)
                .partRevision(savedPartRevision)
                .quantity(BigDecimal.ONE)
                .isMounted(true)
                .build());

        mockMvc.perform(delete("/api/boms/{bomId}/items/{itemId}", savedBOM.getId(), item.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeBOMItem_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/boms/{bomId}/items/{itemId}", savedBOM.getId(), 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    void explodeBOM_shouldReturnTree() throws Exception {
        bomItemRepository.save(BOMItem.builder()
                .bom(savedBOM)
                .partRevision(savedPartRevision)
                .quantity(BigDecimal.ONE)
                .isMounted(true)
                .build());

        mockMvc.perform(get("/api/boms/{id}/explode", savedBOM.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].partNumber").value("PN-001"))
                .andExpect(jsonPath("$[0].level").value(0));
    }

    @Test
    void copyBOM_shouldReturn201() throws Exception {
        bomItemRepository.save(BOMItem.builder()
                .bom(savedBOM)
                .partRevision(savedPartRevision)
                .quantity(BigDecimal.ONE)
                .isMounted(true)
                .build());

        mockMvc.perform(post("/api/boms/{id}/copy", savedBOM.getId())
                        .param("newName", "Copied BOM"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Copied BOM"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.id").value(not(savedBOM.getId())));
    }

    @Test
    void calculateCost_shouldReturnBigDecimal() throws Exception {
        // Set a price on the part revision
        savedPartRevision.setPrice(new BigDecimal("10.50"));
        partRevisionRepository.save(savedPartRevision);

        bomItemRepository.save(BOMItem.builder()
                .bom(savedBOM)
                .partRevision(savedPartRevision)
                .quantity(new BigDecimal("3"))
                .scrapFactor(new BigDecimal("5"))
                .isMounted(true)
                .build());

        mockMvc.perform(get("/api/boms/{id}/cost", savedBOM.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }
}
