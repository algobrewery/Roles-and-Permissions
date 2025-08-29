package com.algobrewery.auth.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a role in the system.
 * Roles define permissions through a JSON policy document.
 */
@Entity
@Table(name = "roles", indexes = {
    @Index(name = "idx_roles_organization_uuid", columnList = "organization_uuid"),
    @Index(name = "idx_roles_management_type", columnList = "role_management_type"),
    @Index(name = "idx_roles_name", columnList = "role_name")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "role_uuid", nullable = false, length = 50)
    private UUID roleUuid;

    @NotBlank
    @Size(max = 100)
    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    @Column(name = "organization_uuid", length = 50)
    private String organizationUuid;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role_management_type", nullable = false, length = 20)
    private RoleManagementType roleManagementType;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "policy", nullable = false, columnDefinition = "JSON")
    private JsonNode policy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @NotBlank
    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    // Custom constructor for DataSeeder
    public Role(String roleName, String organizationUuid, RoleManagementType roleManagementType, 
                String description, JsonNode policy, String createdBy) {
        this.roleName = roleName;
        this.organizationUuid = organizationUuid;
        this.roleManagementType = roleManagementType;
        this.description = description;
        this.policy = policy;
        this.createdBy = createdBy;
    }
}
