-- Fix VARCHAR length for UUID columns
-- Standard UUID string representation is 36 characters, but we need to account for potential variations
-- Increasing to 50 characters to be safe

-- Update roles table UUID columns
ALTER TABLE roles ALTER COLUMN organization_uuid TYPE VARCHAR(50);
ALTER TABLE roles ALTER COLUMN created_by TYPE VARCHAR(50);

-- Update user_roles table UUID columns  
ALTER TABLE user_roles ALTER COLUMN user_uuid TYPE VARCHAR(50);
ALTER TABLE user_roles ALTER COLUMN role_uuid TYPE VARCHAR(50);
ALTER TABLE user_roles ALTER COLUMN organization_uuid TYPE VARCHAR(50);
ALTER TABLE user_roles ALTER COLUMN created_by TYPE VARCHAR(50);