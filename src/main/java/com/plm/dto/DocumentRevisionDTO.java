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
public class DocumentRevisionDTO {

    private Long id;
    private Long documentId;
    private String revision;
    private Integer iteration;
    private String lifecycleState;
    private String description;
    private String filePath;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private Boolean isLatestRevision;
    private Boolean revisionLocked;
    private Long partId;
    private Long assemblyId;
    private Integer version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
