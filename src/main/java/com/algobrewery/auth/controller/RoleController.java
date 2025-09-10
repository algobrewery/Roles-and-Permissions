package com.algobrewery.auth.controller;

import com.algobrewery.auth.dto.RoleRequest;
import com.algobrewery.auth.dto.RoleResponse;
import com.algobrewery.auth.service.RoleService;
import com.algobrewery.auth.util.HeaderValidationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for role management operations.
 */
@RestController
@RequestMapping("/role")
@CrossOrigin(origins = "*")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Create a new role.
     * POST /role
     * Organization UUID is now provided via x-app-org-uuid header.
     */
    @PostMapping
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleRequest request,
                                                  HttpServletRequest httpRequest) {
        // Validate required headers
        HeaderValidationUtil.validateRequiredHeaders(httpRequest);
        
        String userUuid = HeaderValidationUtil.getUserUuid(httpRequest);
        String organizationUuid = HeaderValidationUtil.getOrganizationUuid(httpRequest);
        
        // Set organization UUID from header to request
        request.setOrganizationUuid(organizationUuid);
        
        logger.info("Creating role: {} in organization: {}", request.getRoleName(), organizationUuid);
        
        RoleResponse response = roleService.createRole(request, userUuid).join();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing role.
     * PUT /role/{role_uuid}
     * Organization UUID is now provided via x-app-org-uuid header.
     */
    @PutMapping("/{roleUuid}")
    public ResponseEntity<RoleResponse> updateRole(@PathVariable UUID roleUuid,
                                                  @Valid @RequestBody RoleRequest request,
                                                  HttpServletRequest httpRequest) {
        // Validate required headers
        HeaderValidationUtil.validateRequiredHeaders(httpRequest);
        
        String userUuid = HeaderValidationUtil.getUserUuid(httpRequest);
        String organizationUuid = HeaderValidationUtil.getOrganizationUuid(httpRequest);
        
        // Set organization UUID from header to request
        request.setOrganizationUuid(organizationUuid);
        
        logger.info("Updating role: {} in organization: {}", roleUuid, organizationUuid);
        
        RoleResponse response = roleService.updateRole(roleUuid, request).join();
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a role.
     * DELETE /role/{role_uuid}
     * Organization UUID is now provided via x-app-org-uuid header.
     */
    @DeleteMapping("/{roleUuid}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID roleUuid,
                                          HttpServletRequest httpRequest) {
        // Validate required headers
        HeaderValidationUtil.validateRequiredHeaders(httpRequest);
        
        String userUuid = HeaderValidationUtil.getUserUuid(httpRequest);
        String organizationUuid = HeaderValidationUtil.getOrganizationUuid(httpRequest);
        
        logger.info("Deleting role: {} in organization: {}", roleUuid, organizationUuid);
        
        roleService.deleteRole(roleUuid).join();
        return ResponseEntity.noContent().build();
    }

    /**
     * Get role by UUID.
     * GET /role/{role_uuid}
     */
    @GetMapping("/{roleUuid}")
    public ResponseEntity<RoleResponse> getRole(@PathVariable UUID roleUuid) {
        logger.debug("Getting role: {}", roleUuid);
        
        RoleResponse response = roleService.getRole(roleUuid).join();
        return ResponseEntity.ok(response);
    }

    /**
     * Get roles by organization UUID.
     * GET /role/organization
     * Organization UUID is now provided via x-app-org-uuid header.
     */
    @GetMapping("/organization")
    public ResponseEntity<List<RoleResponse>> getRolesByOrganization(HttpServletRequest httpRequest) {
        // Validate required headers
        HeaderValidationUtil.validateOrganizationHeader(httpRequest);
        
        String organizationUuid = HeaderValidationUtil.getOrganizationUuid(httpRequest);
        
        logger.debug("Getting roles for organization: {}", organizationUuid);
        
        try {
            List<RoleResponse> responses = roleService.getRolesByOrganization(organizationUuid).join();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("Error getting roles for organization: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get system-managed roles.
     * GET /role/system-managed
     */
    @GetMapping("/system-managed")
    public ResponseEntity<List<RoleResponse>> getSystemManagedRoles() {
        logger.debug("Getting system-managed roles");
        
        try {
            List<RoleResponse> responses = roleService.getSystemManagedRoles().join();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("Error getting system-managed roles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
