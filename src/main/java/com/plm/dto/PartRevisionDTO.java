package com.plm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartRevisionDTO {

    private Long id;
    private Long partId;
    private String revision;
    private Integer iteration;
    private String lifecycleState;
    private String releaseState;
    private String description;
    private String mpn;
    private String manufacturer;
    private String datasheet;
    private BigDecimal price;
    private String currency;
    private Boolean isLatestRevision;
    private String revisionNotes;
    private OffsetDateTime releasedDate;
    private String releasedBy;
    private Integer version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
