package com.algobrewery.auth.config;

import com.algobrewery.auth.model.Role;
import com.algobrewery.auth.model.RoleManagementType;
import com.algobrewery.auth.repository.RoleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Data seeder to initialize system-managed roles.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final RoleRepository roleRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public DataSeeder(RoleRepository roleRepository, ObjectMapper objectMapper) {
        this.roleRepository = roleRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting data seeding...");
        
        // Only seed if no system roles exist
        long systemRoleCount = roleRepository.countByRoleManagementType(RoleManagementType.SYSTEM_MANAGED);
        if (systemRoleCount == 0) {
            logger.info("No system roles found, creating initial roles...");
            seedSystemRoles();
            logger.info("Data seeding completed.");
        } else {
            logger.info("System roles already exist ({} found), skipping seeding.", systemRoleCount);
        }
    }

    private void seedSystemRoles() {
        // Owner role - Full access to all operations
        createSystemRoleIfNotExists("Owner", 
            "Full access to all operations across the system",
            "{\"data\":{\"view\":[\"*\"],\"edit\":[\"*\"]},\"features\":{\"execute\":[\"*\"]}}");

        // Manager role - View/Edit users, view organization, approve requests, generate reports
        createSystemRoleIfNotExists("Manager",
            "Can view/edit users, view organization, approve requests, and generate reports",
            "{\"data\":{\"view\":[\"user_basic_info\",\"user_sensitive_info\",\"organization\",\"task\",\"client\"],\"edit\":[\"user_basic_info\",\"task\"]},\"features\":{\"execute\":[\"approve_requests\",\"generate_reports\",\"assign_task\"]}}");

        // User role - View and edit own profile only
        createSystemRoleIfNotExists("User",
            "Can view and edit own profile only",
            "{\"data\":{\"view\":[\"user_basic_info\"],\"edit\":[\"user_basic_info\"]},\"features\":{\"execute\":[]}}");

        // Operator role - System operations and monitoring
        createSystemRoleIfNotExists("Operator",
            "System operations and monitoring capabilities",
            "{\"data\":{\"view\":[\"*\"],\"edit\":[\"task\",\"client\"]},\"features\":{\"execute\":[\"system_monitoring\",\"backup_operations\",\"generate_reports\"]}}");
    }

    private void createSystemRoleIfNotExists(String roleName, String description, String policy) {
        if (!roleRepository.existsByRoleNameAndOrganizationUuidIsNullAndRoleManagementType(roleName, RoleManagementType.SYSTEM_MANAGED)) {
            try {
                JsonNode policyNode = objectMapper.readTree(policy);
                Role role = new Role(
                    roleName,
                    null, // No organization UUID for system-managed roles
                    RoleManagementType.SYSTEM_MANAGED,
                    description,
                    policyNode,
                    "system" // Created by system
                );
                
                Role savedRole = roleRepository.save(role);
                logger.info("Created system role: {} with UUID: {}", roleName, savedRole.getRoleUuid());
            } catch (Exception e) {
                logger.error("Failed to parse policy JSON for role {}: {}", roleName, e.getMessage());
                throw new RuntimeException("Invalid policy JSON for role: " + roleName, e);
            }
        } else {
            logger.debug("System role '{}' already exists, skipping creation.", roleName);
        }
    }
}
