package com.algobrewery.auth.service.impl;

import com.algobrewery.auth.dto.RoleRequest;
import com.algobrewery.auth.dto.RoleResponse;
import com.algobrewery.auth.model.Role;
import com.algobrewery.auth.model.RoleManagementType;
import com.algobrewery.auth.repository.RoleRepository;
import com.algobrewery.auth.service.RoleService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Implementation of RoleServiceInterface for role management operations.
 */
@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    private final RoleRepository roleRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository, ObjectMapper objectMapper) {
        this.roleRepository = roleRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Create a new role.
     */
    @Override
    @CacheEvict(value = "roles", allEntries = true)
    public CompletableFuture<RoleResponse> createRole(RoleRequest request, String createdBy) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Creating role: {}", request.getRoleName());

            // Validate policy JSON
            validatePolicy(request.getPolicy());

            // Check if role name already exists in organization
            if (request.getOrganizationUuid() != null && 
                roleRepository.existsByRoleNameAndOrganizationUuid(request.getRoleName(), request.getOrganizationUuid())) {
                throw new IllegalArgumentException("Role name already exists in this organization");
            }

            Role role = new Role(
                request.getRoleName(),
                request.getOrganizationUuid(),
                request.getRoleManagementType(),
                request.getDescription(),
                request.getPolicy(),
                createdBy
            );

            Role savedRole = roleRepository.save(role);
            logger.info("Role created successfully: {}", savedRole.getRoleUuid());

            return mapToResponse(savedRole);
        });
    }

    /**
     * Update an existing role.
     */
    @Override
    @CacheEvict(value = "roles", allEntries = true)
    public CompletableFuture<RoleResponse> updateRole(UUID roleUuid, RoleRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Updating role: {}", roleUuid);

            Role role = roleRepository.findByRoleUuid(roleUuid)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleUuid));

            // Validate policy JSON
            validatePolicy(request.getPolicy());

            // Update fields (only allow updating name, description, and policy)
            role.setRoleName(request.getRoleName());
            role.setDescription(request.getDescription());
            role.setPolicy(request.getPolicy());
            // Note: roleManagementType and organizationUuid are not updated to maintain data integrity

            Role updatedRole = roleRepository.save(role);
            logger.info("Role updated successfully: {}", roleUuid);

            return mapToResponse(updatedRole);
        });
    }

    /**
     * Delete a role.
     */
    @Override
    @CacheEvict(value = "roles", allEntries = true)
    public CompletableFuture<Void> deleteRole(UUID roleUuid) {
        return CompletableFuture.runAsync(() -> {
            logger.info("Deleting role: {}", roleUuid);

            Role role = roleRepository.findByRoleUuid(roleUuid)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleUuid));

            // Check if role is system-managed
            if (role.getRoleManagementType() == RoleManagementType.SYSTEM_MANAGED) {
                throw new IllegalArgumentException("Cannot delete system-managed role");
            }

            roleRepository.delete(role);
            logger.info("Role deleted successfully: {}", roleUuid);
        });
    }

    /**
     * Get role by UUID.
     */
    @Override
    @Cacheable(value = "roles", key = "#roleUuid")
    public CompletableFuture<RoleResponse> getRole(UUID roleUuid) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Getting role: {}", roleUuid);

            Role role = roleRepository.findByRoleUuid(roleUuid)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleUuid));

            return mapToResponse(role);
        });
    }

    /**
     * Get roles by organization UUID.
     */
    @Override
    @Cacheable(value = "roles", key = "'org_' + #organizationUuid")
    public CompletableFuture<List<RoleResponse>> getRolesByOrganization(String organizationUuid) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Getting roles for organization: {}", organizationUuid);

            List<Role> roles = roleRepository.findByOrganizationUuid(organizationUuid);
            return roles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        });
    }

    /**
     * Get system-managed roles.
     */
    @Override
    @Cacheable(value = "roles", key = "'system_managed'")
    public CompletableFuture<List<RoleResponse>> getSystemManagedRoles() {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Getting system-managed roles");

            List<Role> roles = roleRepository.findSystemManagedRoles(RoleManagementType.SYSTEM_MANAGED);
            return roles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        });
    }

    /**
     * Get role by name and organization UUID.
     */
    @Override
    @Cacheable(value = "roles", key = "'name_' + #roleName + '_' + #organizationUuid")
    public CompletableFuture<Optional<RoleResponse>> getRoleByNameAndOrganization(String roleName, String organizationUuid) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Getting role by name: {} in organization: {}", roleName, organizationUuid);

            return roleRepository.findByRoleNameAndOrganizationUuid(roleName, organizationUuid)
                .map(this::mapToResponse);
        });
    }

    /**
     * Validate policy JSON format.
     */
    private void validatePolicy(JsonNode policy) {
        if (policy == null || policy.isNull()) {
            throw new IllegalArgumentException("Policy cannot be null");
        }
        // JsonNode is already validated JSON, so we just need to check for null
    }

    /**
     * Map Role entity to RoleResponse DTO.
     */
    private RoleResponse mapToResponse(Role role) {
        return new RoleResponse(
            role.getRoleUuid(),
            role.getRoleName(),
            role.getOrganizationUuid(),
            role.getRoleManagementType(),
            role.getDescription(),
            role.getPolicy(),
            role.getCreatedAt(),
            role.getUpdatedAt()
        );
    }
}