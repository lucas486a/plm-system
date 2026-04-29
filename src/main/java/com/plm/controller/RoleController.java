package com.plm.controller;

import com.plm.dto.RoleDTO;
import com.plm.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Manage system roles")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @Operation(summary = "Create a new role", description = "Creates a new system role.")
    @ApiResponse(responseCode = "201", description = "Role created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<RoleDTO> createRole(@RequestBody RoleDTO roleDTO) {
        RoleDTO createdRole = roleService.createRole(roleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
    }

    @GetMapping
    @Operation(summary = "List all roles", description = "Returns a list of all system roles.")
    @ApiResponse(responseCode = "200", description = "List of roles")
    public ResponseEntity<List<RoleDTO>> listRoles() {
        return ResponseEntity.ok(roleService.listRoles());
    }
}
