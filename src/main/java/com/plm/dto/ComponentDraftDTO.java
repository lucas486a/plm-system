package com.plm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentDraftDTO {

    private Long id;
    private Long ecoId;
    private Long bomId;
    private Long partRevisionId;
    private String partRevisionRevision;
    private BigDecimal quantity;
    private String designator;
    private Integer findNumber;
    private Boolean isMounted;
    private String comment;
    private BigDecimal scrapFactor;
    private String action; // ADD, REMOVE, MODIFY
}
