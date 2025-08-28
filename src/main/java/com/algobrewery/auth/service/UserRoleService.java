package com.algobrewery.auth.service;

import com.algobrewery.auth.dto.UserRoleAssignmentResponse;
import com.algobrewery.auth.model.Role;
import com.algobrewery.auth.model.UserRole;
import com.algobrewery.auth.repository.RoleRepository;
import com.algobrewery.auth.repository.UserRoleRepository;
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
import java.util.stream.Collectors;

/**
 * Service class for user-role assignment operations.
 */
@Service
@Transactional
public class UserRoleService {

    private static final Logger logger = LoggerFactory.getLogger(UserRoleService.class);

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UserRoleService(UserRoleRepository userRoleRepository, RoleRepository roleRepository) {
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Assign role to user.
     */
    @CacheEvict(value = "permissions", allEntries = true)
    public UserRoleAssignmentResponse assignRoleToUser(String userUuid, String roleUuid, 
                                                      String organizationUuid, String assignerUuid) {
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
    }

    /**
     * Remove role from user.
     */
    @CacheEvict(value = "permissions", allEntries = true)
    public void removeRoleFromUser(String userUuid, String roleUuid, String organizationUuid) {
        logger.info("Removing role {} from user {} in organization {}", roleUuid, userUuid, organizationUuid);

        // Check if assignment exists
        if (!userRoleRepository.existsByUserUuidAndRoleUuidAndOrganizationUuid(userUuid, roleUuid, organizationUuid)) {
            throw new IllegalArgumentException("Role assignment not found");
        }

        // Delete the assignment
        userRoleRepository.deleteByUserUuidAndRoleUuidAndOrganizationUuid(userUuid, roleUuid, organizationUuid);

        logger.info("Role removed successfully from user: {}", userUuid);
    }

    /**
     * Get user roles.
     */
    @Cacheable(value = "user_roles", key = "#userUuid + '_' + #organizationUuid")
    public List<UserRoleAssignmentResponse> getUserRoles(String userUuid, String organizationUuid) {
        logger.debug("Getting roles for user {} in organization {}", userUuid, organizationUuid);

        List<UserRole> userRoles = userRoleRepository.findByUserUuidAndOrganizationUuid(userUuid, organizationUuid);
        return userRoles.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get all user roles by organization.
     */
    @Cacheable(value = "user_roles", key = "'org_' + #organizationUuid")
    public List<UserRoleAssignmentResponse> getUserRolesByOrganization(String organizationUuid) {
        logger.debug("Getting all user roles for organization: {}", organizationUuid);

        List<UserRole> userRoles = userRoleRepository.findByOrganizationUuid(organizationUuid);
        return userRoles.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get user roles with role details.
     */
    @Cacheable(value = "user_roles", key = "'detailed_' + #userUuid + '_' + #organizationUuid")
    public List<UserRoleAssignmentResponse> getUserRolesWithDetails(String userUuid, String organizationUuid) {
        logger.debug("Getting detailed roles for user {} in organization {}", userUuid, organizationUuid);

        List<UserRole> userRoles = userRoleRepository.findUserRolesWithRoleDetails(userUuid, organizationUuid);
        return userRoles.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Check if user has role.
     */
    public boolean userHasRole(String userUuid, String roleUuid, String organizationUuid) {
        return userRoleRepository.existsByUserUuidAndRoleUuidAndOrganizationUuid(userUuid, roleUuid, organizationUuid);
    }

    /**
     * Count user roles.
     */
    public long countUserRoles(String userUuid, String organizationUuid) {
        return userRoleRepository.countByUserUuidAndOrganizationUuid(userUuid, organizationUuid);
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
