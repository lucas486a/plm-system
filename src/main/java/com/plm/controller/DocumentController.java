package com.plm.controller;

import com.plm.dto.DocumentDTO;
import com.plm.dto.DocumentRevisionDTO;
import com.plm.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Documents", description = "Manage documents, revisions, and file attachments")
public class DocumentController {

    private final DocumentService documentService;

    // ==================== Document CRUD ====================

    @PostMapping
    @Operation(summary = "Create a new document", description = "Creates a new document with the provided details.")
    @ApiResponse(responseCode = "201", description = "Document created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<DocumentDTO> createDocument(@RequestBody DocumentDTO documentDTO) {
        log.info("REST request to create document: {}", documentDTO.getDocumentNumber());
        DocumentDTO created = documentService.createDocument(documentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID", description = "Retrieves a document by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "Document found")
    @ApiResponse(responseCode = "404", description = "Document not found")
    public ResponseEntity<DocumentDTO> getDocumentById(
            @Parameter(description = "Document ID") @PathVariable Long id) {
        log.info("REST request to get document by id: {}", id);
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @GetMapping("/number/{documentNumber}")
    @Operation(summary = "Get document by number", description = "Retrieves a document by its document number.")
    @ApiResponse(responseCode = "200", description = "Document found")
    @ApiResponse(responseCode = "404", description = "Document not found")
    public ResponseEntity<DocumentDTO> getDocumentByNumber(
            @Parameter(description = "Document number") @PathVariable String documentNumber) {
        log.info("REST request to get document by number: {}", documentNumber);
        return ResponseEntity.ok(documentService.getDocumentByNumber(documentNumber));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a document", description = "Updates an existing document with the provided details.")
    @ApiResponse(responseCode = "200", description = "Document updated successfully")
    @ApiResponse(responseCode = "404", description = "Document not found")
    public ResponseEntity<DocumentDTO> updateDocument(
            @Parameter(description = "Document ID") @PathVariable Long id,
            @RequestBody DocumentDTO documentDTO) {
        log.info("REST request to update document id: {}", id);
        return ResponseEntity.ok(documentService.updateDocument(id, documentDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a document", description = "Deletes a document by its unique identifier.")
    @ApiResponse(responseCode = "204", description = "Document deleted successfully")
    @ApiResponse(responseCode = "404", description = "Document not found")
    public ResponseEntity<Void> deleteDocument(
            @Parameter(description = "Document ID") @PathVariable Long id) {
        log.info("REST request to delete document id: {}", id);
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List all documents", description = "Returns a paginated list of all documents.")
    @ApiResponse(responseCode = "200", description = "Paginated list of documents")
    public ResponseEntity<Page<DocumentDTO>> listDocuments(Pageable pageable) {
        log.info("REST request to list documents");
        return ResponseEntity.ok(documentService.listDocuments(pageable));
    }

    // ==================== Revision Management ====================

    @PostMapping("/{id}/revisions")
    @Operation(summary = "Create a document revision", description = "Creates a new revision for the specified document.")
    @ApiResponse(responseCode = "201", description = "Revision created successfully")
    @ApiResponse(responseCode = "404", description = "Document not found")
    public ResponseEntity<DocumentRevisionDTO> createRevision(
            @Parameter(description = "Document ID") @PathVariable Long id,
            @RequestBody DocumentRevisionDTO revisionDTO) {
        log.info("REST request to create revision for document id: {}", id);
        DocumentRevisionDTO created = documentService.createRevision(id, revisionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}/revisions")
    @Operation(summary = "List document revisions", description = "Returns all revisions for the specified document.")
    @ApiResponse(responseCode = "200", description = "List of revisions")
    @ApiResponse(responseCode = "404", description = "Document not found")
    public ResponseEntity<List<DocumentRevisionDTO>> listRevisions(
            @Parameter(description = "Document ID") @PathVariable Long id) {
        log.info("REST request to list revisions for document id: {}", id);
        return ResponseEntity.ok(documentService.listRevisions(id));
    }

    // ==================== File Operations ====================

    @PostMapping("/{id}/revisions/{revisionId}/files")
    @Operation(summary = "Upload a file", description = "Uploads a file attachment to a specific document revision.")
    @ApiResponse(responseCode = "200", description = "File uploaded successfully")
    @ApiResponse(responseCode = "404", description = "Document or revision not found")
    public ResponseEntity<DocumentRevisionDTO> uploadFile(
            @Parameter(description = "Document ID") @PathVariable Long id,
            @Parameter(description = "Revision ID") @PathVariable Long revisionId,
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file) throws IOException {
        log.info("REST request to upload file for document id: {}, revision id: {}", id, revisionId);
        return ResponseEntity.ok(documentService.uploadFile(id, revisionId, file));
    }

    @GetMapping("/{id}/revisions/{revisionId}/files/{fileName}")
    @Operation(summary = "Download a file", description = "Downloads a file attachment from a specific document revision.")
    @ApiResponse(responseCode = "200", description = "File content returned")
    @ApiResponse(responseCode = "404", description = "File not found")
    public ResponseEntity<byte[]> downloadFile(
            @Parameter(description = "Document ID") @PathVariable Long id,
            @Parameter(description = "Revision ID") @PathVariable Long revisionId,
            @Parameter(description = "File name") @PathVariable String fileName) throws IOException {
        log.info("REST request to download file for document id: {}, revision id: {}", id, revisionId);

        byte[] fileContent = documentService.downloadFile(id, revisionId);

        // Get revision to determine content type
        DocumentRevisionDTO revision = documentService.listRevisions(id).stream()
                .filter(r -> r.getId().equals(revisionId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Revision not found"));

        String contentType = revision.getContentType();
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(fileContent);
    }

    // ==================== Exception Handlers ====================

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleConflict(IllegalStateException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIOException(IOException ex) {
        log.error("IO error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("File operation failed: " + ex.getMessage());
    }
}
