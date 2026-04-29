-- =============================================================================
-- PLM System - Core Tables Migration
-- Version: V1
-- Description: Creates all core tables for Part, Document, BOM, ECR/ECO,
--              and User management with full audit trail support.
-- =============================================================================

-- =============================================================================
-- USER MANAGEMENT TABLES
-- =============================================================================

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_is_active ON users(is_active);
CREATE INDEX idx_users_is_deleted ON users(is_deleted);

-- Roles table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_roles_name ON roles(name);
CREATE INDEX idx_roles_is_deleted ON roles(is_deleted);

-- User-Roles junction table (composite PK, no id column)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- =============================================================================
-- PART MANAGEMENT TABLES
-- =============================================================================

-- Parts table (master record)
CREATE TABLE parts (
    id BIGSERIAL PRIMARY KEY,
    part_number VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    part_type VARCHAR(50),
    default_unit VARCHAR(50),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_parts_part_number ON parts(part_number);
CREATE INDEX idx_parts_part_type ON parts(part_type);
CREATE INDEX idx_parts_is_deleted ON parts(is_deleted);

-- Part Revisions table
CREATE TABLE part_revisions (
    id BIGSERIAL PRIMARY KEY,
    part_id BIGINT NOT NULL REFERENCES parts(id) ON DELETE CASCADE,
    revision VARCHAR(50) NOT NULL,
    iteration INTEGER NOT NULL DEFAULT 1,
    lifecycle_state VARCHAR(50),
    release_state VARCHAR(50),
    description TEXT,
    mpn VARCHAR(255),
    manufacturer VARCHAR(255),
    datasheet VARCHAR(500),
    price DECIMAL(15, 4),
    currency VARCHAR(10),
    is_latest_revision BOOLEAN NOT NULL DEFAULT FALSE,
    revision_notes TEXT,
    released_date TIMESTAMP WITH TIME ZONE,
    released_by VARCHAR(100),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    UNIQUE(part_id, revision, iteration)
);

CREATE INDEX idx_part_revisions_part_id ON part_revisions(part_id);
CREATE INDEX idx_part_revisions_lifecycle_state ON part_revisions(lifecycle_state);
CREATE INDEX idx_part_revisions_is_latest_revision ON part_revisions(is_latest_revision);
CREATE INDEX idx_part_revisions_is_deleted ON part_revisions(is_deleted);

-- =============================================================================
-- DOCUMENT MANAGEMENT TABLES
-- =============================================================================

-- Documents table (master record)
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    document_number VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(500) NOT NULL,
    document_type VARCHAR(100),
    description TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_documents_document_number ON documents(document_number);
CREATE INDEX idx_documents_document_type ON documents(document_type);
CREATE INDEX idx_documents_is_deleted ON documents(is_deleted);

-- Assemblies table
CREATE TABLE assemblies (
    id BIGSERIAL PRIMARY KEY,
    part_number VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    revision VARCHAR(50),
    lifecycle_state VARCHAR(50),
    is_latest_revision BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_assemblies_part_number ON assemblies(part_number);
CREATE INDEX idx_assemblies_lifecycle_state ON assemblies(lifecycle_state);
CREATE INDEX idx_assemblies_is_deleted ON assemblies(is_deleted);

-- Document Revisions table
CREATE TABLE document_revisions (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    revision VARCHAR(50) NOT NULL,
    iteration INTEGER NOT NULL DEFAULT 1,
    lifecycle_state VARCHAR(50),
    description TEXT,
    file_path VARCHAR(1000),
    file_name VARCHAR(500),
    file_size BIGINT,
    content_type VARCHAR(255),
    is_latest_revision BOOLEAN NOT NULL DEFAULT FALSE,
    revision_locked BOOLEAN NOT NULL DEFAULT FALSE,
    part_id BIGINT REFERENCES parts(id) ON DELETE SET NULL,
    assembly_id BIGINT REFERENCES assemblies(id) ON DELETE SET NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    UNIQUE(document_id, revision, iteration)
);

CREATE INDEX idx_document_revisions_document_id ON document_revisions(document_id);
CREATE INDEX idx_document_revisions_part_id ON document_revisions(part_id);
CREATE INDEX idx_document_revisions_assembly_id ON document_revisions(assembly_id);
CREATE INDEX idx_document_revisions_lifecycle_state ON document_revisions(lifecycle_state);
CREATE INDEX idx_document_revisions_is_latest_revision ON document_revisions(is_latest_revision);
CREATE INDEX idx_document_revisions_is_deleted ON document_revisions(is_deleted);

-- =============================================================================
-- BOM (BILL OF MATERIALS) TABLES
-- =============================================================================

-- BOMs table
CREATE TABLE boms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    assembly_id BIGINT NOT NULL REFERENCES assemblies(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    version_number INTEGER NOT NULL DEFAULT 1,
    comments TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_boms_assembly_id ON boms(assembly_id);
CREATE INDEX idx_boms_status ON boms(status);
CREATE INDEX idx_boms_is_deleted ON boms(is_deleted);

-- BOM Items table
CREATE TABLE bom_items (
    id BIGSERIAL PRIMARY KEY,
    bom_id BIGINT NOT NULL REFERENCES boms(id) ON DELETE CASCADE,
    part_revision_id BIGINT NOT NULL REFERENCES part_revisions(id) ON DELETE CASCADE,
    quantity DECIMAL(15, 4) NOT NULL DEFAULT 1,
    designator VARCHAR(100),
    find_number INTEGER,
    is_mounted BOOLEAN NOT NULL DEFAULT TRUE,
    comment TEXT,
    scrap_factor DECIMAL(5, 2) DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_bom_items_bom_id ON bom_items(bom_id);
CREATE INDEX idx_bom_items_part_revision_id ON bom_items(part_revision_id);
CREATE INDEX idx_bom_items_find_number ON bom_items(find_number);
CREATE INDEX idx_bom_items_is_deleted ON bom_items(is_deleted);

-- =============================================================================
-- ECR/ECO (ENGINEERING CHANGE) TABLES
-- =============================================================================

-- ECRs (Engineering Change Requests) table
CREATE TABLE ecrs (
    id BIGSERIAL PRIMARY KEY,
    ecr_number VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    assigned_to BIGINT REFERENCES users(id) ON DELETE SET NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_ecrs_ecr_number ON ecrs(ecr_number);
CREATE INDEX idx_ecrs_status ON ecrs(status);
CREATE INDEX idx_ecrs_priority ON ecrs(priority);
CREATE INDEX idx_ecrs_assigned_to ON ecrs(assigned_to);
CREATE INDEX idx_ecrs_is_deleted ON ecrs(is_deleted);

-- ECOs (Engineering Change Orders) table
CREATE TABLE ecos (
    id BIGSERIAL PRIMARY KEY,
    eco_number VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    type VARCHAR(50) NOT NULL DEFAULT 'ECO',
    ecr_id BIGINT REFERENCES ecrs(id) ON DELETE SET NULL,
    current_stage VARCHAR(100),
    effective_date TIMESTAMP WITH TIME ZONE,
    applied_at TIMESTAMP WITH TIME ZONE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_ecos_eco_number ON ecos(eco_number);
CREATE INDEX idx_ecos_status ON ecos(status);
CREATE INDEX idx_ecos_type ON ecos(type);
CREATE INDEX idx_ecos_ecr_id ON ecos(ecr_id);
CREATE INDEX idx_ecos_is_deleted ON ecos(is_deleted);

-- ECO Approvals table
CREATE TABLE eco_approvals (
    id BIGSERIAL PRIMARY KEY,
    eco_id BIGINT NOT NULL REFERENCES ecos(id) ON DELETE CASCADE,
    stage VARCHAR(100) NOT NULL,
    approver_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    decision VARCHAR(50) NOT NULL,
    comments TEXT,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_eco_approvals_eco_id ON eco_approvals(eco_id);
CREATE INDEX idx_eco_approvals_approver_id ON eco_approvals(approver_id);
CREATE INDEX idx_eco_approvals_stage ON eco_approvals(stage);
CREATE INDEX idx_eco_approvals_decision ON eco_approvals(decision);

-- =============================================================================
-- AUDIT LOG TABLE
-- =============================================================================

-- Audit Logs table (no soft delete - audit records are immutable)
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_value JSONB,
    new_value JSONB,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_entity_type_entity_id ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);

-- =============================================================================
-- COMMENTS FOR DOCUMENTATION
-- =============================================================================

COMMENT ON TABLE users IS 'System users with authentication credentials';
COMMENT ON TABLE roles IS 'Authorization roles for RBAC';
COMMENT ON TABLE user_roles IS 'User-role assignments (many-to-many)';
COMMENT ON TABLE parts IS 'Master part records (catalog entries)';
COMMENT ON TABLE part_revisions IS 'Versioned revisions of parts with lifecycle state';
COMMENT ON TABLE documents IS 'Master document records';
COMMENT ON TABLE document_revisions IS 'Versioned revisions of documents with file metadata';
COMMENT ON TABLE assemblies IS 'Assembly definitions (collections of parts)';
COMMENT ON TABLE boms IS 'Bill of Materials headers linked to assemblies';
COMMENT ON TABLE bom_items IS 'Individual line items within a BOM';
COMMENT ON TABLE ecrs IS 'Engineering Change Requests';
COMMENT ON TABLE ecos IS 'Engineering Change Orders (derived from ECRs)';
COMMENT ON TABLE eco_approvals IS 'Approval decisions for ECO workflow stages';
COMMENT ON TABLE audit_logs IS 'Immutable audit trail for all entity changes';
