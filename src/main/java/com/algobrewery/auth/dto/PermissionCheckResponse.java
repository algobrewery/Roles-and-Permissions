package com.algobrewery.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for permission check responses.
 */
@Data
@NoArgsConstructor
public class PermissionCheckResponse {

    @JsonProperty("has_permission")
    private boolean hasPermission;

    @JsonProperty("role_uuid")
    private String roleUuid;

    @JsonProperty("role_name")
    private String roleName;

    @JsonProperty("granted_scope")
    private String grantedScope;

    // Custom constructors for PermissionService
    public PermissionCheckResponse(boolean hasPermission) {
        this.hasPermission = hasPermission;
    }

    public PermissionCheckResponse(boolean hasPermission, String roleUuid, String roleName) {
        this.hasPermission = hasPermission;
        this.roleUuid = roleUuid;
        this.roleName = roleName;
    }

    public PermissionCheckResponse(boolean hasPermission, String roleUuid, String roleName, String grantedScope) {
        this.hasPermission = hasPermission;
        this.roleUuid = roleUuid;
        this.roleName = roleName;
        this.grantedScope = grantedScope;
    }
}
