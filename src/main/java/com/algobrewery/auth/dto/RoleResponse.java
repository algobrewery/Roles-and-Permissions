package com.algobrewery.auth.dto;

import com.algobrewery.auth.model.RoleManagementType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for role responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    @JsonProperty("role_uuid")
    private UUID roleUuid;

    @JsonProperty("role_name")
    private String roleName;

    @JsonProperty("organization_uuid")
    private String organizationUuid;

    @JsonProperty("role_management_type")
    private RoleManagementType roleManagementType;

    private String description;
    private String policy;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

}
