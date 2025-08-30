package com.algobrewery.auth.service;

import com.algobrewery.auth.dto.RoleRequest;
import com.algobrewery.auth.dto.RoleResponse;
import com.algobrewery.auth.model.Role;
import com.algobrewery.auth.model.RoleManagementType;
import com.algobrewery.auth.repository.RoleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService Unit Tests")
class RoleServiceUnitTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RoleService roleService;

    private RoleRequest validRoleRequest;
    private Role mockRole;
    private UUID roleUuid;
    private JsonNode mockPolicy;

    @BeforeEach
    void setUp() throws Exception {
        roleUuid = UUID.randomUUID();
        
        // Create mock policy
        mockPolicy = new ObjectMapper().readTree("{\"data\":{\"view\":[\"task\"],\"edit\":[\"task\"]},\"features\":{\"execute\":[\"create_task\"]}}");
        
        // Create valid role request
        validRoleRequest = new RoleRequest(
            "Test Role",
            "Test role description",
            "org-123",
            RoleManagementType.CUSTOMER_MANAGED,
            mockPolicy
        );

        // Create mock role
        mockRole = new Role(
            "Test Role",
            "org-123",
            RoleManagementType.CUSTOMER_MANAGED,
            "Test role description",
            mockPolicy,
            "user-123"
        );
        mockRole.setRoleUuid(roleUuid);
        mockRole.setCreatedAt(Instant.now());
        mockRole.setUpdatedAt(Instant.now());
    }

    @Test
    @DisplayName("Should create role successfully")
    void testCreateRole_Success() {
        // Given
        when(roleRepository.existsByRoleNameAndOrganizationUuid(anyString(), anyString()))
            .thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(mockRole);

        // When
        RoleResponse response = roleService.createRole(validRoleRequest, "user-123");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRoleUuid()).isEqualTo(roleUuid);
        assertThat(response.getRoleName()).isEqualTo("Test Role");
        assertThat(response.getOrganizationUuid()).isEqualTo("org-123");
        assertThat(response.getRoleManagementType()).isEqualTo(RoleManagementType.CUSTOMER_MANAGED);
        assertThat(response.getDescription()).isEqualTo("Test role description");

        verify(roleRepository).existsByRoleNameAndOrganizationUuid("Test Role", "org-123");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Should throw exception when role name already exists in organization")
    void testCreateRole_RoleNameAlreadyExists() {
        // Given
        when(roleRepository.existsByRoleNameAndOrganizationUuid(anyString(), anyString()))
            .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> roleService.createRole(validRoleRequest, "user-123"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Role name already exists in this organization");

        verify(roleRepository).existsByRoleNameAndOrganizationUuid("Test Role", "org-123");
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("Should throw exception when policy is null")
    void testCreateRole_NullPolicy() {
        // Given
        RoleRequest invalidRequest = new RoleRequest(
            "Test Role",
            "Test role description",
            "org-123",
            RoleManagementType.CUSTOMER_MANAGED,
            null
        );

        // When & Then
        assertThatThrownBy(() -> roleService.createRole(invalidRequest, "user-123"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Policy cannot be null");
    }

    @Test
    @DisplayName("Should update role successfully")
    void testUpdateRole_Success() {
        // Given
        RoleRequest updateRequest = new RoleRequest(
            "Updated Role",
            "Updated description",
            "org-123",
            RoleManagementType.CUSTOMER_MANAGED,
            mockPolicy
        );

        when(roleRepository.findByRoleUuid(roleUuid)).thenReturn(Optional.of(mockRole));
        when(roleRepository.save(any(Role.class))).thenReturn(mockRole);

        // When
        RoleResponse response = roleService.updateRole(roleUuid, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRoleUuid()).isEqualTo(roleUuid);

        verify(roleRepository).findByRoleUuid(roleUuid);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent role")
    void testUpdateRole_RoleNotFound() {
        // Given
        when(roleRepository.findByRoleUuid(roleUuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roleService.updateRole(roleUuid, validRoleRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Role not found: " + roleUuid);

        verify(roleRepository).findByRoleUuid(roleUuid);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("Should delete role successfully")
    void testDeleteRole_Success() {
        // Given
        when(roleRepository.findByRoleUuid(roleUuid)).thenReturn(Optional.of(mockRole));
        doNothing().when(roleRepository).delete(any(Role.class));

        // When
        roleService.deleteRole(roleUuid);

        // Then
        verify(roleRepository).findByRoleUuid(roleUuid);
        verify(roleRepository).delete(mockRole);
    }

    @Test
    @DisplayName("Should throw exception when deleting system-managed role")
    void testDeleteRole_SystemManagedRole() {
        // Given
        Role systemRole = new Role(
            "System Role",
            null,
            RoleManagementType.SYSTEM_MANAGED,
            "System role",
            mockPolicy,
            "system"
        );
        systemRole.setRoleUuid(roleUuid);

        when(roleRepository.findByRoleUuid(roleUuid)).thenReturn(Optional.of(systemRole));

        // When & Then
        assertThatThrownBy(() -> roleService.deleteRole(roleUuid))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cannot delete system-managed role");

        verify(roleRepository).findByRoleUuid(roleUuid);
        verify(roleRepository, never()).delete(any(Role.class));
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent role")
    void testDeleteRole_RoleNotFound() {
        // Given
        when(roleRepository.findByRoleUuid(roleUuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roleService.deleteRole(roleUuid))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Role not found: " + roleUuid);

        verify(roleRepository).findByRoleUuid(roleUuid);
        verify(roleRepository, never()).delete(any(Role.class));
    }

    @Test
    @DisplayName("Should get role by UUID successfully")
    void testGetRole_Success() {
        // Given
        when(roleRepository.findByRoleUuid(roleUuid)).thenReturn(Optional.of(mockRole));

        // When
        RoleResponse response = roleService.getRole(roleUuid);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRoleUuid()).isEqualTo(roleUuid);
        assertThat(response.getRoleName()).isEqualTo("Test Role");

        verify(roleRepository).findByRoleUuid(roleUuid);
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent role")
    void testGetRole_RoleNotFound() {
        // Given
        when(roleRepository.findByRoleUuid(roleUuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roleService.getRole(roleUuid))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Role not found: " + roleUuid);

        verify(roleRepository).findByRoleUuid(roleUuid);
    }

    @Test
    @DisplayName("Should get roles by organization successfully")
    void testGetRolesByOrganization_Success() {
        // Given
        List<Role> roles = Arrays.asList(mockRole);
        when(roleRepository.findByOrganizationUuid("org-123")).thenReturn(roles);

        // When
        List<RoleResponse> responses = roleService.getRolesByOrganization("org-123");

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getRoleUuid()).isEqualTo(roleUuid);
        assertThat(responses.get(0).getOrganizationUuid()).isEqualTo("org-123");

        verify(roleRepository).findByOrganizationUuid("org-123");
    }

    @Test
    @DisplayName("Should get system-managed roles successfully")
    void testGetSystemManagedRoles_Success() {
        // Given
        Role systemRole = new Role(
            "System Role",
            null,
            RoleManagementType.SYSTEM_MANAGED,
            "System role",
            mockPolicy,
            "system"
        );
        systemRole.setRoleUuid(roleUuid);

        List<Role> roles = Arrays.asList(systemRole);
        when(roleRepository.findSystemManagedRoles(RoleManagementType.SYSTEM_MANAGED))
            .thenReturn(roles);

        // When
        List<RoleResponse> responses = roleService.getSystemManagedRoles();

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getRoleUuid()).isEqualTo(roleUuid);
        assertThat(responses.get(0).getRoleManagementType()).isEqualTo(RoleManagementType.SYSTEM_MANAGED);

        verify(roleRepository).findSystemManagedRoles(RoleManagementType.SYSTEM_MANAGED);
    }

    @Test
    @DisplayName("Should get role by name and organization successfully")
    void testGetRoleByNameAndOrganization_Success() {
        // Given
        when(roleRepository.findByRoleNameAndOrganizationUuid("Test Role", "org-123"))
            .thenReturn(Optional.of(mockRole));

        // When
        Optional<RoleResponse> response = roleService.getRoleByNameAndOrganization("Test Role", "org-123");

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().getRoleUuid()).isEqualTo(roleUuid);
        assertThat(response.get().getRoleName()).isEqualTo("Test Role");

        verify(roleRepository).findByRoleNameAndOrganizationUuid("Test Role", "org-123");
    }

    @Test
    @DisplayName("Should return empty when role by name and organization not found")
    void testGetRoleByNameAndOrganization_NotFound() {
        // Given
        when(roleRepository.findByRoleNameAndOrganizationUuid("NonExistent", "org-123"))
            .thenReturn(Optional.empty());

        // When
        Optional<RoleResponse> response = roleService.getRoleByNameAndOrganization("NonExistent", "org-123");

        // Then
        assertThat(response).isEmpty();

        verify(roleRepository).findByRoleNameAndOrganizationUuid("NonExistent", "org-123");
    }
}
