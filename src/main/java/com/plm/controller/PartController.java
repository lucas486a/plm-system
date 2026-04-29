package com.plm.controller;

import com.plm.dto.PartDTO;
import com.plm.dto.PartRevisionDTO;
import com.plm.service.PartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Parts", description = "Manage parts and part revisions")
public class PartController {

    private final PartService partService;

    @PostMapping
    @Operation(summary = "Create a new part", description = "Creates a new part with the provided details.")
    @ApiResponse(responseCode = "201", description = "Part created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<PartDTO> createPart(@RequestBody PartDTO partDTO) {
        log.info("REST request to create part: {}", partDTO.getPartNumber());
        PartDTO createdPart = partService.createPart(partDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPart);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get part by ID", description = "Retrieves a part by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "Part found")
    @ApiResponse(responseCode = "404", description = "Part not found")
    public ResponseEntity<PartDTO> getPartById(
            @Parameter(description = "Part ID") @PathVariable Long id) {
        log.debug("REST request to get part by id: {}", id);
        PartDTO part = partService.getPartById(id);
        return ResponseEntity.ok(part);
    }

    @GetMapping("/number/{partNumber}")
    @Operation(summary = "Get part by number", description = "Retrieves a part by its part number.")
    @ApiResponse(responseCode = "200", description = "Part found")
    @ApiResponse(responseCode = "404", description = "Part not found")
    public ResponseEntity<PartDTO> getPartByNumber(
            @Parameter(description = "Part number") @PathVariable String partNumber) {
        log.debug("REST request to get part by number: {}", partNumber);
        PartDTO part = partService.getPartByNumber(partNumber);
        return ResponseEntity.ok(part);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a part", description = "Updates an existing part with the provided details.")
    @ApiResponse(responseCode = "200", description = "Part updated successfully")
    @ApiResponse(responseCode = "404", description = "Part not found")
    @ApiResponse(responseCode = "409", description = "Optimistic lock conflict")
    public ResponseEntity<PartDTO> updatePart(
            @Parameter(description = "Part ID") @PathVariable Long id,
            @RequestBody PartDTO partDTO) {
        log.info("REST request to update part id: {}", id);
        PartDTO updatedPart = partService.updatePart(id, partDTO);
        return ResponseEntity.ok(updatedPart);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a part", description = "Deletes a part by its unique identifier.")
    @ApiResponse(responseCode = "204", description = "Part deleted successfully")
    @ApiResponse(responseCode = "404", description = "Part not found")
    public ResponseEntity<Void> deletePart(
            @Parameter(description = "Part ID") @PathVariable Long id) {
        log.info("REST request to delete part id: {}", id);
        partService.deletePart(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List all parts", description = "Returns a paginated list of all parts.")
    @ApiResponse(responseCode = "200", description = "Paginated list of parts")
    public ResponseEntity<Page<PartDTO>> listParts(
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("REST request to list parts with pagination: {}", pageable);
        Page<PartDTO> parts = partService.listParts(pageable);
        return ResponseEntity.ok(parts);
    }

    @GetMapping("/search")
    @Operation(summary = "Search parts", description = "Searches parts by a query string with pagination.")
    @ApiResponse(responseCode = "200", description = "Search results")
    public ResponseEntity<Page<PartDTO>> searchParts(
            @Parameter(description = "Search query") @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("REST request to search parts with query: {}, pagination: {}", query, pageable);
        Page<PartDTO> parts = partService.searchParts(query, pageable);
        return ResponseEntity.ok(parts);
    }

    @PostMapping("/{id}/revisions")
    @Operation(summary = "Create a part revision", description = "Creates a new revision for the specified part.")
    @ApiResponse(responseCode = "201", description = "Revision created successfully")
    @ApiResponse(responseCode = "404", description = "Part not found")
    public ResponseEntity<PartRevisionDTO> createPartRevision(
            @Parameter(description = "Part ID") @PathVariable Long id,
            @RequestBody PartRevisionDTO revisionDTO) {
        log.info("REST request to create revision for part id: {}", id);
        PartRevisionDTO createdRevision = partService.createPartRevision(id, revisionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRevision);
    }

    @GetMapping("/{id}/revisions")
    @Operation(summary = "List part revisions", description = "Returns a paginated list of revisions for the specified part.")
    @ApiResponse(responseCode = "200", description = "Paginated list of revisions")
    @ApiResponse(responseCode = "404", description = "Part not found")
    public ResponseEntity<Page<PartRevisionDTO>> listPartRevisions(
            @Parameter(description = "Part ID") @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("REST request to list revisions for part id: {}, pagination: {}", id, pageable);
        Page<PartRevisionDTO> revisions = partService.listPartRevisions(id, pageable);
        return ResponseEntity.ok(revisions);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<String> handleOptimisticLock(OptimisticLockException ex) {
        log.warn("Optimistic lock conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
