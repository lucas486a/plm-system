-- =============================================================================
-- PLM System - Performance Optimization Indexes
-- Version: V2
-- Description: Adds composite and covering indexes for frequently queried
--              columns to optimize common PLM query patterns.
-- =============================================================================

-- =============================================================================
-- COMPOSITE INDEXES FOR BOM EXPANSION QUERIES
-- =============================================================================

-- Composite index on bom_items for efficient BOM expansion (bom_id + part_revision_id)
-- Covers the most common BOM item lookup pattern
CREATE INDEX idx_bom_items_bom_id_part_revision_id ON bom_items(bom_id, part_revision_id)
    WHERE is_deleted = FALSE;

-- Composite index on boms for assembly lookup with status filter
CREATE INDEX idx_boms_assembly_id_status ON boms(assembly_id, status)
    WHERE is_deleted = FALSE;

-- =============================================================================
-- INDEXES FOR PART LOOKUP OPTIMIZATION
-- =============================================================================

-- Composite index for part revision lookup by part_id and latest revision flag
CREATE INDEX idx_part_revisions_part_id_latest ON part_revisions(part_id, is_latest_revision)
    WHERE is_deleted = FALSE;

-- Index for part number search (case-insensitive)
CREATE INDEX idx_parts_part_number_lower ON parts(LOWER(part_number))
    WHERE is_deleted = FALSE;

-- =============================================================================
-- INDEXES FOR DOCUMENT LOOKUP OPTIMIZATION
-- =============================================================================

-- Composite index for document revision lookup by document_id and latest revision flag
CREATE INDEX idx_document_revisions_doc_id_latest ON document_revisions(document_id, is_latest_revision)
    WHERE is_deleted = FALSE;

-- Index for document number search (case-insensitive)
CREATE INDEX idx_documents_doc_number_lower ON documents(LOWER(document_number))
    WHERE is_deleted = FALSE;

-- =============================================================================
-- INDEXES FOR ASSEMBLY LOOKUP OPTIMIZATION
-- =============================================================================

-- Index for assembly part number search (case-insensitive)
CREATE INDEX idx_assemblies_part_number_lower ON assemblies(LOWER(part_number))
    WHERE is_deleted = FALSE;

-- =============================================================================
-- INDEXES FOR ECR/ECO QUERY OPTIMIZATION
-- =============================================================================

-- Composite index for ECO by ECR reference and status
CREATE INDEX idx_ecos_ecr_id_status ON ecos(ecr_id, status)
    WHERE is_deleted = FALSE;

-- Composite index for ECR by status and priority
CREATE INDEX idx_ecrs_status_priority ON ecrs(status, priority)
    WHERE is_deleted = FALSE;

-- =============================================================================
-- INDEXES FOR AUDIT LOG QUERY OPTIMIZATION
-- =============================================================================

-- Composite index for audit log by entity type and timestamp
CREATE INDEX idx_audit_logs_entity_type_timestamp ON audit_logs(entity_type, timestamp DESC);

-- Composite index for audit log by user and timestamp
CREATE INDEX idx_audit_logs_user_id_timestamp ON audit_logs(user_id, timestamp DESC)
    WHERE user_id IS NOT NULL;

-- =============================================================================
-- COMMENTS FOR DOCUMENTATION
-- =============================================================================

COMMENT ON INDEX idx_bom_items_bom_id_part_revision_id IS 'Composite index for BOM expansion queries';
COMMENT ON INDEX idx_boms_assembly_id_status IS 'Composite index for assembly BOM lookup with status filter';
COMMENT ON INDEX idx_part_revisions_part_id_latest IS 'Composite index for latest part revision lookup';
COMMENT ON INDEX idx_document_revisions_doc_id_latest IS 'Composite index for latest document revision lookup';
