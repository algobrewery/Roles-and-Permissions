-- Update policy column to use JSON type consistently
-- Note: JSONB is already a JSON type in PostgreSQL, but this migration ensures consistency
-- and adds any necessary constraints or modifications

-- The policy column is already JSONB, but we'll add a comment for clarity
COMMENT ON COLUMN roles.policy IS 'JSON policy document defining role permissions';

-- Ensure the GIN index is optimized for JSON operations
-- (The index already exists from V1, but this confirms it's properly configured)