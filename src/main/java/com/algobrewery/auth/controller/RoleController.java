package com.algobrewery.auth.controller;

import com.algobrewery.auth.dto.RoleRequest;
import com.algobrewery.auth.dto.RoleResponse;
import com.algobrewery.auth.service.RoleService;
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
     */
    @PostMapping
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleRequest request,
                                                  @RequestHeader("x-app-user-uuid") String userUuid) {
        logger.info("Creating role: {}", request.getRoleName());
        
        try {
            RoleResponse response = roleService.createRole(request, userUuid);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for role creation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creating role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update an existing role.
     * PUT /role/{role_uuid}
     */
    @PutMapping("/{roleUuid}")
    public ResponseEntity<RoleResponse> updateRole(@PathVariable UUID roleUuid,
                                                  @Valid @RequestBody RoleRequest request) {
        logger.info("Updating role: {}", roleUuid);
        
        try {
            RoleResponse response = roleService.updateRole(roleUuid, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for role update: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error updating role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a role.
     * DELETE /role/{role_uuid}
     */
    @DeleteMapping("/{roleUuid}")
    public ResponseEntity<Map<String, Object>> deleteRole(@PathVariable UUID roleUuid) {
        logger.info("Deleting role: {}", roleUuid);
        
        try {
            roleService.deleteRole(roleUuid);
            Map<String, Object> response = Map.of(
                "role_uuid", roleUuid.toString(),
                "status", "deleted"
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for role deletion: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error deleting role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get role by UUID.
     * GET /role/{role_uuid}
     */
    @GetMapping("/{roleUuid}")
    public ResponseEntity<RoleResponse> getRole(@PathVariable UUID roleUuid) {
        logger.debug("Getting role: {}", roleUuid);
        
        try {
            RoleResponse response = roleService.getRole(roleUuid);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Role not found: {}", roleUuid);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error getting role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get roles by organization UUID.
     * GET /role/organization/{organizationUuid}
     */
    @GetMapping("/organization/{organizationUuid}")
    public ResponseEntity<List<RoleResponse>> getRolesByOrganization(@PathVariable String organizationUuid) {
        logger.debug("Getting roles for organization: {}", organizationUuid);
        
        try {
            List<RoleResponse> responses = roleService.getRolesByOrganization(organizationUuid);
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
            List<RoleResponse> responses = roleService.getSystemManagedRoles();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("Error getting system-managed roles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
