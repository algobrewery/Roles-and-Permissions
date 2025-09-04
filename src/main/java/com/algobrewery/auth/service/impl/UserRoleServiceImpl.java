package com.algobrewery.auth.service.impl;

import com.algobrewery.auth.dto.UserRoleAssignmentResponse;
import com.algobrewery.auth.model.Role;
import com.algobrewery.auth.model.UserRole;
import com.algobrewery.auth.repository.RoleRepository;
import com.algobrewery.auth.repository.UserRoleRepository;
import com.algobrewery.auth.service.UserRoleService;
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
 * Implementation of UserRoleServiceInterface for user-role assignment operations.
 */
@Service
@Transactional
public class UserRoleServiceImpl implements UserRoleService {

    private static final Logger logger = LoggerFactory.getLogger(UserRoleServiceImpl.class);

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UserRoleServiceImpl(UserRoleRepository userRoleRepository, RoleRepository roleRepository) {
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Assign role to user.
     */
    @Override
    @CacheEvict(value = "permissions", allEntries = true)
    public CompletableFuture<UserRoleAssignmentResponse> assignRoleToUser(String userUuid, String roleUuid, 
                                                                         String organizationUuid, String assignerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Assigning role {} to user {} in organization {}", roleUuid, userUuid, organizationUuid);

            // Validate role exists
            Optional<Role> roleOpt = roleRepository.findByRoleUuid(UUID.fromString(roleUuid));
            if (roleOpt.isEmpty()) {
                throw new IllegalArgumentException("Role not found: " + roleUuid);
            }

            Role role = roleOpt.get();

            // Check if role belongs to organization (for customer-managed roles)
            if (role.getRoleManagementType().getValue().equals("customer_managed") && 
                !organizationUuid.equals(role.getOrganizationUuid())) {
                throw new IllegalArgumentException("Role does not belong to the specified organization");
            }

            // Check if assignment already exists
            if (userRoleRepository.existsByUserUuidAndRoleUuidAndOrganizationUuid(userUuid, roleUuid, organizationUuid)) {
                throw new IllegalArgumentException("Role is already assigned to user in this organization");
            }

            // Create user role assignment
            UserRole userRole = new UserRole(userUuid, roleUuid, organizationUuid, assignerUuid);
            UserRole savedUserRole = userRoleRepository.save(userRole);

            logger.info("Role assigned successfully to user: {}", userUuid);

            return mapToResponse(savedUserRole);
        });
    }

    /**
     * Remove role from user.
     */
    @Override
    @Transactional
    @CacheEvict(value = "permissions", allEntries = true)
    public CompletableFuture<Void> removeRoleFromUser(String userUuid, String roleUuid, String organizationUuid) {
        logger.info("Removing role {} from user {} in organization {}", roleUuid, userUuid, organizationUuid);

        // Check if assignment exists
        if (!userRoleRepository.existsByUserUuidAndRoleUuidAndOrganizationUuid(userUuid, roleUuid, organizationUuid)) {
            throw new IllegalArgumentException("Role assignment not found");
        }

        // Delete the assignment
        userRoleRepository.deleteByUserUuidAndRoleUuidAndOrganizationUuid(userUuid, roleUuid, organizationUuid);

        logger.info("Role removed successfully from user: {}", userUuid);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Get user roles.
     */
    @Override
    @Cacheable(value = "user_roles", key = "#userUuid + '_' + #organizationUuid")
    public CompletableFuture<List<UserRoleAssignmentResponse>> getUserRoles(String userUuid, String organizationUuid) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Getting roles for user {} in organization {}", userUuid, organizationUuid);

            List<UserRole> userRoles = userRoleRepository.findByUserUuidAndOrganizationUuid(userUuid, organizationUuid);
            return userRoles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        });
    }

    /**
     * Get all user roles by organization.
     */
    @Override
    @Cacheable(value = "user_roles", key = "'org_' + #organizationUuid")
    public CompletableFuture<List<UserRoleAssignmentResponse>> getUserRolesByOrganization(String organizationUuid) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Getting all user roles for organization: {}", organizationUuid);

            List<UserRole> userRoles = userRoleRepository.findByOrganizationUuid(organizationUuid);
            return userRoles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        });
    }

    /**
     * Get user roles with role details.
     */
    @Override
    @Cacheable(value = "user_roles", key = "'detailed_' + #userUuid + '_' + #organizationUuid")
    public CompletableFuture<List<UserRoleAssignmentResponse>> getUserRolesWithDetails(String userUuid, String organizationUuid) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Getting detailed roles for user {} in organization {}", userUuid, organizationUuid);

            List<UserRole> userRoles = userRoleRepository.findUserRolesWithRoleDetails(userUuid, organizationUuid);
            return userRoles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        });
    }

    /**
     * Check if user has role.
     */
    @Override
    public CompletableFuture<Boolean> userHasRole(String userUuid, String roleUuid, String organizationUuid) {
        return CompletableFuture.supplyAsync(() -> {
            return userRoleRepository.existsByUserUuidAndRoleUuidAndOrganizationUuid(userUuid, roleUuid, organizationUuid);
        });
    }

    /**
     * Count user roles.
     */
    @Override
    public CompletableFuture<Long> countUserRoles(String userUuid, String organizationUuid) {
        return CompletableFuture.supplyAsync(() -> {
            return userRoleRepository.countByUserUuidAndOrganizationUuid(userUuid, organizationUuid);
        });
    }

    /**
     * Map UserRole entity to UserRoleAssignmentResponse DTO.
     */
    private UserRoleAssignmentResponse mapToResponse(UserRole userRole) {
        return new UserRoleAssignmentResponse(
            userRole.getUserRoleUuid(),
            userRole.getUserUuid(),
            userRole.getRoleUuid(),
            userRole.getOrganizationUuid(),
            userRole.getCreatedAt()
        );
    }
}