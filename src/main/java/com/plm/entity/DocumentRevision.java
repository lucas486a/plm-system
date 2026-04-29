package com.plm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Entity
@Table(name = "document_revisions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"document_id", "revision", "iteration"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE document_revisions SET is_deleted = true WHERE id = ? AND version = ?")
@Where(clause = "is_deleted = false")
public class DocumentRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false, length = 50)
    private String revision;

    @Column(nullable = false)
    @Builder.Default
    private Integer iteration = 1;

    @Column(name = "lifecycle_state", length = 50)
    private String lifecycleState;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "file_path", length = 1000)
    private String filePath;

    @Column(name = "file_name", length = 500)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type", length = 255)
    private String contentType;

    @Column(name = "is_latest_revision", nullable = false)
    @Builder.Default
    private Boolean isLatestRevision = false;

    @Column(name = "revision_locked", nullable = false)
    @Builder.Default
    private Boolean revisionLocked = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id")
    private Part part;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assembly_id")
    private Assembly assembly;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
