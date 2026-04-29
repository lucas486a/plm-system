package com.plm.service;

import com.plm.dto.DocumentDTO;
import com.plm.dto.DocumentRevisionDTO;
import com.plm.entity.Document;
import com.plm.entity.DocumentRevision;
import com.plm.mapper.DocumentMapper;
import com.plm.mapper.DocumentRevisionMapper;
import com.plm.repository.DocumentRepository;
import com.plm.repository.DocumentRevisionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private DocumentRevisionRepository documentRevisionRepository;
    @Mock
    private DocumentMapper documentMapper;
    @Mock
    private DocumentRevisionMapper documentRevisionMapper;

    @InjectMocks
    private DocumentServiceImpl documentService;

    private Document document;
    private DocumentDTO documentDTO;
    private DocumentRevision documentRevision;
    private DocumentRevisionDTO documentRevisionDTO;

    @BeforeEach
    void setUp() {
        document = Document.builder()
                .id(1L)
                .documentNumber("DOC-001")
                .title("Test Document")
                .documentType("SPECIFICATION")
                .description("A test document")
                .build();

        documentDTO = DocumentDTO.builder()
                .id(1L)
                .documentNumber("DOC-001")
                .title("Test Document")
                .documentType("SPECIFICATION")
                .description("A test document")
                .build();

        documentRevision = DocumentRevision.builder()
                .id(1L)
                .document(document)
                .revision("A")
                .iteration(1)
                .isLatestRevision(true)
                .revisionLocked(false)
                .build();

        documentRevisionDTO = DocumentRevisionDTO.builder()
                .id(1L)
                .documentId(1L)
                .revision("A")
                .iteration(1)
                .isLatestRevision(true)
                .revisionLocked(false)
                .build();
    }

    // ==================== createDocument ====================

    @Nested
    @DisplayName("createDocument")
    class CreateDocument {

        @Test
        @DisplayName("should create document successfully")
        void createDocument_success() {
            when(documentRepository.existsByDocumentNumber("DOC-001")).thenReturn(false);
            when(documentMapper.toEntity(any(DocumentDTO.class))).thenReturn(document);
            when(documentRepository.save(any(Document.class))).thenReturn(document);
            when(documentMapper.toDto(any(Document.class))).thenReturn(documentDTO);

            DocumentDTO result = documentService.createDocument(documentDTO);

            assertThat(result).isNotNull();
            assertThat(result.getDocumentNumber()).isEqualTo("DOC-001");
            verify(documentRepository).save(document);
        }

        @Test
        @DisplayName("should throw when document number already exists")
        void createDocument_duplicateNumber() {
            when(documentRepository.existsByDocumentNumber("DOC-001")).thenReturn(true);

            assertThatThrownBy(() -> documentService.createDocument(documentDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");

            verify(documentRepository, never()).save(any());
        }
    }

    // ==================== getDocumentById ====================

    @Nested
    @DisplayName("getDocumentById")
    class GetDocumentById {

        @Test
        @DisplayName("should return document when found")
        void getDocumentById_found() {
            when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
            when(documentMapper.toDto(document)).thenReturn(documentDTO);

            DocumentDTO result = documentService.getDocumentById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should throw when document not found")
        void getDocumentById_notFound() {
            when(documentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.getDocumentById(99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Document not found");
        }
    }

    // ==================== getDocumentByNumber ====================

    @Nested
    @DisplayName("getDocumentByNumber")
    class GetDocumentByNumber {

        @Test
        @DisplayName("should return document when found by number")
        void getDocumentByNumber_found() {
            when(documentRepository.findByDocumentNumber("DOC-001")).thenReturn(Optional.of(document));
            when(documentMapper.toDto(document)).thenReturn(documentDTO);

            DocumentDTO result = documentService.getDocumentByNumber("DOC-001");

            assertThat(result).isNotNull();
            assertThat(result.getDocumentNumber()).isEqualTo("DOC-001");
        }

        @Test
        @DisplayName("should throw when document number not found")
        void getDocumentByNumber_notFound() {
            when(documentRepository.findByDocumentNumber("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.getDocumentByNumber("UNKNOWN"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // ==================== updateDocument ====================

    @Nested
    @DisplayName("updateDocument")
    class UpdateDocument {

        @Test
        @DisplayName("should update document successfully")
        void updateDocument_success() {
            DocumentDTO updateDTO = DocumentDTO.builder()
                    .documentNumber("DOC-001")
                    .title("Updated Title")
                    .documentType("DRAWING")
                    .description("Updated description")
                    .build();

            when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
            when(documentRepository.save(any(Document.class))).thenReturn(document);
            when(documentMapper.toDto(any(Document.class))).thenReturn(updateDTO);

            DocumentDTO result = documentService.updateDocument(1L, updateDTO);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Updated Title");
            verify(documentRepository).save(document);
        }

        @Test
        @DisplayName("should throw when document not found")
        void updateDocument_notFound() {
            when(documentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.updateDocument(99L, documentDTO))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when changing to duplicate document number")
        void updateDocument_duplicateNumber() {
            DocumentDTO updateDTO = DocumentDTO.builder()
                    .documentNumber("DOC-002")
                    .title("Updated")
                    .build();

            when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
            when(documentRepository.existsByDocumentNumber("DOC-002")).thenReturn(true);

            assertThatThrownBy(() -> documentService.updateDocument(1L, updateDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("should allow update with same document number")
        void updateDocument_sameNumber() {
            when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
            when(documentRepository.save(any(Document.class))).thenReturn(document);
            when(documentMapper.toDto(any(Document.class))).thenReturn(documentDTO);

            documentService.updateDocument(1L, documentDTO);

            verify(documentRepository, never()).existsByDocumentNumber(anyString());
        }
    }

    // ==================== deleteDocument ====================

    @Nested
    @DisplayName("deleteDocument")
    class DeleteDocument {

        @Test
        @DisplayName("should delete document successfully")
        void deleteDocument_success() {
            when(documentRepository.existsById(1L)).thenReturn(true);
            doNothing().when(documentRepository).deleteById(1L);

            documentService.deleteDocument(1L);

            verify(documentRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw when document not found")
        void deleteDocument_notFound() {
            when(documentRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> documentService.deleteDocument(99L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // ==================== listDocuments ====================

    @Nested
    @DisplayName("listDocuments")
    class ListDocuments {

        @Test
        @DisplayName("should return paginated documents")
        void listDocuments_success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Document> page = new PageImpl<>(List.of(document), pageable, 1);
            when(documentRepository.findAll(pageable)).thenReturn(page);
            when(documentMapper.toDto(document)).thenReturn(documentDTO);

            Page<DocumentDTO> result = documentService.listDocuments(pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should return empty page when no documents")
        void listDocuments_empty() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Document> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
            when(documentRepository.findAll(pageable)).thenReturn(page);

            Page<DocumentDTO> result = documentService.listDocuments(pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    // ==================== createRevision ====================

    @Nested
    @DisplayName("createRevision")
    class CreateRevision {

        @Test
        @DisplayName("should create revision successfully")
        void createRevision_success() {
            when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
            when(documentRevisionRepository.findByDocumentIdAndIsLatestRevisionTrue(1L))
                    .thenReturn(Optional.empty());
            when(documentRevisionRepository.findByDocumentIdOrderByRevisionDesc(1L))
                    .thenReturn(Collections.emptyList());
            when(documentRevisionMapper.toEntity(any(DocumentRevisionDTO.class))).thenReturn(documentRevision);
            when(documentRevisionRepository.save(any(DocumentRevision.class))).thenReturn(documentRevision);
            when(documentRevisionMapper.toDto(any(DocumentRevision.class))).thenReturn(documentRevisionDTO);

            DocumentRevisionDTO result = documentService.createRevision(1L, documentRevisionDTO);

            assertThat(result).isNotNull();
            assertThat(result.getRevision()).isEqualTo("A");
            verify(documentRevisionRepository).save(any(DocumentRevision.class));
        }

        @Test
        @DisplayName("should throw when document not found")
        void createRevision_documentNotFound() {
            when(documentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.createRevision(99L, documentRevisionDTO))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should unmark previous latest revision")
        void createRevision_unmarksPreviousLatest() {
            DocumentRevision previousLatest = DocumentRevision.builder()
                    .id(2L).document(document).revision("A").iteration(1).isLatestRevision(true).build();

            when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
            when(documentRevisionRepository.findByDocumentIdAndIsLatestRevisionTrue(1L))
                    .thenReturn(Optional.of(previousLatest));
            when(documentRevisionRepository.findByDocumentIdOrderByRevisionDesc(1L))
                    .thenReturn(List.of(previousLatest));
            when(documentRevisionMapper.toEntity(any(DocumentRevisionDTO.class))).thenReturn(documentRevision);
            when(documentRevisionRepository.save(any(DocumentRevision.class))).thenReturn(documentRevision);
            when(documentRevisionMapper.toDto(any(DocumentRevision.class))).thenReturn(documentRevisionDTO);

            documentService.createRevision(1L, documentRevisionDTO);

            assertThat(previousLatest.getIsLatestRevision()).isFalse();
        }

        @Test
        @DisplayName("should auto-increment iteration for same revision")
        void createRevision_autoIncrementIteration() {
            DocumentRevision existingRev = DocumentRevision.builder()
                    .id(2L).document(document).revision("A").iteration(1).isLatestRevision(false).build();

            when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
            when(documentRevisionRepository.findByDocumentIdAndIsLatestRevisionTrue(1L))
                    .thenReturn(Optional.empty());
            when(documentRevisionRepository.findByDocumentIdOrderByRevisionDesc(1L))
                    .thenReturn(List.of(existingRev));
            when(documentRevisionMapper.toEntity(any(DocumentRevisionDTO.class))).thenReturn(documentRevision);
            when(documentRevisionRepository.save(any(DocumentRevision.class))).thenReturn(documentRevision);
            when(documentRevisionMapper.toDto(any(DocumentRevision.class))).thenReturn(documentRevisionDTO);

            documentService.createRevision(1L, documentRevisionDTO);

            assertThat(documentRevision.getIteration()).isEqualTo(2);
        }
    }

    // ==================== listRevisions ====================

    @Nested
    @DisplayName("listRevisions")
    class ListRevisions {

        @Test
        @DisplayName("should return revisions for document")
        void listRevisions_success() {
            when(documentRepository.existsById(1L)).thenReturn(true);
            when(documentRevisionRepository.findByDocumentIdOrderByRevisionDesc(1L))
                    .thenReturn(List.of(documentRevision));
            when(documentRevisionMapper.toDtoList(anyList())).thenReturn(List.of(documentRevisionDTO));

            List<DocumentRevisionDTO> result = documentService.listRevisions(1L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should throw when document not found")
        void listRevisions_documentNotFound() {
            when(documentRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> documentService.listRevisions(99L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // ==================== uploadFile ====================

    @Nested
    @DisplayName("uploadFile")
    class UploadFile {

        @Test
        @DisplayName("should throw when revision not found")
        void uploadFile_revisionNotFound() {
            when(documentRevisionRepository.findById(99L)).thenReturn(Optional.empty());

            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.pdf", "application/pdf", "content".getBytes());

            assertThatThrownBy(() -> documentService.uploadFile(1L, 99L, file))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when revision does not belong to document")
        void uploadFile_revisionMismatch() {
            DocumentRevision otherRevision = DocumentRevision.builder()
                    .id(1L)
                    .document(Document.builder().id(999L).build())
                    .revisionLocked(false)
                    .build();

            when(documentRevisionRepository.findById(1L)).thenReturn(Optional.of(otherRevision));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.pdf", "application/pdf", "content".getBytes());

            assertThatThrownBy(() -> documentService.uploadFile(1L, 1L, file))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not belong");
        }

        @Test
        @DisplayName("should throw when revision is locked")
        void uploadFile_lockedRevision() {
            DocumentRevision lockedRevision = DocumentRevision.builder()
                    .id(1L)
                    .document(document)
                    .revisionLocked(true)
                    .build();

            when(documentRevisionRepository.findById(1L)).thenReturn(Optional.of(lockedRevision));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.pdf", "application/pdf", "content".getBytes());

            assertThatThrownBy(() -> documentService.uploadFile(1L, 1L, file))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("locked");
        }
    }

    // ==================== downloadFile ====================

    @Nested
    @DisplayName("downloadFile")
    class DownloadFile {

        @Test
        @DisplayName("should throw when revision not found")
        void downloadFile_revisionNotFound() {
            when(documentRevisionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.downloadFile(1L, 99L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when revision does not belong to document")
        void downloadFile_revisionMismatch() {
            DocumentRevision otherRevision = DocumentRevision.builder()
                    .id(1L)
                    .document(Document.builder().id(999L).build())
                    .build();

            when(documentRevisionRepository.findById(1L)).thenReturn(Optional.of(otherRevision));

            assertThatThrownBy(() -> documentService.downloadFile(1L, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not belong");
        }

        @Test
        @DisplayName("should throw when no file attached")
        void downloadFile_noFile() {
            DocumentRevision noFileRevision = DocumentRevision.builder()
                    .id(1L)
                    .document(document)
                    .filePath(null)
                    .build();

            when(documentRevisionRepository.findById(1L)).thenReturn(Optional.of(noFileRevision));

            assertThatThrownBy(() -> documentService.downloadFile(1L, 1L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("No file attached");
        }
    }
}
