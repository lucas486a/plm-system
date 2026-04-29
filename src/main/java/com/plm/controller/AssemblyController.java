package com.plm.controller;

import com.plm.dto.AssemblyDTO;
import com.plm.service.AssemblyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assemblies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Assemblies", description = "Manage product assemblies")
public class AssemblyController {

    private final AssemblyService assemblyService;

    @PostMapping
    @Operation(summary = "Create a new assembly", description = "Creates a new product assembly.")
    @ApiResponse(responseCode = "201", description = "Assembly created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<AssemblyDTO> createAssembly(@RequestBody AssemblyDTO assemblyDTO) {
        log.info("REST request to create assembly: {}", assemblyDTO.getPartNumber());
        AssemblyDTO createdAssembly = assemblyService.createAssembly(assemblyDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAssembly);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get assembly by ID", description = "Retrieves an assembly by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "Assembly found")
    @ApiResponse(responseCode = "404", description = "Assembly not found")
    public ResponseEntity<AssemblyDTO> getAssembly(
            @Parameter(description = "Assembly ID") @PathVariable Long id) {
        log.debug("REST request to get assembly by id: {}", id);
        AssemblyDTO assembly = assemblyService.getAssemblyById(id);
        return ResponseEntity.ok(assembly);
    }

    @GetMapping
    @Operation(summary = "List all assemblies", description = "Returns a paginated list of all assemblies.")
    @ApiResponse(responseCode = "200", description = "Paginated list of assemblies")
    public ResponseEntity<Page<AssemblyDTO>> listAssemblies(
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("REST request to list assemblies with pagination: {}", pageable);
        Page<AssemblyDTO> assemblies = assemblyService.listAssemblies(pageable);
        return ResponseEntity.ok(assemblies);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an assembly", description = "Updates an existing assembly with the provided details.")
    @ApiResponse(responseCode = "200", description = "Assembly updated successfully")
    @ApiResponse(responseCode = "404", description = "Assembly not found")
    public ResponseEntity<AssemblyDTO> updateAssembly(
            @Parameter(description = "Assembly ID") @PathVariable Long id,
            @RequestBody AssemblyDTO assemblyDTO) {
        log.info("REST request to update assembly id: {}", id);
        AssemblyDTO updatedAssembly = assemblyService.updateAssembly(id, assemblyDTO);
        return ResponseEntity.ok(updatedAssembly);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an assembly", description = "Deletes an assembly by its unique identifier.")
    @ApiResponse(responseCode = "204", description = "Assembly deleted successfully")
    @ApiResponse(responseCode = "404", description = "Assembly not found")
    public ResponseEntity<Void> deleteAssembly(
            @Parameter(description = "Assembly ID") @PathVariable Long id) {
        log.info("REST request to delete assembly id: {}", id);
        assemblyService.deleteAssembly(id);
        return ResponseEntity.noContent().build();
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
}
