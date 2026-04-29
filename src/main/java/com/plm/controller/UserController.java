package com.plm.controller;

import com.plm.dto.CreateUserRequest;
import com.plm.dto.RoleDTO;
import com.plm.dto.UserDTO;
import com.plm.service.UserService;
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

import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Manage users and role assignments")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user account with the provided details.")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserRequest request) {
        UserDTO userDTO = UserDTO.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .build();

        UserDTO createdUser = userService.createUser(userDTO, request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a user by their unique identifier.")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username", description = "Retrieves a user by their username.")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserDTO> getUserByUsername(
            @Parameter(description = "Username") @PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user", description = "Updates an existing user with the provided details.")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user", description = "Deletes a user by their unique identifier.")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List all users", description = "Returns a paginated list of all users.")
    @ApiResponse(responseCode = "200", description = "Paginated list of users")
    public ResponseEntity<Page<UserDTO>> listUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.listUsers(pageable));
    }

    @PostMapping("/{id}/roles")
    @Operation(summary = "Assign a role to user", description = "Assigns a role to the specified user.")
    @ApiResponse(responseCode = "200", description = "Role assigned successfully")
    @ApiResponse(responseCode = "404", description = "User or role not found")
    public ResponseEntity<Set<RoleDTO>> assignRole(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Parameter(description = "Role ID to assign") @RequestParam Long roleId) {
        return ResponseEntity.ok(userService.assignRole(id, roleId));
    }

    @GetMapping("/{id}/roles")
    @Operation(summary = "Get user roles", description = "Returns all roles assigned to the specified user.")
    @ApiResponse(responseCode = "200", description = "Set of roles")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Set<RoleDTO>> getUserRoles(
            @Parameter(description = "User ID") @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserRoles(id));
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    @Operation(summary = "Remove a role from user", description = "Removes a role assignment from the specified user.")
    @ApiResponse(responseCode = "204", description = "Role removed successfully")
    @ApiResponse(responseCode = "404", description = "User or role not found")
    public ResponseEntity<Void> removeRole(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Parameter(description = "Role ID to remove") @PathVariable Long roleId) {
        userService.removeRole(id, roleId);
        return ResponseEntity.noContent().build();
    }
}
