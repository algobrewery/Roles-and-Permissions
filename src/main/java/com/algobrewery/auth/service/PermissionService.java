package com.algobrewery.auth.service;

import com.algobrewery.auth.dto.PermissionCheckRequest;
import com.algobrewery.auth.dto.PermissionCheckResponse;

import java.util.concurrent.CompletableFuture;

/**
 * Service interface for permission checking operations.
 * Updated to use header-based context instead of request body.
 */
public interface PermissionService {
    /**
     * Check permission for a user with context from headers.
     * 
     * @param userUuid the user UUID from x-app-user-uuid header
     * @param organizationUuid the organization UUID from x-app-org-uuid header
     * @param request the permission check request (without user/org context)
     * @return permission check response
     */
    CompletableFuture<PermissionCheckResponse> checkPermission(String userUuid, String organizationUuid, PermissionCheckRequest request);
    
    /**
     * Check permission by endpoint for a user with context from headers.
     * 
     * @param userUuid the user UUID from x-app-user-uuid header
     * @param organizationUuid the organization UUID from x-app-org-uuid header
     * @param request the permission check request (without user/org context)
     * @return permission check response
     */
    CompletableFuture<PermissionCheckResponse> checkPermissionByEndpoint(String userUuid, String organizationUuid, PermissionCheckRequest request);
    
    // Legacy methods for backward compatibility (deprecated)
    @Deprecated
    CompletableFuture<PermissionCheckResponse> checkPermission(PermissionCheckRequest request);
    @Deprecated
    CompletableFuture<PermissionCheckResponse> checkPermissionByEndpoint(PermissionCheckRequest request);
}
