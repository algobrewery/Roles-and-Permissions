package com.algobrewery.auth.controller;

import com.algobrewery.auth.dto.PermissionCheckRequest;
import com.algobrewery.auth.dto.PermissionCheckResponse;
import com.algobrewery.auth.dto.EndpointPermissionCheckRequest;
import com.algobrewery.auth.service.PermissionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for permission checking operations.
 */
@RestController
@CrossOrigin(origins = "*")
public class PermissionController {

    private static final Logger logger = LoggerFactory.getLogger(PermissionController.class);

    private final PermissionService permissionService;

    @Autowired
    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * Check permission for specific action and resource.
     * POST /permission/check
     */
    @PostMapping("/permission/check")
    public ResponseEntity<PermissionCheckResponse> checkPermission(@Valid @RequestBody PermissionCheckRequest request) {
        logger.debug("Checking permission for user: {}, action: {}, resource: {}", 
                    request.getUserUuid(), request.getAction(), request.getResource());
        
        try {
            PermissionCheckResponse response = permissionService.checkPermission(request).join();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking permission: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Check permission for specific action and resource (legacy endpoint).
     * POST /has-permission
     */
    @PostMapping("/has-permission")
    public ResponseEntity<PermissionCheckResponse> hasPermission(@Valid @RequestBody PermissionCheckRequest request) {
        logger.debug("Checking permission for user: {}, action: {}, resource: {}", 
                    request.getUserUuid(), request.getAction(), request.getResource());
        
        try {
            PermissionCheckResponse response = permissionService.checkPermission(request).join();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking permission: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Check permission using endpoint mapping (for API Gateway).
     * POST /check-permission
     */
    @PostMapping("/check-permission")
    public ResponseEntity<PermissionCheckResponse> checkPermissionByEndpoint(@Valid @RequestBody EndpointPermissionCheckRequest request) {
        logger.debug("Checking permission by endpoint for user: {}, endpoint: {}", 
                    request.getUserUuid(), request.getEndpoint());
        
        try {
            // Convert to PermissionCheckRequest for the service
            PermissionCheckRequest serviceRequest = new PermissionCheckRequest();
            serviceRequest.setUserUuid(request.getUserUuid());
            serviceRequest.setOrganizationUuid(request.getOrganizationUuid());
            serviceRequest.setEndpoint(request.getEndpoint());
            serviceRequest.setResourceId(request.getResourceId());
            
            PermissionCheckResponse response = permissionService.checkPermissionByEndpoint(serviceRequest).join();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking permission by endpoint: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
