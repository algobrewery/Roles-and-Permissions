package com.algobrewery.auth.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing the assignment of a role to a user within an organization.
 */
@Entity
@Table(name = "user_roles", indexes = {
    @Index(name = "idx_user_roles_user_uuid", columnList = "user_uuid"),
    @Index(name = "idx_user_roles_organization_uuid", columnList = "organization_uuid")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_role_uuid", nullable = false, length = 50)
    private UUID userRoleUuid;

    @NotBlank
    @Column(name = "user_uuid", nullable = false, length = 50)
    private String userUuid;

    @NotBlank
    @Column(name = "role_uuid", nullable = false, length = 50)
    private String roleUuid;

    @NotBlank
    @Column(name = "organization_uuid", nullable = false, length = 50)
    private String organizationUuid;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @NotBlank
    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    // Custom constructor for UserRoleService
    public UserRole(String userUuid, String roleUuid, String organizationUuid, String createdBy) {
        this.userUuid = userUuid;
        this.roleUuid = roleUuid;
        this.organizationUuid = organizationUuid;
        this.createdBy = createdBy;
    }
}
