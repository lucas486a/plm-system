package com.plm.controller;

import com.plm.entity.AuditLog;
import com.plm.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Query system audit trail and activity history")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/{id}")
    @Operation(summary = "Get audit log by ID", description = "Retrieves a single audit log entry by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "Audit log found")
    @ApiResponse(responseCode = "404", description = "Audit log not found")
    public ResponseEntity<AuditLog> getAuditLogById(
            @Parameter(description = "Audit log ID") @PathVariable Long id) {
        AuditLog auditLog = auditLogService.getAuditLogById(id);
        return ResponseEntity.ok(auditLog);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get entity history", description = "Returns the full audit history for a specific entity instance.")
    @ApiResponse(responseCode = "200", description = "List of audit log entries")
    public ResponseEntity<List<AuditLog>> getEntityHistory(
            @Parameter(description = "Entity type (e.g. Part, Document)") @PathVariable String entityType,
            @Parameter(description = "Entity ID") @PathVariable Long entityId) {
        List<AuditLog> history = auditLogService.getEntityHistory(entityType, entityId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/entity-type/{entityType}")
    @Operation(summary = "Get logs by entity type", description = "Returns paginated audit logs filtered by entity type.")
    @ApiResponse(responseCode = "200", description = "Paginated list of audit logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByEntityType(
            @Parameter(description = "Entity type") @PathVariable String entityType,
            Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogService.getAuditLogsByEntityType(entityType, pageable);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get logs by user", description = "Returns paginated audit logs filtered by the user who performed the action.")
    @ApiResponse(responseCode = "200", description = "Paginated list of audit logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByUserId(
            @Parameter(description = "User ID") @PathVariable Long userId,
            Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogService.getAuditLogsByUserId(userId, pageable);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/action/{action}")
    @Operation(summary = "Get logs by action", description = "Returns paginated audit logs filtered by action type (CREATE, UPDATE, DELETE, etc.).")
    @ApiResponse(responseCode = "200", description = "Paginated list of audit logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByAction(
            @Parameter(description = "Action type") @PathVariable String action,
            Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogService.getAuditLogsByAction(action, pageable);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/time-range")
    @Operation(summary = "Get logs by time range", description = "Returns paginated audit logs within a specified time range.")
    @ApiResponse(responseCode = "200", description = "Paginated list of audit logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByTimeRange(
            @Parameter(description = "Start time (ISO 8601)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startTime,
            @Parameter(description = "End time (ISO 8601)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endTime,
            Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogService.getAuditLogsByTimeRange(startTime, endTime, pageable);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/entity-type/{entityType}/action/{action}")
    @Operation(summary = "Get logs by entity type and action", description = "Returns paginated audit logs filtered by both entity type and action.")
    @ApiResponse(responseCode = "200", description = "Paginated list of audit logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByEntityTypeAndAction(
            @Parameter(description = "Entity type") @PathVariable String entityType,
            @Parameter(description = "Action type") @PathVariable String action,
            Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogService.getAuditLogsByEntityTypeAndAction(entityType, action, pageable);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/search")
    @Operation(summary = "Search audit logs", description = "Searches audit logs with multiple optional criteria.")
    @ApiResponse(responseCode = "200", description = "Paginated search results")
    public ResponseEntity<Page<AuditLog>> searchAuditLogs(
            @Parameter(description = "Entity type filter") @RequestParam(required = false) String entityType,
            @Parameter(description = "Action filter") @RequestParam(required = false) String action,
            @Parameter(description = "User ID filter") @RequestParam(required = false) Long userId,
            @Parameter(description = "Start time (ISO 8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startTime,
            @Parameter(description = "End time (ISO 8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endTime,
            Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogService.searchAuditLogs(
                entityType, action, userId, startTime, endTime, pageable);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/count/entity-type/{entityType}")
    @Operation(summary = "Count logs by entity type", description = "Returns the total count of audit logs for a given entity type.")
    @ApiResponse(responseCode = "200", description = "Count value")
    public ResponseEntity<Long> countByEntityType(
            @Parameter(description = "Entity type") @PathVariable String entityType) {
        long count = auditLogService.countByEntityType(entityType);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/entity-type/{entityType}/entity-id/{entityId}")
    @Operation(summary = "Count logs by entity type and ID", description = "Returns the total count of audit logs for a specific entity instance.")
    @ApiResponse(responseCode = "200", description = "Count value")
    public ResponseEntity<Long> countByEntityTypeAndEntityId(
            @Parameter(description = "Entity type") @PathVariable String entityType,
            @Parameter(description = "Entity ID") @PathVariable Long entityId) {
        long count = auditLogService.countByEntityTypeAndEntityId(entityType, entityId);
        return ResponseEntity.ok(count);
    }
}
