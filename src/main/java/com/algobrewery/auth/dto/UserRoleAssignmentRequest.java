package com.algobrewery.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user-role assignment requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleAssignmentRequest {

    @NotBlank(message = "Role UUID is required")
    @JsonProperty("role_uuid")
    private String roleUuid;

    @NotBlank(message = "Organization UUID is required")
    @JsonProperty("organization_uuid")
    private String organizationUuid;

}
