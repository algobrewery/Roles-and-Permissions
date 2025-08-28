-- Create roles table
CREATE TABLE roles (
    role_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_name VARCHAR(100) NOT NULL,
    organization_uuid VARCHAR(36),
    role_management_type VARCHAR(20) NOT NULL CHECK (role_management_type IN ('customer_managed', 'system_managed')),
    description VARCHAR(255),
    policy JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(36) NOT NULL
);

-- Create indexes
CREATE INDEX idx_roles_organization_uuid ON roles(organization_uuid);
CREATE INDEX idx_roles_management_type ON roles(role_management_type);
CREATE INDEX idx_roles_name ON roles(role_name);
CREATE INDEX idx_roles_policy ON roles USING GIN(policy);

-- Create unique constraint for role name within organization
CREATE UNIQUE INDEX idx_roles_name_org_unique ON roles(role_name, organization_uuid) 
WHERE organization_uuid IS NOT NULL;

-- Create unique constraint for system-managed roles (no organization)
CREATE UNIQUE INDEX idx_roles_system_unique ON roles(role_name) 
WHERE organization_uuid IS NULL AND role_management_type = 'system_managed';
