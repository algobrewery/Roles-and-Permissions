package com.algobrewery.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for user-role assignment responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleAssignmentResponse {

    @JsonProperty("user_role_uuid")
    private UUID userRoleUuid;

    @JsonProperty("user_uuid")
    private String userUuid;

    @JsonProperty("role_uuid")
    private String roleUuid;

    @JsonProperty("organization_uuid")
    private String organizationUuid;

    @JsonProperty("created_at")
    private Instant createdAt;

}
