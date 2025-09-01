package com.algobrewery.auth.service;

import com.algobrewery.auth.dto.UserRoleAssignmentResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for user-role assignment operations.
 */
public interface UserRoleService {
    CompletableFuture<UserRoleAssignmentResponse> assignRoleToUser(String userUuid, String roleUuid, 
                                                                  String organizationUuid, String assignerUuid);
    CompletableFuture<Void> removeRoleFromUser(String userUuid, String roleUuid, String organizationUuid);
    CompletableFuture<List<UserRoleAssignmentResponse>> getUserRoles(String userUuid, String organizationUuid);
    CompletableFuture<List<UserRoleAssignmentResponse>> getUserRolesByOrganization(String organizationUuid);
    CompletableFuture<List<UserRoleAssignmentResponse>> getUserRolesWithDetails(String userUuid, String organizationUuid);
    CompletableFuture<Boolean> userHasRole(String userUuid, String roleUuid, String organizationUuid);
    CompletableFuture<Long> countUserRoles(String userUuid, String organizationUuid);
}
