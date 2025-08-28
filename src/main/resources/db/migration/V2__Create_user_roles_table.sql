-- Create user_roles table
CREATE TABLE user_roles (
    user_role_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_uuid VARCHAR(36) NOT NULL,
    role_uuid VARCHAR(36) NOT NULL,
    organization_uuid VARCHAR(36) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NOT NULL
);

-- Create indexes
CREATE INDEX idx_user_roles_user_uuid ON user_roles(user_uuid);
CREATE INDEX idx_user_roles_organization_uuid ON user_roles(organization_uuid);
CREATE INDEX idx_user_roles_role_uuid ON user_roles(role_uuid);

-- Create composite unique constraint
CREATE UNIQUE INDEX idx_user_roles_unique ON user_roles(user_uuid, role_uuid, organization_uuid);

-- Create foreign key constraints
ALTER TABLE user_roles 
ADD CONSTRAINT fk_user_roles_role_uuid 
FOREIGN KEY (role_uuid) REFERENCES roles(role_uuid) ON DELETE CASCADE;
