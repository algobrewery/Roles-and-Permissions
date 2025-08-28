-- Seed system-managed roles
INSERT INTO roles (role_uuid, role_name, organization_uuid, role_management_type, description, policy, created_at, created_by)
VALUES 
    (
        gen_random_uuid(),
        'Owner',
        NULL,
        'system_managed',
        'Full access to all operations across the system',
        '{"data":{"view":["*"],"edit":["*"]},"features":{"execute":["*"]}}',
        CURRENT_TIMESTAMP,
        'system'
    ),
    (
        gen_random_uuid(),
        'Manager',
        NULL,
        'system_managed',
        'Can view/edit users, view organization, approve requests, and generate reports',
        '{"data":{"view":["user_basic_info","user_sensitive_info","organization","task","client"],"edit":["user_basic_info","task"]},"features":{"execute":["approve_requests","generate_reports","assign_task"]}}',
        CURRENT_TIMESTAMP,
        'system'
    ),
    (
        gen_random_uuid(),
        'User',
        NULL,
        'system_managed',
        'Can view and edit own profile only',
        '{"data":{"view":["user_basic_info"],"edit":["user_basic_info"]},"features":{"execute":[]}}',
        CURRENT_TIMESTAMP,
        'system'
    ),
    (
        gen_random_uuid(),
        'Operator',
        NULL,
        'system_managed',
        'System operations and monitoring capabilities',
        '{"data":{"view":["*"],"edit":["task","client"]},"features":{"execute":["system_monitoring","backup_operations","generate_reports"]}}',
        CURRENT_TIMESTAMP,
        'system'
    )
ON CONFLICT (role_name) WHERE organization_uuid IS NULL AND role_management_type = 'system_managed' DO NOTHING;
