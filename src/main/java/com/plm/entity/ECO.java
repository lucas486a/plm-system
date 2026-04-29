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
@Table(name = "ecos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE ecos SET is_deleted = true WHERE id = ? AND version = ?")
@Where(clause = "is_deleted = false")
@EntityListeners(AuditingEntityListener.class)
public class ECO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "eco_number", nullable = false, unique = true, length = 100)
    private String ecoNumber;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "DRAFT";

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String type = "ECO";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ecr_id")
    private ECR ecr;

    @Column(name = "current_stage", length = 100)
    private String currentStage;

    @Column(name = "effective_date")
    private OffsetDateTime effectiveDate;

    @Column(name = "process_instance_id", length = 255)
    private String processInstanceId;

    @Column(name = "applied_at")
    private OffsetDateTime appliedAt;

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
