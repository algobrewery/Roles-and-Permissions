package com.algobrewery.auth.controller;

import com.algobrewery.auth.dto.PermissionCheckRequest;
import com.algobrewery.auth.dto.PermissionCheckResponse;
import com.algobrewery.auth.dto.EndpointPermissionCheckRequest;
import com.algobrewery.auth.service.PermissionService;
import com.algobrewery.auth.util.HeaderValidationUtil;
import jakarta.servlet.http.HttpServletRequest;
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
     * User UUID and Organization UUID are now provided via headers.
     */
    @PostMapping("/permission/check")
    public ResponseEntity<PermissionCheckResponse> checkPermission(
            @Valid @RequestBody PermissionCheckRequest request,
            HttpServletRequest httpRequest) {
        
        // Validate required headers
        HeaderValidationUtil.validateRequiredHeaders(httpRequest);
        
        String userUuid = HeaderValidationUtil.getUserUuid(httpRequest);
        String organizationUuid = HeaderValidationUtil.getOrganizationUuid(httpRequest);
        
        logger.debug("Checking permission for user: {}, action: {}, resource: {}", 
                    userUuid, request.getAction(), request.getResource());
        
        PermissionCheckResponse response = permissionService.checkPermission(
            userUuid, organizationUuid, request).join();
        return ResponseEntity.ok(response);
    }

    /**
     * Check permission for specific action and resource (legacy endpoint).
     * POST /has-permission
     * User UUID and Organization UUID are now provided via headers.
     */
    @PostMapping("/has-permission")
    public ResponseEntity<PermissionCheckResponse> hasPermission(
            @Valid @RequestBody PermissionCheckRequest request,
            HttpServletRequest httpRequest) {
        
        // Validate required headers
        HeaderValidationUtil.validateRequiredHeaders(httpRequest);
        
        String userUuid = HeaderValidationUtil.getUserUuid(httpRequest);
        String organizationUuid = HeaderValidationUtil.getOrganizationUuid(httpRequest);
        
        logger.debug("Checking permission for user: {}, action: {}, resource: {}", 
                    userUuid, request.getAction(), request.getResource());
        
        PermissionCheckResponse response = permissionService.checkPermission(
            userUuid, organizationUuid, request).join();
        return ResponseEntity.ok(response);
    }

    /**
     * Check permission using endpoint mapping (for API Gateway).
     * POST /check-permission
     * User UUID and Organization UUID are now provided via headers.
     */
    @PostMapping("/check-permission")
    public ResponseEntity<PermissionCheckResponse> checkPermissionByEndpoint(
            @Valid @RequestBody EndpointPermissionCheckRequest request,
            HttpServletRequest httpRequest) {
        
        // Validate required headers
        HeaderValidationUtil.validateRequiredHeaders(httpRequest);
        
        String userUuid = HeaderValidationUtil.getUserUuid(httpRequest);
        String organizationUuid = HeaderValidationUtil.getOrganizationUuid(httpRequest);
        
        logger.debug("Checking permission by endpoint for user: {}, endpoint: {}", 
                    userUuid, request.getEndpoint());
        
        // Convert to PermissionCheckRequest for the service
        PermissionCheckRequest serviceRequest = new PermissionCheckRequest();
        serviceRequest.setEndpoint(request.getEndpoint());
        serviceRequest.setResourceId(request.getResourceId());
        
        PermissionCheckResponse response = permissionService.checkPermissionByEndpoint(
            userUuid, organizationUuid, serviceRequest).join();
        return ResponseEntity.ok(response);
    }
}
