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
@Table(name = "bom_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE bom_items SET is_deleted = true WHERE id = ? AND version = ?")
@Where(clause = "is_deleted = false")
@EntityListeners(AuditingEntityListener.class)
public class BOMItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bom_id", nullable = false)
    @BatchSize(size = 16)
    private BOM bom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_revision_id", nullable = false)
    @BatchSize(size = 16)
    private PartRevision partRevision;

    @Column(nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(length = 100)
    private String designator;

    @Column(name = "find_number")
    private Integer findNumber;

    @Column(name = "is_mounted", nullable = false)
    @Builder.Default
    private Boolean isMounted = true;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "scrap_factor", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal scrapFactor = BigDecimal.ZERO;

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
