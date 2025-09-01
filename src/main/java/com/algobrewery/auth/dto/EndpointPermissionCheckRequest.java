package com.algobrewery.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for endpoint-based permission check requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndpointPermissionCheckRequest {

    @NotBlank(message = "User UUID is required")
    @JsonProperty("user_uuid")
    private String userUuid;

    @NotBlank(message = "Organization UUID is required")
    @JsonProperty("organization_uuid")
    private String organizationUuid;

    @NotBlank(message = "Endpoint is required")
    private String endpoint;

    // Optional field for resource ID
    @JsonProperty("resource_id")
    private String resourceId;
}