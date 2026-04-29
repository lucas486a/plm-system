package com.plm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BOMExplodeNode {

    private Long bomItemId;
    private Long partRevisionId;
    private String partNumber;
    private String partName;
    private String revision;
    private BigDecimal quantity;
    private BigDecimal scrapFactor;
    private String designator;
    private Integer findNumber;
    private Boolean isMounted;
    private Integer level;

    @Builder.Default
    private List<BOMExplodeNode> children = new ArrayList<>();
}
