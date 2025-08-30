package com.algobrewery.auth.integration;

import com.algobrewery.auth.dto.RoleRequest;
import com.algobrewery.auth.dto.RoleResponse;
import com.algobrewery.auth.model.RoleManagementType;
import com.algobrewery.auth.service.RoleService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("integration-test")
@Transactional
@DisplayName("Database Integration Tests with Real PostgreSQL")
class DatabaseIntegrationTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private ObjectMapper objectMapper;

    private Faker faker;
    private JsonNode testPolicy;
    private String testOrganizationUuid;
    private String createdBy;

    @BeforeEach
    void setUp() throws Exception {
        faker = new Faker();
        testPolicy = objectMapper.readTree("{\"data\":{\"view\":[\"task\"],\"edit\":[\"task\"]},\"features\":{\"execute\":[\"create_task\"]}}");
        testOrganizationUuid = "org-" + faker.number().randomNumber();
        createdBy = "user-" + faker.number().randomNumber();
    }

    @Test
    @DisplayName("Should create and retrieve role using real database")
    void testCreateAndRetrieveRole() {
        // Given
        String roleName = "DB Integration Test Role " + faker.number().randomNumber();
        RoleRequest request = new RoleRequest(
            roleName,
            "Database integration test role description",
            testOrganizationUuid,
            RoleManagementType.CUSTOMER_MANAGED,
            testPolicy
        );

        // When
        RoleResponse createdRole = roleService.createRole(request, createdBy);

        // Then
        assertThat(createdRole).isNotNull();
        assertThat(createdRole.getRoleName()).isEqualTo(roleName);
        assertThat(createdRole.getOrganizationUuid()).isEqualTo(testOrganizationUuid);
        assertThat(createdRole.getRoleUuid()).isNotNull();

        // Verify we can retrieve the role
        RoleResponse retrievedRole = roleService.getRole(createdRole.getRoleUuid());
        assertThat(retrievedRole).isNotNull();
        assertThat(retrievedRole.getRoleName()).isEqualTo(roleName);
        assertThat(retrievedRole.getRoleUuid()).isEqualTo(createdRole.getRoleUuid());
    }

    @Test
    @DisplayName("Should update role using real database")
    void testUpdateRole() {
        // Given - Create a role first
        String originalName = "Original DB Role " + faker.number().randomNumber();
        RoleRequest createRequest = new RoleRequest(
            originalName,
            "Original description",
            testOrganizationUuid,
            RoleManagementType.CUSTOMER_MANAGED,
            testPolicy
        );

        RoleResponse createdRole = roleService.createRole(createRequest, createdBy);

        // When - Update the role
        String updatedName = "Updated DB Role " + faker.number().randomNumber();
        RoleRequest updateRequest = new RoleRequest(
            updatedName,
            "Updated description",
            testOrganizationUuid,
            RoleManagementType.CUSTOMER_MANAGED,
            testPolicy
        );

        RoleResponse updatedRole = roleService.updateRole(createdRole.getRoleUuid(), updateRequest);

        // Then
        assertThat(updatedRole).isNotNull();
        assertThat(updatedRole.getRoleName()).isEqualTo(updatedName);
        assertThat(updatedRole.getRoleUuid()).isEqualTo(createdRole.getRoleUuid());
        assertThat(updatedRole.getDescription()).isEqualTo("Updated description");
    }

    @Test
    @DisplayName("Should delete role using real database")
    void testDeleteRole() {
        // Given - Create a role first
        String roleName = "Delete DB Test Role " + faker.number().randomNumber();
        RoleRequest createRequest = new RoleRequest(
            roleName,
            "Test description",
            testOrganizationUuid,
            RoleManagementType.CUSTOMER_MANAGED,
            testPolicy
        );

        RoleResponse createdRole = roleService.createRole(createRequest, createdBy);

        // When
        roleService.deleteRole(createdRole.getRoleUuid());

        // Then - Verify the role is deleted by trying to get it (should throw exception)
        assertThatThrownBy(() -> roleService.getRole(createdRole.getRoleUuid()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Role not found");
    }

    @Test
    @DisplayName("Should get roles by organization using real database")
    void testGetRolesByOrganization() {
        // Given - Create multiple roles for the same organization
        String roleName1 = "DB Org Role 1 " + faker.number().randomNumber();
        String roleName2 = "DB Org Role 2 " + faker.number().randomNumber();

        RoleRequest request1 = new RoleRequest(
            roleName1,
            "First role",
            testOrganizationUuid,
            RoleManagementType.CUSTOMER_MANAGED,
            testPolicy
        );

        RoleRequest request2 = new RoleRequest(
            roleName2,
            "Second role",
            testOrganizationUuid,
            RoleManagementType.CUSTOMER_MANAGED,
            testPolicy
        );

        roleService.createRole(request1, createdBy);
        roleService.createRole(request2, createdBy);

        // When
        List<RoleResponse> roles = roleService.getRolesByOrganization(testOrganizationUuid);

        // Then
        assertThat(roles).hasSize(2);
        assertThat(roles).allMatch(role -> role.getOrganizationUuid().equals(testOrganizationUuid));
    }

    @Test
    @DisplayName("Should get system-managed roles using real database")
    void testGetSystemManagedRoles() {
        // When
        List<RoleResponse> systemRoles = roleService.getSystemManagedRoles();

        // Then
        assertThat(systemRoles).isNotNull();
        // System roles should exist (they are created during application startup)
        assertThat(systemRoles).isNotEmpty();
        assertThat(systemRoles).allMatch(role -> 
            role.getRoleManagementType() == RoleManagementType.SYSTEM_MANAGED);
    }

    @Test
    @DisplayName("Should throw exception when creating role with duplicate name")
    void testCreateRoleWithDuplicateName() {
        // Given - Create a role first
        String roleName = "Duplicate DB Role " + faker.number().randomNumber();
        RoleRequest request = new RoleRequest(
            roleName,
            "Test description",
            testOrganizationUuid,
            RoleManagementType.CUSTOMER_MANAGED,
            testPolicy
        );

        roleService.createRole(request, createdBy);

        // When & Then - Try to create another role with the same name
        assertThatThrownBy(() -> roleService.createRole(request, createdBy))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Role name already exists");
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent role")
    void testUpdateNonExistentRole() {
        // Given
        java.util.UUID nonExistentUuid = java.util.UUID.randomUUID();
        RoleRequest updateRequest = new RoleRequest(
            "Non-existent Role",
            "Test description",
            testOrganizationUuid,
            RoleManagementType.CUSTOMER_MANAGED,
            testPolicy
        );

        // When & Then
        assertThatThrownBy(() -> roleService.updateRole(nonExistentUuid, updateRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Role not found");
    }
}
