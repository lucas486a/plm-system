package com.plm.controller;

import com.plm.dto.ComponentDraftDTO;
import com.plm.dto.ECOApprovalDTO;
import com.plm.dto.ECODTO;
import com.plm.service.ECOService;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Engineering Change Orders", description = "Manage ECOs, workflow transitions, approvals, and component drafts")
public class ECOController {

    private final ECOService ecoService;

    // =========================================================================
    // ECR-to-ECO Conversion (must be before /api/ecos/{id} to avoid conflict)
    // =========================================================================

    @PostMapping("/ecrs/{ecrId}/convert-to-eco")
    @Operation(summary = "Convert ECR to ECO", description = "Creates a new ECO from an approved ECR.")
    @ApiResponse(responseCode = "201", description = "ECO created from ECR")
    @ApiResponse(responseCode = "404", description = "ECR not found")
    @ApiResponse(responseCode = "409", description = "ECR not in approved status")
    public ResponseEntity<ECODTO> convertECRToECO(
            @Parameter(description = "ECR ID") @PathVariable Long ecrId) {
        log.info("REST request to convert ECR id: {} to ECO", ecrId);
        ECODTO eco = ecoService.convertECRToECO(ecrId);
        return ResponseEntity.status(HttpStatus.CREATED).body(eco);
    }

    // =========================================================================
    // ECO CRUD
    // =========================================================================

    @PostMapping("/ecos")
    @Operation(summary = "Create a new ECO", description = "Creates a new Engineering Change Order.")
    @ApiResponse(responseCode = "201", description = "ECO created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<ECODTO> createECO(@RequestBody ECODTO ecoDTO) {
        log.info("REST request to create ECO: {}", ecoDTO.getEcoNumber());
        ECODTO createdEco = ecoService.createECO(ecoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEco);
    }

    @GetMapping("/ecos/{id}")
    @Operation(summary = "Get ECO by ID", description = "Retrieves an ECO by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "ECO found")
    @ApiResponse(responseCode = "404", description = "ECO not found")
    public ResponseEntity<ECODTO> getECOById(
            @Parameter(description = "ECO ID") @PathVariable Long id) {
        log.debug("REST request to get ECO by id: {}", id);
        ECODTO eco = ecoService.getECOById(id);
        return ResponseEntity.ok(eco);
    }

    @GetMapping("/ecos/number/{ecoNumber}")
    @Operation(summary = "Get ECO by number", description = "Retrieves an ECO by its ECO number.")
    @ApiResponse(responseCode = "200", description = "ECO found")
    @ApiResponse(responseCode = "404", description = "ECO not found")
    public ResponseEntity<ECODTO> getECOByNumber(
            @Parameter(description = "ECO number") @PathVariable String ecoNumber) {
        log.debug("REST request to get ECO by number: {}", ecoNumber);
        ECODTO eco = ecoService.getECOByNumber(ecoNumber);
        return ResponseEntity.ok(eco);
    }

    @PutMapping("/ecos/{id}")
    @Operation(summary = "Update an ECO", description = "Updates an existing ECO with the provided details.")
    @ApiResponse(responseCode = "200", description = "ECO updated successfully")
    @ApiResponse(responseCode = "404", description = "ECO not found")
    @ApiResponse(responseCode = "409", description = "Optimistic lock conflict")
    public ResponseEntity<ECODTO> updateECO(
            @Parameter(description = "ECO ID") @PathVariable Long id,
            @RequestBody ECODTO ecoDTO) {
        log.info("REST request to update ECO id: {}", id);
        ECODTO updatedEco = ecoService.updateECO(id, ecoDTO);
        return ResponseEntity.ok(updatedEco);
    }

    @DeleteMapping("/ecos/{id}")
    @Operation(summary = "Delete an ECO", description = "Deletes an ECO by its unique identifier.")
    @ApiResponse(responseCode = "204", description = "ECO deleted successfully")
    @ApiResponse(responseCode = "404", description = "ECO not found")
    public ResponseEntity<Void> deleteECO(
            @Parameter(description = "ECO ID") @PathVariable Long id) {
        log.info("REST request to delete ECO id: {}", id);
        ecoService.deleteECO(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ecos")
    @Operation(summary = "List all ECOs", description = "Returns a paginated list of all ECOs.")
    @ApiResponse(responseCode = "200", description = "Paginated list of ECOs")
    public ResponseEntity<Page<ECODTO>> listECOs(
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("REST request to list ECOs with pagination: {}", pageable);
        Page<ECODTO> ecos = ecoService.listECOs(pageable);
        return ResponseEntity.ok(ecos);
    }

    // =========================================================================
    // ECO Workflow Operations
    // =========================================================================

    @PostMapping("/ecos/{id}/submit")
    @Operation(summary = "Submit an ECO", description = "Submits the ECO for approval review.")
    @ApiResponse(responseCode = "200", description = "ECO submitted successfully")
    @ApiResponse(responseCode = "404", description = "ECO not found")
    @ApiResponse(responseCode = "409", description = "Invalid status transition")
    public ResponseEntity<ECODTO> submitECO(
            @Parameter(description = "ECO ID") @PathVariable Long id) {
        log.info("REST request to submit ECO id: {}", id);
        ECODTO eco = ecoService.submitECO(id);
        return ResponseEntity.ok(eco);
    }

    @PostMapping("/ecos/{id}/approve")
    @Operation(summary = "Approve an ECO", description = "Approves the ECO at a specific workflow stage.")
    @ApiResponse(responseCode = "200", description = "ECO approved at stage")
    @ApiResponse(responseCode = "404", description = "ECO not found")
    @ApiResponse(responseCode = "409", description = "Invalid status transition")
    public ResponseEntity<ECOApprovalDTO> approveECO(
            @Parameter(description = "ECO ID") @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long approverId = ((Number) body.get("approverId")).longValue();
        String stage = (String) body.get("stage");
        String comments = (String) body.get("comments");
        log.info("REST request to approve ECO id: {} at stage: {} by approver: {}", id, stage, approverId);
        ECOApprovalDTO approval = ecoService.approveECO(id, approverId, stage, comments);
        return ResponseEntity.ok(approval);
    }

    @PostMapping("/ecos/{id}/reject")
    @Operation(summary = "Reject an ECO", description = "Rejects the ECO at a specific workflow stage.")
    @ApiResponse(responseCode = "200", description = "ECO rejected at stage")
    @ApiResponse(responseCode = "404", description = "ECO not found")
    @ApiResponse(responseCode = "409", description = "Invalid status transition")
    public ResponseEntity<ECOApprovalDTO> rejectECO(
            @Parameter(description = "ECO ID") @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long approverId = ((Number) body.get("approverId")).longValue();
        String stage = (String) body.get("stage");
        String comments = (String) body.get("comments");
        log.info("REST request to reject ECO id: {} at stage: {} by approver: {}", id, stage, approverId);
        ECOApprovalDTO approval = ecoService.rejectECO(id, approverId, stage, comments);
        return ResponseEntity.ok(approval);
    }

    @PostMapping("/ecos/{id}/apply")
    @Operation(summary = "Apply an ECO", description = "Applies the approved ECO changes to the system.")
    @ApiResponse(responseCode = "200", description = "ECO applied successfully")
    @ApiResponse(responseCode = "404", description = "ECO not found")
    @ApiResponse(responseCode = "409", description = "ECO not in approved status")
    public ResponseEntity<ECODTO> applyECO(
            @Parameter(description = "ECO ID") @PathVariable Long id) {
        log.info("REST request to apply ECO id: {}", id);
        ECODTO eco = ecoService.applyECO(id);
        return ResponseEntity.ok(eco);
    }

    @PostMapping("/ecos/{id}/close")
    @Operation(summary = "Close an ECO", description = "Closes the ECO after all changes have been applied.")
    @ApiResponse(responseCode = "200", description = "ECO closed successfully")
    @ApiResponse(responseCode = "404", description = "ECO not found")
    @ApiResponse(responseCode = "409", description = "ECO not in applied status")
    public ResponseEntity<ECODTO> closeECO(
            @Parameter(description = "ECO ID") @PathVariable Long id) {
        log.info("REST request to close ECO id: {}", id);
        ECODTO eco = ecoService.closeECO(id);
        return ResponseEntity.ok(eco);
    }

    // =========================================================================
    // Component Drafts
    // =========================================================================

    @PostMapping("/ecos/{id}/component-drafts")
    @Operation(summary = "Add a component draft", description = "Adds a component draft to the specified ECO.")
    @ApiResponse(responseCode = "201", description = "Component draft added")
    @ApiResponse(responseCode = "404", description = "ECO not found")
    public ResponseEntity<ComponentDraftDTO> addComponentDraft(
            @Parameter(description = "ECO ID") @PathVariable Long id,
            @RequestBody ComponentDraftDTO draftDTO) {
        log.info("REST request to add component draft to ECO id: {}", id);
        ComponentDraftDTO draft = ecoService.addComponentDraft(id, draftDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(draft);
    }

    @GetMapping("/ecos/{id}/component-drafts")
    @Operation(summary = "List component drafts", description = "Returns all component drafts for the specified ECO.")
    @ApiResponse(responseCode = "200", description = "List of component drafts")
    @ApiResponse(responseCode = "404", description = "ECO not found")
    public ResponseEntity<List<ComponentDraftDTO>> listComponentDrafts(
            @Parameter(description = "ECO ID") @PathVariable Long id) {
        log.debug("REST request to list component drafts for ECO id: {}", id);
        List<ComponentDraftDTO> drafts = ecoService.listComponentDrafts(id);
        return ResponseEntity.ok(drafts);
    }

    // =========================================================================
    // Exception Handlers
    // =========================================================================

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

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<String> handleOptimisticLock(OptimisticLockException ex) {
        log.warn("Optimistic lock conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
