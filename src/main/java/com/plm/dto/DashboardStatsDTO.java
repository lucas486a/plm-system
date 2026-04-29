package com.plm.dto;

import com.plm.entity.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    private long totalParts;
    private long totalDocuments;
    private long openECRs;
    private long openECOs;
    private List<AuditLog> recentActivities;
}
