package com.algobrewery.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for permission check requests.
 * User UUID and Organization UUID are now provided via headers (x-app-user-uuid and x-app-org-uuid).
 */
@Data
@NoArgsConstructor
public class PermissionCheckRequest {

    // user_uuid removed - now comes from x-app-user-uuid header
    // organization_uuid removed - now comes from x-app-org-uuid header

    @NotBlank(message = "Action is required")
    private String action;

    @NotBlank(message = "Resource is required")
    private String resource;

    // Optional field for endpoint-based checks
    private String endpoint;

    // Optional field for resource ID
    @JsonProperty("resource_id")
    private String resourceId;

    // Custom constructor for PermissionService (now requires context parameters)
    public PermissionCheckRequest(String userUuid, String organizationUuid, String action, String resource) {
        // Note: userUuid and organizationUuid are now passed separately to service methods
        this.action = action;
        this.resource = resource;
    }
}
