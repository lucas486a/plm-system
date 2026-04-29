package com.plm.controller;

import com.plm.dto.*;
import com.plm.service.BOMService;
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

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "BOMs", description = "Manage Bills of Materials, BOM items, snapshots, and cost analysis")
public class BOMController {

    private final BOMService bomService;

    // ==================== BOM CRUD ====================

    @PostMapping("/boms")
    @Operation(summary = "Create a new BOM", description = "Creates a new Bill of Materials.")
    @ApiResponse(responseCode = "201", description = "BOM created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<BOMDTO> createBOM(@RequestBody BOMDTO bomDTO) {
        BOMDTO created = bomService.createBOM(bomDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/boms/{id}")
    @Operation(summary = "Get BOM by ID", description = "Retrieves a BOM by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "BOM found")
    @ApiResponse(responseCode = "404", description = "BOM not found")
    public ResponseEntity<BOMDTO> getBOM(
            @Parameter(description = "BOM ID") @PathVariable Long id) {
        return ResponseEntity.ok(bomService.getBOMById(id));
    }

    @PutMapping("/boms/{id}")
    @Operation(summary = "Update a BOM", description = "Updates an existing BOM with the provided details.")
    @ApiResponse(responseCode = "200", description = "BOM updated successfully")
    @ApiResponse(responseCode = "404", description = "BOM not found")
    public ResponseEntity<BOMDTO> updateBOM(
            @Parameter(description = "BOM ID") @PathVariable Long id,
            @RequestBody BOMDTO bomDTO) {
        return ResponseEntity.ok(bomService.updateBOM(id, bomDTO));
    }

    @DeleteMapping("/boms/{id}")
    @Operation(summary = "Delete a BOM", description = "Deletes a BOM by its unique identifier.")
    @ApiResponse(responseCode = "204", description = "BOM deleted successfully")
    @ApiResponse(responseCode = "404", description = "BOM not found")
    public ResponseEntity<Void> deleteBOM(
            @Parameter(description = "BOM ID") @PathVariable Long id) {
        bomService.deleteBOM(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/boms")
    @Operation(summary = "List all BOMs", description = "Returns a paginated list of all BOMs.")
    @ApiResponse(responseCode = "200", description = "Paginated list of BOMs")
    public ResponseEntity<Page<BOMDTO>> listBOMs(Pageable pageable) {
        return ResponseEntity.ok(bomService.listBOMs(pageable));
    }

    // ==================== BOM Item CRUD ====================

    @PostMapping("/boms/{bomId}/items")
    @Operation(summary = "Add a BOM item", description = "Adds a new item (component) to the specified BOM.")
    @ApiResponse(responseCode = "201", description = "BOM item added successfully")
    @ApiResponse(responseCode = "404", description = "BOM not found")
    public ResponseEntity<BOMItemDTO> addBOMItem(
            @Parameter(description = "BOM ID") @PathVariable Long bomId,
            @RequestBody BOMItemDTO itemDTO) {
        BOMItemDTO created = bomService.addBOMItem(bomId, itemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/boms/{bomId}/items")
    @Operation(summary = "List BOM items", description = "Returns all items for the specified BOM.")
    @ApiResponse(responseCode = "200", description = "List of BOM items")
    @ApiResponse(responseCode = "404", description = "BOM not found")
    public ResponseEntity<List<BOMItemDTO>> getBOMItems(
            @Parameter(description = "BOM ID") @PathVariable Long bomId) {
        return ResponseEntity.ok(bomService.getBOMItems(bomId));
    }

    @PutMapping("/boms/{bomId}/items/{itemId}")
    @Operation(summary = "Update a BOM item", description = "Updates an existing item in the specified BOM.")
    @ApiResponse(responseCode = "200", description = "BOM item updated successfully")
    @ApiResponse(responseCode = "404", description = "BOM or item not found")
    public ResponseEntity<BOMItemDTO> updateBOMItem(
            @Parameter(description = "BOM ID") @PathVariable Long bomId,
            @Parameter(description = "BOM Item ID") @PathVariable Long itemId,
            @RequestBody BOMItemDTO itemDTO) {
        return ResponseEntity.ok(bomService.updateBOMItem(bomId, itemId, itemDTO));
    }

    @DeleteMapping("/boms/{bomId}/items/{itemId}")
    @Operation(summary = "Remove a BOM item", description = "Removes an item from the specified BOM.")
    @ApiResponse(responseCode = "204", description = "BOM item removed successfully")
    @ApiResponse(responseCode = "404", description = "BOM or item not found")
    public ResponseEntity<Void> removeBOMItem(
            @Parameter(description = "BOM ID") @PathVariable Long bomId,
            @Parameter(description = "BOM Item ID") @PathVariable Long itemId) {
        bomService.removeBOMItem(bomId, itemId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Multi-level BOM Expansion ====================

    @GetMapping("/boms/{id}/explode")
    @Operation(summary = "Explode BOM", description = "Returns the full multi-level BOM expansion as a tree structure.")
    @ApiResponse(responseCode = "200", description = "BOM explosion tree")
    @ApiResponse(responseCode = "404", description = "BOM not found")
    public ResponseEntity<List<BOMExplodeNode>> explodeBOM(
            @Parameter(description = "BOM ID") @PathVariable Long id) {
        return ResponseEntity.ok(bomService.explodeBOM(id));
    }

    // ==================== Where-Used Analysis ====================

    @GetMapping("/parts/{id}/where-used")
    @Operation(summary = "Where-used analysis", description = "Returns all BOMs that reference the specified part.")
    @ApiResponse(responseCode = "200", description = "List of BOMs referencing the part")
    @ApiResponse(responseCode = "404", description = "Part not found")
    public ResponseEntity<List<BOMDTO>> whereUsed(
            @Parameter(description = "Part ID") @PathVariable Long id) {
        return ResponseEntity.ok(bomService.whereUsed(id));
    }

    // ==================== BOM Copy ====================

    @PostMapping("/boms/{id}/copy")
    @Operation(summary = "Copy a BOM", description = "Creates a copy of an existing BOM with a new name.")
    @ApiResponse(responseCode = "201", description = "BOM copied successfully")
    @ApiResponse(responseCode = "404", description = "Source BOM not found")
    public ResponseEntity<BOMDTO> copyBOM(
            @Parameter(description = "Source BOM ID") @PathVariable Long id,
            @Parameter(description = "Name for the new BOM") @RequestParam String newName) {
        BOMDTO copied = bomService.copyBOM(id, newName);
        return ResponseEntity.status(HttpStatus.CREATED).body(copied);
    }

    // ==================== BOM Comparison ====================

    @GetMapping("/boms/compare")
    @Operation(summary = "Compare two BOMs", description = "Compares two BOMs and returns the differences.")
    @ApiResponse(responseCode = "200", description = "BOM comparison result")
    @ApiResponse(responseCode = "404", description = "One or both BOMs not found")
    public ResponseEntity<BOMComparisonResult> compareBOMs(
            @Parameter(description = "First BOM ID") @RequestParam Long bom1,
            @Parameter(description = "Second BOM ID") @RequestParam Long bom2) {
        return ResponseEntity.ok(bomService.compareBOMs(bom1, bom2));
    }

    // ==================== BOM Export/Import ====================

    @GetMapping("/boms/{id}/export")
    @Operation(summary = "Export BOM to CSV", description = "Exports the specified BOM as a CSV file.")
    @ApiResponse(responseCode = "200", description = "CSV file content")
    @ApiResponse(responseCode = "404", description = "BOM not found")
    public ResponseEntity<String> exportBOM(
            @Parameter(description = "BOM ID") @PathVariable Long id,
            @Parameter(description = "Export format") @RequestParam(defaultValue = "csv") String format) {
        String csv = bomService.exportBOMToCsv(id);
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=bom-" + id + ".csv")
                .body(csv);
    }

    @PostMapping("/boms/import")
    @Operation(summary = "Import BOM from CSV", description = "Creates a new BOM by importing data from a CSV string.")
    @ApiResponse(responseCode = "201", description = "BOM imported successfully")
    @ApiResponse(responseCode = "400", description = "Invalid CSV data")
    public ResponseEntity<BOMDTO> importBOM(
            @RequestBody String csvData,
            @Parameter(description = "Assembly ID to associate with") @RequestParam Long assemblyId,
            @Parameter(description = "Name for the imported BOM") @RequestParam String bomName) {
        BOMDTO imported = bomService.importBOMFromCsv(csvData, assemblyId, bomName);
        return ResponseEntity.status(HttpStatus.CREATED).body(imported);
    }

    // ==================== BOM Snapshots ====================

    @PostMapping("/boms/{id}/snapshots")
    @Operation(summary = "Create a BOM snapshot", description = "Creates a point-in-time snapshot of the specified BOM.")
    @ApiResponse(responseCode = "201", description = "Snapshot created successfully")
    @ApiResponse(responseCode = "404", description = "BOM not found")
    public ResponseEntity<BOMSnapshot> createSnapshot(
            @Parameter(description = "BOM ID") @PathVariable Long id,
            @Parameter(description = "Optional label for the snapshot") @RequestParam(required = false) String label) {
        BOMSnapshot snapshot = bomService.createSnapshot(id, label);
        return ResponseEntity.status(HttpStatus.CREATED).body(snapshot);
    }

    @GetMapping("/boms/{id}/snapshots")
    @Operation(summary = "List BOM snapshots", description = "Returns all snapshots for the specified BOM.")
    @ApiResponse(responseCode = "200", description = "List of snapshots")
    @ApiResponse(responseCode = "404", description = "BOM not found")
    public ResponseEntity<List<BOMSnapshot>> listSnapshots(
            @Parameter(description = "BOM ID") @PathVariable Long id) {
        return ResponseEntity.ok(bomService.listSnapshots(id));
    }

    // ==================== BOM Cost ====================

    @GetMapping("/boms/{id}/cost")
    @Operation(summary = "Calculate BOM cost", description = "Calculates the total cost of all items in the specified BOM.")
    @ApiResponse(responseCode = "200", description = "Total BOM cost")
    @ApiResponse(responseCode = "404", description = "BOM not found")
    public ResponseEntity<BigDecimal> calculateCost(
            @Parameter(description = "BOM ID") @PathVariable Long id) {
        return ResponseEntity.ok(bomService.calculateBOMCost(id));
    }
}
