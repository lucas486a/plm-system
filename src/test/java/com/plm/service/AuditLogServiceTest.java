package com.plm.service;

import com.plm.entity.AuditLog;
import com.plm.entity.User;
import com.plm.repository.AuditLogRepository;
import com.plm.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    private AuditLog auditLog;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        auditLog = AuditLog.builder()
                .id(1L)
                .entityType("Part")
                .entityId(1L)
                .action("CREATE")
                .oldValue(null)
                .newValue("{\"partNumber\":\"P-001\"}")
                .user(user)
                .ipAddress("127.0.0.1")
                .timestamp(OffsetDateTime.now())
                .build();
    }

    // ==================== recordAuditLog ====================

    @Nested
    @DisplayName("recordAuditLog")
    class RecordAuditLog {

        @Test
        @DisplayName("should record audit log with user")
        void recordAuditLog_withUser() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);

            AuditLog result = auditLogService.recordAuditLog(
                    "Part", 1L, "CREATE", null, "{\"partNumber\":\"P-001\"}", 1L, "127.0.0.1");

            assertThat(result).isNotNull();
            assertThat(result.getEntityType()).isEqualTo("Part");
            assertThat(result.getEntityId()).isEqualTo(1L);
            assertThat(result.getAction()).isEqualTo("CREATE");
            assertThat(result.getUser()).isEqualTo(user);
            assertThat(result.getIpAddress()).isEqualTo("127.0.0.1");
            verify(auditLogRepository).save(any(AuditLog.class));
        }

        @Test
        @DisplayName("should record audit log without user")
        void recordAuditLog_withoutUser() {
            AuditLog logWithoutUser = AuditLog.builder()
                    .id(2L)
                    .entityType("Part")
                    .entityId(1L)
                    .action("CREATE")
                    .timestamp(OffsetDateTime.now())
                    .build();

            when(auditLogRepository.save(any(AuditLog.class))).thenReturn(logWithoutUser);

            AuditLog result = auditLogService.recordAuditLog(
                    "Part", 1L, "CREATE", null, null, null, null);

            assertThat(result).isNotNull();
            assertThat(result.getUser()).isNull();
            verify(userRepository, never()).findById(any());
        }

        @Test
        @DisplayName("should handle non-existent user gracefully")
        void recordAuditLog_userNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());
            when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);

            AuditLog result = auditLogService.recordAuditLog(
                    "Part", 1L, "CREATE", null, null, 99L, null);

            assertThat(result).isNotNull();
            // User should not be set when not found
        }
    }

    // ==================== getAuditLogById ====================

    @Nested
    @DisplayName("getAuditLogById")
    class GetAuditLogById {

        @Test
        @DisplayName("should return audit log when found")
        void getAuditLogById_found() {
            when(auditLogRepository.findById(1L)).thenReturn(Optional.of(auditLog));

            AuditLog result = auditLogService.getAuditLogById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should throw when audit log not found")
        void getAuditLogById_notFound() {
            when(auditLogRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> auditLogService.getAuditLogById(99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Audit log not found");
        }
    }

    // ==================== getEntityHistory ====================

    @Nested
    @DisplayName("getEntityHistory")
    class GetEntityHistory {

        @Test
        @DisplayName("should return entity history")
        void getEntityHistory_success() {
            when(auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc("Part", 1L))
                    .thenReturn(List.of(auditLog));

            List<AuditLog> result = auditLogService.getEntityHistory("Part", 1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEntityType()).isEqualTo("Part");
        }

        @Test
        @DisplayName("should return empty list when no history")
        void getEntityHistory_empty() {
            when(auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc("Part", 999L))
                    .thenReturn(List.of());

            List<AuditLog> result = auditLogService.getEntityHistory("Part", 999L);

            assertThat(result).isEmpty();
        }
    }

    // ==================== getAuditLogsByEntityType ====================

    @Nested
    @DisplayName("getAuditLogsByEntityType")
    class GetAuditLogsByEntityType {

        @Test
        @DisplayName("should return paginated audit logs by entity type")
        void getAuditLogsByEntityType_success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<AuditLog> page = new PageImpl<>(List.of(auditLog), pageable, 1);
            when(auditLogRepository.findByEntityTypeOrderByTimestampDesc("Part", pageable))
                    .thenReturn(page);

            Page<AuditLog> result = auditLogService.getAuditLogsByEntityType("Part", pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    // ==================== getAuditLogsByUserId ====================

    @Nested
    @DisplayName("getAuditLogsByUserId")
    class GetAuditLogsByUserId {

        @Test
        @DisplayName("should return paginated audit logs by user ID")
        void getAuditLogsByUserId_success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<AuditLog> page = new PageImpl<>(List.of(auditLog), pageable, 1);
            when(auditLogRepository.findByUserIdOrderByTimestampDesc(1L, pageable))
                    .thenReturn(page);

            Page<AuditLog> result = auditLogService.getAuditLogsByUserId(1L, pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    // ==================== getAuditLogsByAction ====================

    @Nested
    @DisplayName("getAuditLogsByAction")
    class GetAuditLogsByAction {

        @Test
        @DisplayName("should return paginated audit logs by action")
        void getAuditLogsByAction_success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<AuditLog> page = new PageImpl<>(List.of(auditLog), pageable, 1);
            when(auditLogRepository.findByActionOrderByTimestampDesc("CREATE", pageable))
                    .thenReturn(page);

            Page<AuditLog> result = auditLogService.getAuditLogsByAction("CREATE", pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    // ==================== getAuditLogsByTimeRange ====================

    @Nested
    @DisplayName("getAuditLogsByTimeRange")
    class GetAuditLogsByTimeRange {

        @Test
        @DisplayName("should return paginated audit logs by time range")
        void getAuditLogsByTimeRange_success() {
            Pageable pageable = PageRequest.of(0, 10);
            OffsetDateTime start = OffsetDateTime.now().minusDays(1);
            OffsetDateTime end = OffsetDateTime.now();
            Page<AuditLog> page = new PageImpl<>(List.of(auditLog), pageable, 1);
            when(auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end, pageable))
                    .thenReturn(page);

            Page<AuditLog> result = auditLogService.getAuditLogsByTimeRange(start, end, pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    // ==================== getAuditLogsByEntityTypeAndAction ====================

    @Nested
    @DisplayName("getAuditLogsByEntityTypeAndAction")
    class GetAuditLogsByEntityTypeAndAction {

        @Test
        @DisplayName("should return paginated audit logs by entity type and action")
        void getAuditLogsByEntityTypeAndAction_success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<AuditLog> page = new PageImpl<>(List.of(auditLog), pageable, 1);
            when(auditLogRepository.findByEntityTypeAndActionOrderByTimestampDesc("Part", "CREATE", pageable))
                    .thenReturn(page);

            Page<AuditLog> result = auditLogService.getAuditLogsByEntityTypeAndAction("Part", "CREATE", pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    // ==================== searchAuditLogs ====================

    @Nested
    @DisplayName("searchAuditLogs")
    class SearchAuditLogs {

        @Test
        @DisplayName("should search audit logs with filters")
        void searchAuditLogs_success() {
            Pageable pageable = PageRequest.of(0, 10);
            OffsetDateTime start = OffsetDateTime.now().minusDays(1);
            OffsetDateTime end = OffsetDateTime.now();
            Page<AuditLog> page = new PageImpl<>(List.of(auditLog), pageable, 1);
            when(auditLogRepository.searchAuditLogs("Part", "CREATE", 1L, start, end, pageable))
                    .thenReturn(page);

            Page<AuditLog> result = auditLogService.searchAuditLogs(
                    "Part", "CREATE", 1L, start, end, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should search with null filters")
        void searchAuditLogs_nullFilters() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<AuditLog> page = new PageImpl<>(List.of(auditLog), pageable, 1);
            when(auditLogRepository.searchAuditLogs(null, null, null, null, null, pageable))
                    .thenReturn(page);

            Page<AuditLog> result = auditLogService.searchAuditLogs(
                    null, null, null, null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    // ==================== countByEntityType ====================

    @Nested
    @DisplayName("countByEntityType")
    class CountByEntityType {

        @Test
        @DisplayName("should return count by entity type")
        void countByEntityType_success() {
            when(auditLogRepository.countByEntityType("Part")).thenReturn(5L);

            long count = auditLogService.countByEntityType("Part");

            assertThat(count).isEqualTo(5L);
        }
    }

    // ==================== countByEntityTypeAndEntityId ====================

    @Nested
    @DisplayName("countByEntityTypeAndEntityId")
    class CountByEntityTypeAndEntityId {

        @Test
        @DisplayName("should return count by entity type and entity ID")
        void countByEntityTypeAndEntityId_success() {
            when(auditLogRepository.countByEntityTypeAndEntityId("Part", 1L)).thenReturn(3L);

            long count = auditLogService.countByEntityTypeAndEntityId("Part", 1L);

            assertThat(count).isEqualTo(3L);
        }
    }
}
