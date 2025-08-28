package com.algobrewery.auth.repository;

import com.algobrewery.auth.model.Role;
import com.algobrewery.auth.model.RoleManagementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Role entity.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Find role by UUID.
     */
    Optional<Role> findByRoleUuid(UUID roleUuid);

    /**
     * Find roles by organization UUID.
     */
    List<Role> findByOrganizationUuid(String organizationUuid);

    /**
     * Find roles by role management type.
     */
    List<Role> findByRoleManagementType(RoleManagementType roleManagementType);

    /**
     * Find roles by organization UUID and role management type.
     */
    List<Role> findByOrganizationUuidAndRoleManagementType(String organizationUuid, RoleManagementType roleManagementType);

    /**
     * Find role by name and organization UUID.
     */
    Optional<Role> findByRoleNameAndOrganizationUuid(String roleName, String organizationUuid);

    /**
     * Check if role exists by name and organization UUID.
     */
    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE r.roleName = :roleName AND r.organizationUuid = :organizationUuid")
    boolean existsByRoleNameAndOrganizationUuid(@Param("roleName") String roleName, @Param("organizationUuid") String organizationUuid);

    /**
     * Find system-managed roles (no organization UUID).
     */
    @Query("SELECT r FROM Role r WHERE r.roleManagementType = :managementType AND r.organizationUuid IS NULL")
    List<Role> findSystemManagedRoles(@Param("managementType") RoleManagementType managementType);

    /**
     * Check if system-managed role exists by name.
     */
    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE r.roleName = :roleName AND r.organizationUuid IS NULL AND r.roleManagementType = :managementType")
    boolean existsByRoleNameAndOrganizationUuidIsNullAndRoleManagementType(@Param("roleName") String roleName, @Param("managementType") RoleManagementType managementType);

    /**
     * Find roles by organization UUID with policy containing specific resource.
     */
    @Query("SELECT r FROM Role r WHERE r.organizationUuid = :organizationUuid AND r.policy LIKE %:resource%")
    List<Role> findByOrganizationUuidAndPolicyContainingResource(@Param("organizationUuid") String organizationUuid, 
                                                                @Param("resource") String resource);

    /**
     * Count roles by role management type.
     */
    long countByRoleManagementType(RoleManagementType roleManagementType);
}
