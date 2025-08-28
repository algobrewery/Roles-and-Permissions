package com.algobrewery.auth.dto;

import com.algobrewery.auth.model.RoleManagementType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for role creation and update requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {

    @NotBlank(message = "Role name is required")
    @Size(max = 100, message = "Role name must not exceed 100 characters")
    @JsonProperty("role_name")
    private String roleName;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @JsonProperty("organization_uuid")
    private String organizationUuid;

    @JsonProperty("role_management_type")
    private RoleManagementType roleManagementType;

    @NotBlank(message = "Policy is required")
    private String policy;

}
