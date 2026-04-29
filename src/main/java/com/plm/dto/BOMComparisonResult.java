package com.plm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BOMComparisonResult {

    private Long bom1Id;
    private String bom1Name;
    private Long bom2Id;
    private String bom2Name;

    private List<BOMItemDiff> addedItems;
    private List<BOMItemDiff> removedItems;
    private List<BOMItemChange> modifiedItems;

    private int totalAdded;
    private int totalRemoved;
    private int totalModified;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BOMItemDiff {
        private Long partRevisionId;
        private String partNumber;
        private String partName;
        private String revision;
        private BigDecimal quantity;
        private String designator;
        private Integer findNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BOMItemChange {
        private Long partRevisionId;
        private String partNumber;
        private String partName;
        private String revision;
        private BigDecimal bom1Quantity;
        private BigDecimal bom2Quantity;
        private String bom1Designator;
        private String bom2Designator;
        private Integer bom1FindNumber;
        private Integer bom2FindNumber;
    }
}
