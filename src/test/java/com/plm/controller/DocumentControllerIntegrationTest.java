package com.plm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plm.dto.DocumentDTO;
import com.plm.entity.Document;
import com.plm.repository.DocumentRepository;
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
class DocumentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DocumentRepository documentRepository;

    private Document savedDocument;

    @BeforeEach
    void setUp() {
        documentRepository.deleteAll();
        savedDocument = documentRepository.save(Document.builder()
                .documentNumber("DOC-001")
                .title("Test Document")
                .documentType("SPECIFICATION")
                .description("A test document")
                .build());
    }

    @Test
    void createDocument_shouldReturn201() throws Exception {
        DocumentDTO newDoc = DocumentDTO.builder()
                .documentNumber("DOC-002")
                .title("New Document")
                .documentType("DRAWING")
                .description("A new document")
                .build();

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDoc)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentNumber").value("DOC-002"))
                .andExpect(jsonPath("$.title").value("New Document"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void createDocument_duplicateNumber_shouldReturn400() throws Exception {
        DocumentDTO duplicate = DocumentDTO.builder()
                .documentNumber("DOC-001")
                .title("Duplicate Document")
                .build();

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDocumentById_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/documents/{id}", savedDocument.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedDocument.getId()))
                .andExpect(jsonPath("$.documentNumber").value("DOC-001"))
                .andExpect(jsonPath("$.title").value("Test Document"));
    }

    @Test
    void getDocumentById_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/documents/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDocumentByNumber_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/documents/number/{documentNumber}", "DOC-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentNumber").value("DOC-001"))
                .andExpect(jsonPath("$.title").value("Test Document"));
    }

    @Test
    void getDocumentByNumber_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/documents/number/{documentNumber}", "NONEXISTENT"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateDocument_shouldReturn200() throws Exception {
        DocumentDTO update = DocumentDTO.builder()
                .documentNumber("DOC-001")
                .title("Updated Document")
                .documentType("SPECIFICATION")
                .description("Updated description")
                .build();

        mockMvc.perform(put("/api/documents/{id}", savedDocument.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Document"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void updateDocument_notFound_shouldReturn404() throws Exception {
        DocumentDTO update = DocumentDTO.builder()
                .documentNumber("DOC-001")
                .title("Updated Document")
                .build();

        mockMvc.perform(put("/api/documents/{id}", 99999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDocument_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/documents/{id}", savedDocument.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteDocument_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/documents/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    void listDocuments_shouldReturnPaginatedResults() throws Exception {
        documentRepository.save(Document.builder()
                .documentNumber("DOC-002").title("Document 2").build());
        documentRepository.save(Document.builder()
                .documentNumber("DOC-003").title("Document 3").build());

        mockMvc.perform(get("/api/documents")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void createRevision_shouldReturn201() throws Exception {
        String revisionJson = """
                {
                    "revision": "A",
                    "iteration": 1,
                    "lifecycleState": "DRAFT",
                    "description": "Initial revision",
                    "revisionLocked": false
                }
                """;

        mockMvc.perform(post("/api/documents/{id}/revisions", savedDocument.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(revisionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.revision").value("A"))
                .andExpect(jsonPath("$.documentId").value(savedDocument.getId()))
                .andExpect(jsonPath("$.isLatestRevision").value(true));
    }

    @Test
    void listRevisions_shouldReturnRevisions() throws Exception {
        // Create a revision first
        String revisionJson = """
                {
                    "revision": "A",
                    "iteration": 1,
                    "lifecycleState": "DRAFT",
                    "revisionLocked": false
                }
                """;

        mockMvc.perform(post("/api/documents/{id}/revisions", savedDocument.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(revisionJson))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/documents/{id}/revisions", savedDocument.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
