package com.algobrewery.auth.service;

import com.algobrewery.auth.dto.RoleRequest;
import com.algobrewery.auth.dto.RoleResponse;
import com.algobrewery.auth.model.RoleManagementType;
import com.algobrewery.auth.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RoleServiceTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    private RoleRequest validRoleRequest;

    @BeforeEach
    void setUp() {
        validRoleRequest = new RoleRequest(
            "Test Role",
            "Test role description",
            "org-123",
            RoleManagementType.CUSTOMER_MANAGED,
            "{\"data\":{\"view\":[\"task\"],\"edit\":[\"task\"]},\"features\":{\"execute\":[\"create_task\"]}}"
        );
    }

    @Test
    void testCreateRole() {
        // When
        RoleResponse response = roleService.createRole(validRoleRequest, "user-123");

        // Then
        assertNotNull(response);
        assertNotNull(response.getRoleUuid());
        assertEquals("Test Role", response.getRoleName());
        assertEquals("org-123", response.getOrganizationUuid());
        assertEquals(RoleManagementType.CUSTOMER_MANAGED, response.getRoleManagementType());
        assertEquals("Test role description", response.getDescription());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    void testCreateRoleWithInvalidPolicy() {
        // Given
        RoleRequest invalidRequest = new RoleRequest(
            "Test Role",
            "Test role description",
            "org-123",
            RoleManagementType.CUSTOMER_MANAGED,
            "invalid json"
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            roleService.createRole(invalidRequest, "user-123");
        });
    }

    @Test
    void testGetRole() {
        // Given
        RoleResponse createdRole = roleService.createRole(validRoleRequest, "user-123");

        // When
        RoleResponse retrievedRole = roleService.getRole(createdRole.getRoleUuid());

        // Then
        assertNotNull(retrievedRole);
        assertEquals(createdRole.getRoleUuid(), retrievedRole.getRoleUuid());
        assertEquals(createdRole.getRoleName(), retrievedRole.getRoleName());
    }

    @Test
    void testGetRoleNotFound() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            roleService.getRole(UUID.randomUUID());
        });
    }

    @Test
    void testGetRolesByOrganization() {
        // Given
        roleService.createRole(validRoleRequest, "user-123");
        roleService.createRole(validRoleRequest, "user-124");

        // When
        List<RoleResponse> roles = roleService.getRolesByOrganization("org-123");

        // Then
        assertNotNull(roles);
        assertTrue(roles.size() >= 2);
        roles.forEach(role -> assertEquals("org-123", role.getOrganizationUuid()));
    }

    @Test
    void testGetSystemManagedRoles() {
        // When
        List<RoleResponse> systemRoles = roleService.getSystemManagedRoles();

        // Then
        assertNotNull(systemRoles);
        assertTrue(systemRoles.size() >= 4); // Owner, Manager, User, Operator
        systemRoles.forEach(role -> {
            assertEquals(RoleManagementType.SYSTEM_MANAGED, role.getRoleManagementType());
            assertNull(role.getOrganizationUuid());
        });
    }
}
