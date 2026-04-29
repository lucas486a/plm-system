package com.plm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "part_revisions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"part_id", "revision", "iteration"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE part_revisions SET is_deleted = true WHERE id = ? AND version = ?")
@Where(clause = "is_deleted = false")
@EntityListeners(AuditingEntityListener.class)
public class PartRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    @BatchSize(size = 16)
    private Part part;

    @Column(nullable = false, length = 50)
    private String revision;

    @Column(nullable = false)
    @Builder.Default
    private Integer iteration = 1;

    @Column(name = "lifecycle_state", length = 50)
    private String lifecycleState;

    @Column(name = "release_state", length = 50)
    private String releaseState;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String mpn;

    @Column(length = 255)
    private String manufacturer;

    @Column(length = 500)
    private String datasheet;

    @Column(precision = 15, scale = 4)
    private BigDecimal price;

    @Column(length = 10)
    private String currency;

    @Column(name = "is_latest_revision", nullable = false)
    @Builder.Default
    private Boolean isLatestRevision = false;

    @Column(name = "revision_notes", columnDefinition = "TEXT")
    private String revisionNotes;

    @Column(name = "released_date")
    private OffsetDateTime releasedDate;

    @Column(name = "released_by", length = 100)
    private String releasedBy;

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
    @Column(name = "created_by", length = 100, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
