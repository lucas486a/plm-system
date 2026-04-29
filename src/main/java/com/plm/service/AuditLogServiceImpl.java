package com.plm.service;

import com.plm.entity.AuditLog;
import com.plm.entity.User;
import com.plm.repository.AuditLogRepository;
import com.plm.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AuditLog recordAuditLog(String entityType, Long entityId, String action,
                                   String oldValue, String newValue, Long userId, String ipAddress) {
        log.debug("Recording audit log: {} {} on {} (id: {})", action, entityType, entityId);

        AuditLog auditLog = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .ipAddress(ipAddress)
                .timestamp(OffsetDateTime.now())
                .build();

        // Set user if userId is provided
        if (userId != null) {
            userRepository.findById(userId).ifPresent(auditLog::setUser);
        }

        AuditLog savedLog = auditLogRepository.save(auditLog);
        log.debug("Recorded audit log with id: {}", savedLog.getId());
        return savedLog;
    }

    @Override
    public AuditLog getAuditLogById(Long id) {
        log.debug("Fetching audit log by id: {}", id);
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Audit log not found with id: " + id));
    }

    @Override
    public List<AuditLog> getEntityHistory(String entityType, Long entityId) {
        log.debug("Fetching audit history for {} (id: {})", entityType, entityId);
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }

    @Override
    public Page<AuditLog> getAuditLogsByEntityType(String entityType, Pageable pageable) {
        log.debug("Fetching audit logs by entity type: {}, pagination: {}", entityType, pageable);
        return auditLogRepository.findByEntityTypeOrderByTimestampDesc(entityType, pageable);
    }

    @Override
    public Page<AuditLog> getAuditLogsByUserId(Long userId, Pageable pageable) {
        log.debug("Fetching audit logs by user id: {}, pagination: {}", userId, pageable);
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }

    @Override
    public Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable) {
        log.debug("Fetching audit logs by action: {}, pagination: {}", action, pageable);
        return auditLogRepository.findByActionOrderByTimestampDesc(action, pageable);
    }

    @Override
    public Page<AuditLog> getAuditLogsByTimeRange(OffsetDateTime startTime, OffsetDateTime endTime, Pageable pageable) {
        log.debug("Fetching audit logs between {} and {}, pagination: {}", startTime, endTime, pageable);
        return auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(startTime, endTime, pageable);
    }

    @Override
    public Page<AuditLog> getAuditLogsByEntityTypeAndAction(String entityType, String action, Pageable pageable) {
        log.debug("Fetching audit logs by entity type: {} and action: {}, pagination: {}", entityType, action, pageable);
        return auditLogRepository.findByEntityTypeAndActionOrderByTimestampDesc(entityType, action, pageable);
    }

    @Override
    public Page<AuditLog> searchAuditLogs(String entityType, String action, Long userId,
                                          OffsetDateTime startTime, OffsetDateTime endTime, Pageable pageable) {
        log.debug("Searching audit logs: entityType={}, action={}, userId={}, startTime={}, endTime={}, pagination={}",
                entityType, action, userId, startTime, endTime, pageable);
        return auditLogRepository.searchAuditLogs(entityType, action, userId, startTime, endTime, pageable);
    }

    @Override
    public long countByEntityType(String entityType) {
        return auditLogRepository.countByEntityType(entityType);
    }

    @Override
    public long countByEntityTypeAndEntityId(String entityType, Long entityId) {
        return auditLogRepository.countByEntityTypeAndEntityId(entityType, entityId);
    }
}
