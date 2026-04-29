package com.plm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BOMSnapshot {

    private Long id;
    private Long bomId;
    private String bomName;
    private String snapshotLabel;
    private String comments;
    private OffsetDateTime snapshotDate;
    private String createdBy;
    private int itemCount;
    private BigDecimal totalCost;
    private List<BOMSnapshotItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BOMSnapshotItem {
        private Long partRevisionId;
        private String partNumber;
        private String partName;
        private String revision;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineCost;
        private String designator;
        private Integer findNumber;
        private Boolean isMounted;
        private BigDecimal scrapFactor;
        private String comment;
    }
}
