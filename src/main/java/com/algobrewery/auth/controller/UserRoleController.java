package com.algobrewery.auth.controller;

import com.algobrewery.auth.dto.UserRoleAssignmentRequest;
import com.algobrewery.auth.dto.UserRoleAssignmentResponse;
import com.algobrewery.auth.service.UserRoleService;
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
     * Organization UUID is now provided via x-app-org-uuid header.
     */
    @PostMapping("/{userUuid}/roles")
    public ResponseEntity<UserRoleAssignmentResponse> assignRoleToUser(
            @PathVariable String userUuid,
            @Valid @RequestBody UserRoleAssignmentRequest request,
            HttpServletRequest httpRequest) {
        
        // Validate required headers
        HeaderValidationUtil.validateRequiredHeaders(httpRequest);
        
        String organizationUuid = HeaderValidationUtil.getOrganizationUuid(httpRequest);
        String assignerUuid = HeaderValidationUtil.getUserUuid(httpRequest);
        
        logger.info("Assigning role {} to user {} in organization {}", 
                   request.getRoleUuid(), userUuid, organizationUuid);
        
        UserRoleAssignmentResponse response = userRoleService.assignRoleToUser(
            userUuid, request.getRoleUuid(), organizationUuid, assignerUuid).join();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Remove role from user.
     * DELETE /users/{user_uuid}/roles/{role_uuid}
     * Organization UUID is now provided via x-app-org-uuid header.
     */
    @DeleteMapping("/{userUuid}/roles/{roleUuid}")
    public ResponseEntity<Void> removeRoleFromUser(
            @PathVariable String userUuid,
            @PathVariable String roleUuid,
            HttpServletRequest httpRequest) {
        
        // Validate required headers
        HeaderValidationUtil.validateOrganizationHeader(httpRequest);
        
        String organizationUuid = HeaderValidationUtil.getOrganizationUuid(httpRequest);
        
        logger.info("Removing role {} from user {} in organization {}", 
                   roleUuid, userUuid, organizationUuid);
        
        userRoleService.removeRoleFromUser(userUuid, roleUuid, organizationUuid).join();
        return ResponseEntity.noContent().build();
    }

    /**
     * Get user roles.
     * GET /users/{user_uuid}/roles
     * Organization UUID is now provided via x-app-org-uuid header.
     */
    @GetMapping("/{userUuid}/roles")
    public ResponseEntity<List<UserRoleAssignmentResponse>> getUserRoles(
            @PathVariable String userUuid,
            HttpServletRequest httpRequest) {
        
        // Validate required headers
        HeaderValidationUtil.validateOrganizationHeader(httpRequest);
        
        String organizationUuid = HeaderValidationUtil.getOrganizationUuid(httpRequest);
        
        logger.debug("Getting roles for user {} in organization {}", userUuid, organizationUuid);
        
        List<UserRoleAssignmentResponse> responses = userRoleService.getUserRoles(userUuid, organizationUuid).join();
        return ResponseEntity.ok(responses);
    }
}
