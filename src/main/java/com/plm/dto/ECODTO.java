package com.plm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ECODTO {

    private Long id;
    private String ecoNumber;
    private String title;
    private String description;
    private String status;
    private String type;
    private Long ecrId;
    private String currentStage;
    private String processInstanceId;
    private OffsetDateTime effectiveDate;
    private OffsetDateTime appliedAt;
    private Integer version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
