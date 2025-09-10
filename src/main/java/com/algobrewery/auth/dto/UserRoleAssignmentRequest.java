package com.algobrewery.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user-role assignment requests.
 * Organization UUID is now provided via x-app-org-uuid header.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleAssignmentRequest {

    @NotBlank(message = "Role UUID is required")
    @JsonProperty("role_uuid")
    private String roleUuid;

    // organization_uuid removed - now comes from x-app-org-uuid header

}
