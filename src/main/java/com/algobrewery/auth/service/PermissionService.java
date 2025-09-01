package com.algobrewery.auth.service;

import com.algobrewery.auth.dto.PermissionCheckRequest;
import com.algobrewery.auth.dto.PermissionCheckResponse;

import java.util.concurrent.CompletableFuture;

/**
 * Service interface for permission checking operations.
 */
public interface PermissionService {
    CompletableFuture<PermissionCheckResponse> checkPermission(PermissionCheckRequest request);
    CompletableFuture<PermissionCheckResponse> checkPermissionByEndpoint(PermissionCheckRequest request);
}
