package com.plm.controller;

import com.plm.dto.DashboardStatsDTO;
import com.plm.entity.AuditLog;
import com.plm.repository.AuditLogRepository;
import com.plm.repository.DocumentRepository;
import com.plm.repository.ECORepository;
import com.plm.repository.ECRRepository;
import com.plm.repository.PartRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "Dashboard statistics and overview")
public class DashboardController {

    private final PartRepository partRepository;
    private final DocumentRepository documentRepository;
    private final ECRRepository ecrRepository;
    private final ECORepository ecoRepository;
    private final AuditLogRepository auditLogRepository;

    @GetMapping("/stats")
    @Transactional(readOnly = true)
    @Operation(summary = "Get dashboard statistics", description = "Returns aggregated statistics for the dashboard overview.")
    @ApiResponse(responseCode = "200", description = "Dashboard statistics retrieved successfully")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        log.debug("REST request to get dashboard statistics");

        long totalParts = partRepository.count();
        long totalDocuments = documentRepository.count();

        long openECRs = ecrRepository.count()
                - ecrRepository.findByStatus("APPROVED").size()
                - ecrRepository.findByStatus("REJECTED").size();

        long openECOs = ecoRepository.count()
                - ecoRepository.findByStatus("APPLIED").size()
                - ecoRepository.findByStatus("CLOSED").size();

        List<AuditLog> recentActivities = auditLogRepository
                .findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp")))
                .getContent();

        DashboardStatsDTO stats = DashboardStatsDTO.builder()
                .totalParts(totalParts)
                .totalDocuments(totalDocuments)
                .openECRs(openECRs)
                .openECOs(openECOs)
                .recentActivities(recentActivities)
                .build();

        return ResponseEntity.ok(stats);
    }
}
