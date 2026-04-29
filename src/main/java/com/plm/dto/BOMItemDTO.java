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
public class BOMItemDTO {

    private Long id;
    private Long bomId;
    private Long partRevisionId;
    private String partRevisionRevision;
    private BigDecimal quantity;
    private String designator;
    private Integer findNumber;
    private Boolean isMounted;
    private String comment;
    private BigDecimal scrapFactor;
    private Integer version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
