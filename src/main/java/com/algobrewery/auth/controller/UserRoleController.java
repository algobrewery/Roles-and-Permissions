package com.algobrewery.auth.controller;

import com.algobrewery.auth.dto.UserRoleAssignmentRequest;
import com.algobrewery.auth.dto.UserRoleAssignmentResponse;
import com.algobrewery.auth.service.UserRoleService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for user-role assignment operations.
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserRoleController {

    private static final Logger logger = LoggerFactory.getLogger(UserRoleController.class);

    private final UserRoleService userRoleService;

    @Autowired
    public UserRoleController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    /**
     * Assign role to user.
     * POST /users/{user_uuid}/roles
     */
    @PostMapping("/{userUuid}/roles")
    public ResponseEntity<UserRoleAssignmentResponse> assignRoleToUser(
            @PathVariable String userUuid,
            @Valid @RequestBody UserRoleAssignmentRequest request,
            @RequestHeader("x-app-user-uuid") String assignerUuid) {
        
        logger.info("Assigning role {} to user {} in organization {}", 
                   request.getRoleUuid(), userUuid, request.getOrganizationUuid());
        
        try {
            UserRoleAssignmentResponse response = userRoleService.assignRoleToUser(
                userUuid, request.getRoleUuid(), request.getOrganizationUuid(), assignerUuid).join();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for role assignment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error assigning role to user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Remove role from user.
     * DELETE /users/{user_uuid}/roles/{role_uuid}
     */
    @DeleteMapping("/{userUuid}/roles/{roleUuid}")
    public ResponseEntity<Void> removeRoleFromUser(
            @PathVariable String userUuid,
            @PathVariable String roleUuid,
            @RequestParam("organization_uuid") String organizationUuid) {
        
        logger.info("Removing role {} from user {} in organization {}", 
                   roleUuid, userUuid, organizationUuid);
        
        try {
            userRoleService.removeRoleFromUser(userUuid, roleUuid, organizationUuid).join();
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for role removal: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error removing role from user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get user roles.
     * GET /users/{user_uuid}/roles
     */
    @GetMapping("/{userUuid}/roles")
    public ResponseEntity<List<UserRoleAssignmentResponse>> getUserRoles(
            @PathVariable String userUuid,
            @RequestParam("organization_uuid") String organizationUuid) {
        
        logger.debug("Getting roles for user {} in organization {}", userUuid, organizationUuid);
        
        try {
            List<UserRoleAssignmentResponse> responses = userRoleService.getUserRoles(userUuid, organizationUuid).join();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("Error getting user roles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
