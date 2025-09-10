package com.algobrewery.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for endpoint-based permission check requests.
 * User UUID and Organization UUID are now provided via headers (x-app-user-uuid and x-app-org-uuid).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndpointPermissionCheckRequest {

    // user_uuid removed - now comes from x-app-user-uuid header
    // organization_uuid removed - now comes from x-app-org-uuid header

    @NotBlank(message = "Endpoint is required")
    private String endpoint;

    // Optional field for resource ID
    @JsonProperty("resource_id")
    private String resourceId;
}