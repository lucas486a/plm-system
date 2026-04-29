package com.plm.repository;

import com.plm.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find audit logs by entity type and entity ID, ordered by timestamp descending.
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId);

    /**
     * Find audit logs by entity type, ordered by timestamp descending.
     */
    Page<AuditLog> findByEntityTypeOrderByTimestampDesc(String entityType, Pageable pageable);

    /**
     * Find audit logs by user ID, ordered by timestamp descending.
     */
    Page<AuditLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    /**
     * Find audit logs by action, ordered by timestamp descending.
     */
    Page<AuditLog> findByActionOrderByTimestampDesc(String action, Pageable pageable);

    /**
     * Find audit logs within a time range, ordered by timestamp descending.
     */
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
            OffsetDateTime startTime, OffsetDateTime endTime, Pageable pageable);

    /**
     * Find audit logs by entity type and action, ordered by timestamp descending.
     */
    Page<AuditLog> findByEntityTypeAndActionOrderByTimestampDesc(
            String entityType, String action, Pageable pageable);

    /**
     * Search audit logs by entity type, action, and user ID with pagination.
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:userId IS NULL OR a.user.id = :userId) AND " +
           "(:startTime IS NULL OR a.timestamp >= :startTime) AND " +
           "(:endTime IS NULL OR a.timestamp <= :endTime) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> searchAuditLogs(
            @Param("entityType") String entityType,
            @Param("action") String action,
            @Param("userId") Long userId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime,
            Pageable pageable);

    /**
     * Count audit logs by entity type.
     */
    long countByEntityType(String entityType);

    /**
     * Count audit logs by entity type and entity ID.
     */
    long countByEntityTypeAndEntityId(String entityType, Long entityId);
}
