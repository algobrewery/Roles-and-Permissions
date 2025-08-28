package com.algobrewery.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for permission check requests.
 */
@Data
@NoArgsConstructor
public class PermissionCheckRequest {

    @NotBlank(message = "User UUID is required")
    @JsonProperty("user_uuid")
    private String userUuid;

    @NotBlank(message = "Organization UUID is required")
    @JsonProperty("organization_uuid")
    private String organizationUuid;

    @NotBlank(message = "Action is required")
    private String action;

    @NotBlank(message = "Resource is required")
    private String resource;

    // Optional field for endpoint-based checks
    private String endpoint;

    // Optional field for resource ID
    @JsonProperty("resource_id")
    private String resourceId;

    // Custom constructor for PermissionService
    public PermissionCheckRequest(String userUuid, String organizationUuid, String action, String resource) {
        this.userUuid = userUuid;
        this.organizationUuid = organizationUuid;
        this.action = action;
        this.resource = resource;
    }
}
