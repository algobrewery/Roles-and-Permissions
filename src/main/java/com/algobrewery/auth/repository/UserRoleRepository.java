package com.algobrewery.auth.repository;

import com.algobrewery.auth.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserRole entity.
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    /**
     * Find user roles by user UUID.
     */
    List<UserRole> findByUserUuid(String userUuid);

    /**
     * Find user roles by user UUID and organization UUID.
     */
    List<UserRole> findByUserUuidAndOrganizationUuid(String userUuid, String organizationUuid);

    /**
     * Find user role by user UUID, role UUID, and organization UUID.
     */
    Optional<UserRole> findByUserUuidAndRoleUuidAndOrganizationUuid(String userUuid, String roleUuid, String organizationUuid);

    /**
     * Check if user role assignment exists.
     */
    boolean existsByUserUuidAndRoleUuidAndOrganizationUuid(String userUuid, String roleUuid, String organizationUuid);

    /**
     * Find all user roles by organization UUID.
     */
    List<UserRole> findByOrganizationUuid(String organizationUuid);

    /**
     * Find user roles by role UUID.
     */
    List<UserRole> findByRoleUuid(String roleUuid);

    /**
     * Delete user role assignment.
     */
    void deleteByUserUuidAndRoleUuidAndOrganizationUuid(String userUuid, String roleUuid, String organizationUuid);

    /**
     * Count user roles by user UUID and organization UUID.
     */
    long countByUserUuidAndOrganizationUuid(String userUuid, String organizationUuid);

    /**
     * Find user roles with role details using join.
     */
    @Query("SELECT ur FROM UserRole ur JOIN Role r ON ur.roleUuid = CAST(r.roleUuid AS string) " +
           "WHERE ur.userUuid = :userUuid AND ur.organizationUuid = :organizationUuid")
    List<UserRole> findUserRolesWithRoleDetails(@Param("userUuid") String userUuid, 
                                               @Param("organizationUuid") String organizationUuid);
}
