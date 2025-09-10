package com.algobrewery.auth.controller;

import com.algobrewery.auth.dto.RoleRequest;
import com.algobrewery.auth.dto.RoleResponse;
import com.algobrewery.auth.model.RoleManagementType;
import com.algobrewery.auth.service.RoleService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("RoleController Integration Tests")
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService roleService;

    @Autowired
    private ObjectMapper objectMapper;

    private String testRoleUuid;
    private String testOrganizationUuid;
    private String testUserUuid;
    private RoleRequest testRoleRequest;
    private RoleResponse testRoleResponse;
    private JsonNode testPolicy;

    @BeforeEach
    void setUp() throws Exception {
        testRoleUuid = UUID.randomUUID().toString();
        testOrganizationUuid = UUID.randomUUID().toString();
        testUserUuid = UUID.randomUUID().toString();

        // Create test policy
        String policyJson = "{\"data\":{\"view\":[\"task\"],\"edit\":[\"task\"]},\"features\":{\"execute\":[\"create_task\"]}}";
        testPolicy = new ObjectMapper().readTree(policyJson);

        // Create test role request (organization UUID will be set from headers in controller)
        testRoleRequest = new RoleRequest();
        testRoleRequest.setRoleName("Test Role");
        testRoleRequest.setRoleManagementType(RoleManagementType.CUSTOMER_MANAGED);
        testRoleRequest.setDescription("Test role description");
        testRoleRequest.setPolicy(testPolicy);

        // Create test role response
        testRoleResponse = new RoleResponse();
        testRoleResponse.setRoleUuid(UUID.fromString(testRoleUuid));
        testRoleResponse.setRoleName("Test Role");
        testRoleResponse.setOrganizationUuid(testOrganizationUuid);
        testRoleResponse.setRoleManagementType(RoleManagementType.CUSTOMER_MANAGED);
        testRoleResponse.setDescription("Test role description");
        testRoleResponse.setPolicy(testPolicy);
        testRoleResponse.setCreatedAt(Instant.now());
        testRoleResponse.setUpdatedAt(Instant.now());
    }

    @Test
    @DisplayName("Should create role successfully")
    void testCreateRole_Success() throws Exception {
        // Given
        when(roleService.createRole(any(RoleRequest.class), eq(testUserUuid)))
            .thenReturn(CompletableFuture.completedFuture(testRoleResponse));

        // When & Then
        mockMvc.perform(post("/role")
                .header("x-app-user-uuid", testUserUuid)
                .header("x-app-org-uuid", testOrganizationUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRoleRequest)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.role_uuid").value(testRoleUuid.toString()))
            .andExpect(jsonPath("$.role_name").value("Test Role"))
            .andExpect(jsonPath("$.organization_uuid").value(testOrganizationUuid))
            .andExpect(jsonPath("$.role_management_type").value("CUSTOMER_MANAGED"));
    }

    @Test
    @DisplayName("Should return bad request when creating role without required headers")
    void testCreateRole_MissingHeaders() throws Exception {
        // When & Then - Missing user header
        mockMvc.perform(post("/role")
                .header("x-app-org-uuid", testOrganizationUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRoleRequest)))
            .andExpect(status().isBadRequest());

        // When & Then - Missing organization header
        mockMvc.perform(post("/role")
                .header("x-app-user-uuid", testUserUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRoleRequest)))
            .andExpect(status().isBadRequest());

        // When & Then - Missing both headers
        mockMvc.perform(post("/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRoleRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when creating role with invalid request")
    void testCreateRole_InvalidRequest() throws Exception {
        // Given - Create invalid request
        RoleRequest invalidRequest = new RoleRequest();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/role")
                .header("x-app-user-uuid", testUserUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when service throws IllegalArgumentException")
    void testCreateRole_ServiceThrowsIllegalArgumentException() throws Exception {
        // Given
        when(roleService.createRole(any(RoleRequest.class), eq(testUserUuid)))
            .thenReturn(CompletableFuture.failedFuture(new IllegalArgumentException("Invalid role data")));

        // When & Then
        mockMvc.perform(post("/role")
                .header("x-app-user-uuid", testUserUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRoleRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return internal server error when service throws exception")
    void testCreateRole_ServiceThrowsException() throws Exception {
        // Given
        when(roleService.createRole(any(RoleRequest.class), eq(testUserUuid)))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Database error")));

        // When & Then
        mockMvc.perform(post("/role")
                .header("x-app-user-uuid", testUserUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRoleRequest)))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should update role successfully")
    void testUpdateRole_Success() throws Exception {
        // Given
        when(roleService.updateRole(eq(UUID.fromString(testRoleUuid)), any(RoleRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(testRoleResponse));

        // When & Then
        mockMvc.perform(put("/role/{roleUuid}", testRoleUuid)
                .header("x-app-user-uuid", testUserUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRoleRequest)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.role_uuid").value(testRoleUuid))
            .andExpect(jsonPath("$.role_name").value("Test Role"));
    }

    @Test
    @DisplayName("Should return bad request when updating role with invalid request")
    void testUpdateRole_InvalidRequest() throws Exception {
        // Given - Create invalid request
        RoleRequest invalidRequest = new RoleRequest();
        // Missing required fields

        // When & Then
        mockMvc.perform(put("/role/{roleUuid}", testRoleUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when updating non-existent role")
    void testUpdateRole_RoleNotFound() throws Exception {
        // Given
        when(roleService.updateRole(eq(UUID.fromString(testRoleUuid)), any(RoleRequest.class)))
            .thenReturn(CompletableFuture.failedFuture(new IllegalArgumentException("Role not found")));

        // When & Then
        mockMvc.perform(put("/role/{roleUuid}", testRoleUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRoleRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should delete role successfully")
    void testDeleteRole_Success() throws Exception {
        // Given
        when(roleService.deleteRole(UUID.fromString(testRoleUuid)))
            .thenReturn(CompletableFuture.completedFuture(null));

        // When & Then
        mockMvc.perform(delete("/role/{roleUuid}", testRoleUuid)
                .header("x-app-user-uuid", testUserUuid))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return bad request when deleting non-existent role")
    void testDeleteRole_RoleNotFound() throws Exception {
        // Given
        when(roleService.deleteRole(UUID.fromString(testRoleUuid)))
            .thenReturn(CompletableFuture.failedFuture(new IllegalArgumentException("Role not found")));

        // When & Then
        mockMvc.perform(delete("/role/{roleUuid}", testRoleUuid)
                .header("x-app-user-uuid", testUserUuid))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get role by UUID successfully")
    void testGetRole_Success() throws Exception {
        // Given
        when(roleService.getRole(UUID.fromString(testRoleUuid)))
            .thenReturn(CompletableFuture.completedFuture(testRoleResponse));

        // When & Then
        mockMvc.perform(get("/role/{roleUuid}", testRoleUuid))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.role_uuid").value(testRoleUuid))
            .andExpect(jsonPath("$.role_name").value("Test Role"));
    }

    @Test
    @DisplayName("Should return bad request when getting non-existent role")
    void testGetRole_RoleNotFound() throws Exception {
        // Given
        when(roleService.getRole(UUID.fromString(testRoleUuid)))
            .thenReturn(CompletableFuture.failedFuture(new IllegalArgumentException("Role not found")));

        // When & Then
        mockMvc.perform(get("/role/{roleUuid}", testRoleUuid))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get roles by organization successfully")
    void testGetRolesByOrganization_Success() throws Exception {
        // Given
        List<RoleResponse> roles = List.of(testRoleResponse);
        when(roleService.getRolesByOrganization(testOrganizationUuid))
            .thenReturn(CompletableFuture.completedFuture(roles));

        // When & Then
        mockMvc.perform(get("/role/organization/{organizationUuid}", testOrganizationUuid))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].role_uuid").value(testRoleUuid.toString()))
            .andExpect(jsonPath("$[0].role_name").value("Test Role"));
    }

    @Test
    @DisplayName("Should return empty list when organization has no roles")
    void testGetRolesByOrganization_NoRoles() throws Exception {
        // Given
        when(roleService.getRolesByOrganization(testOrganizationUuid))
            .thenReturn(CompletableFuture.completedFuture(List.of()));

        // When & Then
        mockMvc.perform(get("/role/organization/{organizationUuid}", testOrganizationUuid))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Should get system-managed roles successfully")
    void testGetSystemManagedRoles_Success() throws Exception {
        // Given
        List<RoleResponse> systemRoles = List.of(testRoleResponse);
        when(roleService.getSystemManagedRoles())
            .thenReturn(CompletableFuture.completedFuture(systemRoles));

        // When & Then
        mockMvc.perform(get("/role/system-managed"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].role_uuid").value(testRoleUuid.toString()))
            .andExpect(jsonPath("$[0].role_name").value("Test Role"));
    }

    @Test
    @DisplayName("Should return internal server error when service throws exception")
    void testGetSystemManagedRoles_ServiceThrowsException() throws Exception {
        // Given
        when(roleService.getSystemManagedRoles())
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Database error")));

        // When & Then
        mockMvc.perform(get("/role/system-managed"))
            .andExpect(status().isInternalServerError());
    }
}
