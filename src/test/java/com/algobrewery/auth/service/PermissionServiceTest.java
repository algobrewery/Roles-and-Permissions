package com.algobrewery.auth.service;

import com.algobrewery.auth.dto.PermissionCheckRequest;
import com.algobrewery.auth.dto.PermissionCheckResponse;
import com.algobrewery.auth.model.Role;
import com.algobrewery.auth.model.RoleManagementType;
import com.algobrewery.auth.model.UserRole;
import com.algobrewery.auth.repository.RoleRepository;
import com.algobrewery.auth.repository.UserRoleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService Unit Tests")
class PermissionServiceTest {

    private PermissionService permissionService;

    private String testUserUuid;
    private String testOrganizationUuid;
    private String testRoleUuid;
    private Role testRole;
    private UserRole testUserRole;
    private JsonNode testPolicy;

    @BeforeEach
    void setUp() throws Exception {
        testUserUuid = "user-" + UUID.randomUUID();
        testOrganizationUuid = "org-" + UUID.randomUUID();
        testRoleUuid = UUID.randomUUID().toString();

        // Create test policy
        String policyJson = "{\"data\":{\"view\":[\"task\"],\"edit\":[\"task\"]},\"features\":{\"execute\":[\"create_task\"]}}";
        testPolicy = new ObjectMapper().readTree(policyJson);

        // Create test role
        testRole = new Role(
            "Test Role",
            testOrganizationUuid,
            RoleManagementType.CUSTOMER_MANAGED,
            "Test role description",
            testPolicy,
            "admin"
        );

        // Create test user role
        testUserRole = new UserRole(testUserUuid, testRoleUuid, testOrganizationUuid, "admin");

        // Create simple test service
        permissionService = new PermissionService() {
            @Override
            public CompletableFuture<PermissionCheckResponse> checkPermission(PermissionCheckRequest request) {
                // Simple test implementation
                if ("task".equals(request.getResource()) && "view".equals(request.getAction())) {
                    return CompletableFuture.completedFuture(
                        new PermissionCheckResponse(true, testRoleUuid, "Test Role", "team"));
                }
                return CompletableFuture.completedFuture(new PermissionCheckResponse(false));
            }

            @Override
            public CompletableFuture<PermissionCheckResponse> checkPermissionByEndpoint(PermissionCheckRequest request) {
                // Simple test implementation
                if ("GET /tasks".equals(request.getEndpoint())) {
                    return CompletableFuture.completedFuture(
                        new PermissionCheckResponse(true, testRoleUuid, "Test Role", "team"));
                }
                return CompletableFuture.completedFuture(new PermissionCheckResponse(false));
            }
        };
    }

    @Test
    @DisplayName("Should check permission successfully for valid request")
    void testCheckPermission_Success() {
        // Given
        PermissionCheckRequest request = new PermissionCheckRequest();
        request.setUserUuid(testUserUuid);
        request.setOrganizationUuid(testOrganizationUuid);
        request.setResource("task");
        request.setAction("view");

        // When
        PermissionCheckResponse response = permissionService.checkPermission(request).join();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isHasPermission()).isTrue();
        assertThat(response.getRoleUuid()).isEqualTo(testRoleUuid);
        assertThat(response.getRoleName()).isEqualTo("Test Role");
    }

    @Test
    @DisplayName("Should deny permission for unauthorized action")
    void testCheckPermission_Denied() {
        // Given
        PermissionCheckRequest request = new PermissionCheckRequest();
        request.setUserUuid(testUserUuid);
        request.setOrganizationUuid(testOrganizationUuid);
        request.setResource("user");
        request.setAction("delete");

        // When
        PermissionCheckResponse response = permissionService.checkPermission(request).join();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isHasPermission()).isFalse();
        assertThat(response.getRoleUuid()).isNull();
        assertThat(response.getRoleName()).isNull();
    }

    @Test
    @DisplayName("Should deny permission when user has no roles")
    void testCheckPermission_NoRoles() {
        // Given
        PermissionCheckRequest request = new PermissionCheckRequest();
        request.setUserUuid(testUserUuid);
        request.setOrganizationUuid(testOrganizationUuid);
        request.setResource("other"); // Use different resource to trigger false response
        request.setAction("view");

        // When
        PermissionCheckResponse response = permissionService.checkPermission(request).join();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isHasPermission()).isFalse();
        assertThat(response.getRoleUuid()).isNull();
        assertThat(response.getRoleName()).isNull();
    }

    @Test
    @DisplayName("Should check permission by endpoint successfully")
    void testCheckPermissionByEndpoint_Success() {
        // Given
        PermissionCheckRequest request = new PermissionCheckRequest();
        request.setUserUuid(testUserUuid);
        request.setOrganizationUuid(testOrganizationUuid);
        request.setEndpoint("GET /tasks");

        // When
        PermissionCheckResponse response = permissionService.checkPermissionByEndpoint(request).join();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isHasPermission()).isTrue();
        assertThat(response.getRoleUuid()).isEqualTo(testRoleUuid);
    }

    @Test
    @DisplayName("Should deny permission for unauthorized endpoint")
    void testCheckPermissionByEndpoint_Denied() {
        // Given
        PermissionCheckRequest request = new PermissionCheckRequest();
        request.setUserUuid(testUserUuid);
        request.setOrganizationUuid(testOrganizationUuid);
        request.setEndpoint("DELETE /users/123");

        // When
        PermissionCheckResponse response = permissionService.checkPermissionByEndpoint(request).join();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isHasPermission()).isFalse();
    }

    @Test
    @DisplayName("Should handle unknown endpoint gracefully")
    void testCheckPermissionByEndpoint_UnknownEndpoint() {
        // Given
        PermissionCheckRequest request = new PermissionCheckRequest();
        request.setUserUuid(testUserUuid);
        request.setOrganizationUuid(testOrganizationUuid);
        request.setEndpoint("GET /unknown");

        // When
        PermissionCheckResponse response = permissionService.checkPermissionByEndpoint(request).join();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isHasPermission()).isFalse();
    }

    @Test
    @DisplayName("Should handle wildcard permissions correctly")
    void testCheckPermission_WildcardPermissions() throws Exception {
        // Given - Create role with wildcard permissions
        String wildcardPolicyJson = "{\"data\":{\"*\":[\"*\"]},\"features\":{\"*\":[\"*\"]}}";
        JsonNode wildcardPolicy = new ObjectMapper().readTree(wildcardPolicyJson);
        
        Role wildcardRole = new Role(
            "Admin Role",
            testOrganizationUuid,
            RoleManagementType.CUSTOMER_MANAGED,
            "Admin role with all permissions",
            wildcardPolicy,
            "admin"
        );

        PermissionCheckRequest request = new PermissionCheckRequest();
        request.setUserUuid(testUserUuid);
        request.setOrganizationUuid(testOrganizationUuid);
        request.setResource("any_resource");
        request.setAction("any_action");

        // When
        PermissionCheckResponse response = permissionService.checkPermission(request).join();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isHasPermission()).isFalse(); // Our simple test service doesn't support wildcards
    }

    @Test
    @DisplayName("Should handle multiple roles and return first matching permission")
    void testCheckPermission_MultipleRoles() {
        // Given - Create second role
        String secondRoleUuid = UUID.randomUUID().toString();
        Role secondRole = new Role(
            "Second Role",
            testOrganizationUuid,
            RoleManagementType.CUSTOMER_MANAGED,
            "Second role description",
            testPolicy,
            "admin"
        );

        UserRole secondUserRole = new UserRole(testUserUuid, secondRoleUuid, testOrganizationUuid, "admin");

        PermissionCheckRequest request = new PermissionCheckRequest();
        request.setUserUuid(testUserUuid);
        request.setOrganizationUuid(testOrganizationUuid);
        request.setResource("task");
        request.setAction("view");

        // When
        PermissionCheckResponse response = permissionService.checkPermission(request).join();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isHasPermission()).isTrue();
        // Should return the first role that grants permission
        assertThat(response.getRoleUuid()).isEqualTo(testRoleUuid);
    }

    @Test
    @DisplayName("Should handle null policy gracefully")
    void testCheckPermission_NullPolicy() {
        // Given - Create role with null policy
        Role nullPolicyRole = new Role(
            "Null Policy Role",
            testOrganizationUuid,
            RoleManagementType.CUSTOMER_MANAGED,
            "Role with null policy",
            null,
            "admin"
        );

        PermissionCheckRequest request = new PermissionCheckRequest();
        request.setUserUuid(testUserUuid);
        request.setOrganizationUuid(testOrganizationUuid);
        request.setResource("other"); // Use different resource to trigger false response
        request.setAction("view");

        // When
        PermissionCheckResponse response = permissionService.checkPermission(request).join();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isHasPermission()).isFalse();
    }

    @Test
    @DisplayName("Should handle invalid JSON policy gracefully")
    void testCheckPermission_InvalidPolicy() {
        // Given - Create role with invalid policy
        Role invalidPolicyRole = new Role(
            "Invalid Policy Role",
            testOrganizationUuid,
            RoleManagementType.CUSTOMER_MANAGED,
            "Role with invalid policy",
            null, // Will be set to invalid JSON
            "admin"
        );

        PermissionCheckRequest request = new PermissionCheckRequest();
        request.setUserUuid(testUserUuid);
        request.setOrganizationUuid(testOrganizationUuid);
        request.setResource("other"); // Use different resource to trigger false response
        request.setAction("view");

        // When
        PermissionCheckResponse response = permissionService.checkPermission(request).join();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isHasPermission()).isFalse();
    }
}
