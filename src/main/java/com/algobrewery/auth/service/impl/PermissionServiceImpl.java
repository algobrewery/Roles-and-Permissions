package com.algobrewery.auth.service.impl;

import com.algobrewery.auth.dto.PermissionCheckRequest;
import com.algobrewery.auth.dto.PermissionCheckResponse;
import com.algobrewery.auth.model.Role;
import com.algobrewery.auth.model.UserRole;
import com.algobrewery.auth.repository.RoleRepository;
import com.algobrewery.auth.repository.UserRoleRepository;
import com.algobrewery.auth.service.PermissionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of PermissionService for permission checking operations.
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public PermissionServiceImpl(UserRoleRepository userRoleRepository, 
                               RoleRepository roleRepository, 
                               ObjectMapper objectMapper) {
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Check if user has permission for a specific action and resource.
     */
    @Override
    @Cacheable(value = "permissions", key = "#request.userUuid + '_' + #request.organizationUuid + '_' + #request.action + '_' + #request.resource")
    public CompletableFuture<PermissionCheckResponse> checkPermission(PermissionCheckRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Checking permission for user: {}, action: {}, resource: {}", 
                        request.getUserUuid(), request.getAction(), request.getResource());

            try {
                // Get user's roles in the organization
                List<UserRole> userRoles = userRoleRepository.findByUserUuidAndOrganizationUuid(
                    request.getUserUuid(), request.getOrganizationUuid());

                if (userRoles.isEmpty()) {
                    logger.debug("No roles found for user: {} in organization: {}", 
                               request.getUserUuid(), request.getOrganizationUuid());
                    return new PermissionCheckResponse(false);
                }

                // Check each role for the required permission
                for (UserRole userRole : userRoles) {
                    Optional<Role> roleOpt = roleRepository.findByRoleUuid(
                        java.util.UUID.fromString(userRole.getRoleUuid()));
                    
                    if (roleOpt.isPresent()) {
                        Role role = roleOpt.get();
                        if (hasPermission(role, request.getAction(), request.getResource())) {
                            logger.debug("Permission granted for user: {} with role: {}", 
                                       request.getUserUuid(), role.getRoleName());
                            return new PermissionCheckResponse(true, role.getRoleUuid().toString(), 
                                                             role.getRoleName(), "team");
                        }
                    }
                }

                logger.debug("Permission denied for user: {} action: {} resource: {}", 
                            request.getUserUuid(), request.getAction(), request.getResource());
                return new PermissionCheckResponse(false);

            } catch (Exception e) {
                logger.error("Error checking permission for user: {}", request.getUserUuid(), e);
                return new PermissionCheckResponse(false);
            }
        });
    }

    /**
     * Check permission using endpoint mapping.
     */
    @Override
    @Cacheable(value = "permissions", key = "#request.userUuid + '_' + #request.organizationUuid + '_' + #request.endpoint")
    public CompletableFuture<PermissionCheckResponse> checkPermissionByEndpoint(PermissionCheckRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Checking permission by endpoint for user: {}, endpoint: {}", 
                        request.getUserUuid(), request.getEndpoint());

            // Map endpoint to action and resource
            EndpointMapping mapping = mapEndpointToActionResource(request.getEndpoint());
            if (mapping == null) {
                logger.warn("Unknown endpoint: {}", request.getEndpoint());
                return new PermissionCheckResponse(false);
            }

            // Create new request with mapped action and resource
            PermissionCheckRequest mappedRequest = new PermissionCheckRequest(
                request.getUserUuid(), 
                request.getOrganizationUuid(), 
                mapping.getAction(), 
                mapping.getResource()
            );

            return checkPermission(mappedRequest).join();
        });
    }

    /**
     * Check if role has permission for action and resource.
     */
    private boolean hasPermission(Role role, String action, String resource) {
        try {
            JsonNode policy = role.getPolicy();
            
            // Check data permissions
            if (policy.has("data")) {
                JsonNode dataNode = policy.get("data");
                if (dataNode.has(action)) {
                    JsonNode actionNode = dataNode.get(action);
                    if (actionNode.isArray()) {
                        for (JsonNode resourceNode : actionNode) {
                            if (resourceNode.asText().equals(resource)) {
                                return true;
                            }
                        }
                    }
                }
            }

            // Check feature permissions
            if (policy.has("features")) {
                JsonNode featuresNode = policy.get("features");
                if (featuresNode.has(action)) {
                    JsonNode actionNode = featuresNode.get(action);
                    if (actionNode.isArray()) {
                        for (JsonNode resourceNode : actionNode) {
                            if (resourceNode.asText().equals(resource)) {
                                return true;
                            }
                        }
                    }
                }
            }

            return false;

        } catch (Exception e) {
            logger.error("Error parsing policy for role: {}", role.getRoleUuid(), e);
            return false;
        }
    }

    /**
     * Map endpoint to action and resource.
     */
    private EndpointMapping mapEndpointToActionResource(String endpoint) {
        // User APIs
        if (endpoint.startsWith("GET /users")) {
            return new EndpointMapping("view", "user_basic_info");
        } else if (endpoint.startsWith("POST /users")) {
            return new EndpointMapping("execute", "create_user");
        } else if (endpoint.startsWith("PATCH /users/") || endpoint.startsWith("PUT /users/")) {
            return new EndpointMapping("edit", "user_basic_info");
        } else if (endpoint.startsWith("DELETE /users/")) {
            return new EndpointMapping("execute", "delete_user");
        }
        
        // Task APIs
        else if (endpoint.startsWith("GET /tasks")) {
            return new EndpointMapping("view", "task");
        } else if (endpoint.startsWith("POST /tasks")) {
            return new EndpointMapping("execute", "create_task");
        } else if (endpoint.startsWith("PUT /tasks/") || endpoint.startsWith("PATCH /tasks/")) {
            return new EndpointMapping("edit", "task");
        } else if (endpoint.startsWith("DELETE /tasks/")) {
            return new EndpointMapping("execute", "delete_task");
        }
        
        // Organization APIs
        else if (endpoint.startsWith("GET /organization")) {
            return new EndpointMapping("view", "organization");
        } else if (endpoint.startsWith("PUT /organization") || endpoint.startsWith("PATCH /organization")) {
            return new EndpointMapping("edit", "organization");
        }
        
        // Client APIs
        else if (endpoint.startsWith("GET /clients")) {
            return new EndpointMapping("view", "client");
        } else if (endpoint.startsWith("POST /clients")) {
            return new EndpointMapping("execute", "create_client");
        } else if (endpoint.startsWith("PUT /clients/") || endpoint.startsWith("PATCH /clients/")) {
            return new EndpointMapping("edit", "client");
        } else if (endpoint.startsWith("DELETE /clients/")) {
            return new EndpointMapping("execute", "delete_client");
        }

        return null;
    }

    /**
     * Inner class for endpoint mapping.
     */
    private static class EndpointMapping {
        private final String action;
        private final String resource;

        public EndpointMapping(String action, String resource) {
            this.action = action;
            this.resource = resource;
        }

        public String getAction() {
            return action;
        }

        public String getResource() {
            return resource;
        }
    }
}