package com.plm.service;

import com.plm.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;

public interface AuditLogService {

    /**
     * Record an audit log entry.
     */
    AuditLog recordAuditLog(String entityType, Long entityId, String action,
                            String oldValue, String newValue, Long userId, String ipAddress);

    /**
     * Get audit log by ID.
     */
    AuditLog getAuditLogById(Long id);

    /**
     * Get audit history for a specific entity.
     */
    List<AuditLog> getEntityHistory(String entityType, Long entityId);

    /**
     * Get audit logs by entity type with pagination.
     */
    Page<AuditLog> getAuditLogsByEntityType(String entityType, Pageable pageable);

    /**
     * Get audit logs by user ID with pagination.
     */
    Page<AuditLog> getAuditLogsByUserId(Long userId, Pageable pageable);

    /**
     * Get audit logs by action with pagination.
     */
    Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable);

    /**
     * Get audit logs within a time range with pagination.
     */
    Page<AuditLog> getAuditLogsByTimeRange(OffsetDateTime startTime, OffsetDateTime endTime, Pageable pageable);

    /**
     * Get audit logs by entity type and action with pagination.
     */
    Page<AuditLog> getAuditLogsByEntityTypeAndAction(String entityType, String action, Pageable pageable);

    /**
     * Search audit logs with multiple criteria and pagination.
     */
    Page<AuditLog> searchAuditLogs(String entityType, String action, Long userId,
                                   OffsetDateTime startTime, OffsetDateTime endTime, Pageable pageable);

    /**
     * Count audit logs by entity type.
     */
    long countByEntityType(String entityType);

    /**
     * Count audit logs by entity type and entity ID.
     */
    long countByEntityTypeAndEntityId(String entityType, Long entityId);
}
