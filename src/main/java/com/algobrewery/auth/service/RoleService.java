package com.algobrewery.auth.service;

import com.algobrewery.auth.dto.RoleRequest;
import com.algobrewery.auth.dto.RoleResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for role management operations.
 */
public interface RoleService {
    CompletableFuture<RoleResponse> createRole(RoleRequest request, String createdBy);
    CompletableFuture<RoleResponse> updateRole(UUID roleUuid, RoleRequest request);
    CompletableFuture<Void> deleteRole(UUID roleUuid);
    CompletableFuture<RoleResponse> getRole(UUID roleUuid);
    CompletableFuture<List<RoleResponse>> getRolesByOrganization(String organizationUuid);
    CompletableFuture<List<RoleResponse>> getSystemManagedRoles();
    CompletableFuture<Optional<RoleResponse>> getRoleByNameAndOrganization(String roleName, String organizationUuid);
}
