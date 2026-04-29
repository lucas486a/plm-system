package com.plm.controller;

import com.plm.dto.ECRDTO;
import com.plm.dto.PartDTO;
import com.plm.service.ECRService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ecrs")
@RequiredArgsConstructor
@Tag(name = "Engineering Change Requests", description = "Manage ECRs, status transitions, and affected parts")
public class ECRController {

    private final ECRService ecrService;

    // =========================================================================
    // CRUD Operations
    // =========================================================================

    @PostMapping
    @Operation(summary = "Create a new ECR", description = "Creates a new Engineering Change Request.")
    @ApiResponse(responseCode = "201", description = "ECR created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<ECRDTO> createECR(@RequestBody ECRDTO ecrDTO) {
        ECRDTO created = ecrService.createECR(ecrDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ECR by ID", description = "Retrieves an ECR by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "ECR found")
    @ApiResponse(responseCode = "404", description = "ECR not found")
    public ResponseEntity<ECRDTO> getECRById(
            @Parameter(description = "ECR ID") @PathVariable Long id) {
        return ResponseEntity.ok(ecrService.getECRById(id));
    }

    @GetMapping("/number/{ecrNumber}")
    @Operation(summary = "Get ECR by number", description = "Retrieves an ECR by its ECR number.")
    @ApiResponse(responseCode = "200", description = "ECR found")
    @ApiResponse(responseCode = "404", description = "ECR not found")
    public ResponseEntity<ECRDTO> getECRByNumber(
            @Parameter(description = "ECR number") @PathVariable String ecrNumber) {
        return ResponseEntity.ok(ecrService.getECRByNumber(ecrNumber));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an ECR", description = "Updates an existing ECR with the provided details.")
    @ApiResponse(responseCode = "200", description = "ECR updated successfully")
    @ApiResponse(responseCode = "404", description = "ECR not found")
    public ResponseEntity<ECRDTO> updateECR(
            @Parameter(description = "ECR ID") @PathVariable Long id,
            @RequestBody ECRDTO ecrDTO) {
        return ResponseEntity.ok(ecrService.updateECR(id, ecrDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an ECR", description = "Deletes an ECR by its unique identifier.")
    @ApiResponse(responseCode = "204", description = "ECR deleted successfully")
    @ApiResponse(responseCode = "404", description = "ECR not found")
    public ResponseEntity<Void> deleteECR(
            @Parameter(description = "ECR ID") @PathVariable Long id) {
        ecrService.deleteECR(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List all ECRs", description = "Returns a paginated list of all ECRs.")
    @ApiResponse(responseCode = "200", description = "Paginated list of ECRs")
    public ResponseEntity<Page<ECRDTO>> listECRs(Pageable pageable) {
        return ResponseEntity.ok(ecrService.listECRs(pageable));
    }

    // =========================================================================
    // Status Management
    // =========================================================================

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit an ECR", description = "Transitions the ECR to submitted status for evaluation.")
    @ApiResponse(responseCode = "200", description = "ECR submitted successfully")
    @ApiResponse(responseCode = "404", description = "ECR not found")
    @ApiResponse(responseCode = "409", description = "Invalid status transition")
    public ResponseEntity<ECRDTO> submitECR(
            @Parameter(description = "ECR ID") @PathVariable Long id) {
        return ResponseEntity.ok(ecrService.submitECR(id));
    }

    @PostMapping("/{id}/evaluate")
    @Operation(summary = "Evaluate an ECR", description = "Transitions the ECR to evaluation status.")
    @ApiResponse(responseCode = "200", description = "ECR moved to evaluation")
    @ApiResponse(responseCode = "404", description = "ECR not found")
    @ApiResponse(responseCode = "409", description = "Invalid status transition")
    public ResponseEntity<ECRDTO> evaluateECR(
            @Parameter(description = "ECR ID") @PathVariable Long id) {
        return ResponseEntity.ok(ecrService.evaluateECR(id));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve an ECR", description = "Approves the ECR, allowing it to be converted to an ECO.")
    @ApiResponse(responseCode = "200", description = "ECR approved")
    @ApiResponse(responseCode = "404", description = "ECR not found")
    @ApiResponse(responseCode = "409", description = "Invalid status transition")
    public ResponseEntity<ECRDTO> approveECR(
            @Parameter(description = "ECR ID") @PathVariable Long id) {
        return ResponseEntity.ok(ecrService.approveECR(id));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject an ECR", description = "Rejects the ECR.")
    @ApiResponse(responseCode = "200", description = "ECR rejected")
    @ApiResponse(responseCode = "404", description = "ECR not found")
    @ApiResponse(responseCode = "409", description = "Invalid status transition")
    public ResponseEntity<ECRDTO> rejectECR(
            @Parameter(description = "ECR ID") @PathVariable Long id) {
        return ResponseEntity.ok(ecrService.rejectECR(id));
    }

    // =========================================================================
    // Assignment
    // =========================================================================

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign an ECR", description = "Assigns the ECR to a specific user for handling.")
    @ApiResponse(responseCode = "200", description = "ECR assigned successfully")
    @ApiResponse(responseCode = "404", description = "ECR or user not found")
    public ResponseEntity<ECRDTO> assignECR(
            @Parameter(description = "ECR ID") @PathVariable Long id,
            @Parameter(description = "User ID to assign to") @RequestParam Long userId) {
        return ResponseEntity.ok(ecrService.assignECR(id, userId));
    }

    // =========================================================================
    // Impact Analysis (Affected Parts)
    // =========================================================================

    @GetMapping("/{id}/affected-parts")
    @Operation(summary = "Get affected parts", description = "Returns all parts affected by this ECR.")
    @ApiResponse(responseCode = "200", description = "List of affected parts")
    @ApiResponse(responseCode = "404", description = "ECR not found")
    public ResponseEntity<List<PartDTO>> getAffectedParts(
            @Parameter(description = "ECR ID") @PathVariable Long id) {
        return ResponseEntity.ok(ecrService.getAffectedParts(id));
    }

    @PostMapping("/{id}/affected-parts")
    @Operation(summary = "Add an affected part", description = "Adds a part to the ECR's list of affected parts.")
    @ApiResponse(responseCode = "201", description = "Affected part added")
    @ApiResponse(responseCode = "404", description = "ECR or part not found")
    public ResponseEntity<PartDTO> addAffectedPart(
            @Parameter(description = "ECR ID") @PathVariable Long id,
            @Parameter(description = "Part ID to add") @RequestParam Long partId) {
        PartDTO added = ecrService.addAffectedPart(id, partId);
        return ResponseEntity.status(HttpStatus.CREATED).body(added);
    }

    @DeleteMapping("/{id}/affected-parts/{partId}")
    @Operation(summary = "Remove an affected part", description = "Removes a part from the ECR's list of affected parts.")
    @ApiResponse(responseCode = "204", description = "Affected part removed")
    @ApiResponse(responseCode = "404", description = "ECR or part not found")
    public ResponseEntity<Void> removeAffectedPart(
            @Parameter(description = "ECR ID") @PathVariable Long id,
            @Parameter(description = "Part ID to remove") @PathVariable Long partId) {
        ecrService.removeAffectedPart(id, partId);
        return ResponseEntity.noContent().build();
    }
}
