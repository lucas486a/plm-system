-- Create admin user for dev/testing
-- Password: admin123 (BCrypt encoded)
INSERT INTO users (username, password_hash, email, full_name, is_active, is_deleted, version, created_at, updated_at)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHx', 'admin@plm.com', 'Admin User', true, false, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
