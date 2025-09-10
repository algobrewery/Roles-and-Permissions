package com.algobrewery.auth.controller;

import com.algobrewery.auth.dto.PermissionCheckRequest;
import com.algobrewery.auth.dto.PermissionCheckResponse;
import com.algobrewery.auth.service.PermissionService;
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

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.algobrewery.auth.dto.EndpointPermissionCheckRequest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("PermissionController Integration Tests")
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PermissionService permissionService;

    @Autowired
    private ObjectMapper objectMapper;

    private String testUserUuid;
    private String testOrganizationUuid;
    private PermissionCheckRequest testRequest;
    private PermissionCheckResponse testResponse;

    @BeforeEach
    void setUp() {
        testUserUuid = UUID.randomUUID().toString();
        testOrganizationUuid = UUID.randomUUID().toString();

        // Create test request (without user/org context - now comes from headers)
        testRequest = new PermissionCheckRequest();
        testRequest.setResource("task");
        testRequest.setAction("view");

        // Create test response
        testResponse = new PermissionCheckResponse(true, "role-uuid", "Test Role", "team");
    }

    @Test
    @DisplayName("Should check permission successfully")
    void testCheckPermission_Success() throws Exception {
        // Given
        when(permissionService.checkPermission(eq(testUserUuid), eq(testOrganizationUuid), eq(testRequest)))
            .thenReturn(CompletableFuture.completedFuture(testResponse));

        // When & Then
        mockMvc.perform(post("/permission/check")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-app-user-uuid", testUserUuid)
                .header("x-app-org-uuid", testOrganizationUuid)
                .content(objectMapper.writeValueAsString(testRequest)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.has_permission").value(true))
            .andExpect(jsonPath("$.role_uuid").value(testResponse.getRoleUuid()))
            .andExpect(jsonPath("$.role_name").value("Test Role"))
            .andExpect(jsonPath("$.granted_scope").value("team"));
    }

    @Test
    @DisplayName("Should return bad request when checking permission with invalid request")
    void testCheckPermission_InvalidRequest() throws Exception {
        // Given - Create invalid request (missing required fields)
        String invalidJson = "{}";

        // When & Then
        mockMvc.perform(post("/permission/check")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-app-user-uuid", testUserUuid)
                .header("x-app-org-uuid", testOrganizationUuid)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when missing required headers")
    void testCheckPermission_MissingHeaders() throws Exception {
        // Given - Valid request but missing headers
        String validJson = "{\"action\":\"view\",\"resource\":\"task\"}";

        // When & Then - Missing user header
        mockMvc.perform(post("/permission/check")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-app-org-uuid", testOrganizationUuid)
                .content(validJson))
            .andExpect(status().isBadRequest());

        // When & Then - Missing organization header
        mockMvc.perform(post("/permission/check")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-app-user-uuid", testUserUuid)
                .content(validJson))
            .andExpect(status().isBadRequest());

        // When & Then - Missing both headers
        mockMvc.perform(post("/permission/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when legacy endpoint receives invalid request")
    void testHasPermission_InvalidRequest() throws Exception {
        // Given - Create invalid request (missing required fields)
        String invalidJson = "{}";

        // When & Then
        mockMvc.perform(post("/has-permission")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-app-user-uuid", testUserUuid)
                .header("x-app-org-uuid", testOrganizationUuid)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when endpoint-based check receives invalid request")
    void testCheckPermissionByEndpoint_InvalidRequest() throws Exception {
        // Given - Create invalid request (missing required fields)
        String invalidJson = "{}";

        // When & Then
        mockMvc.perform(post("/check-permission")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-app-user-uuid", testUserUuid)
                .header("x-app-org-uuid", testOrganizationUuid)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle permission denied response correctly")
    void testCheckPermission_PermissionDenied() throws Exception {
        // Given - Create denied response
        PermissionCheckResponse deniedResponse = new PermissionCheckResponse(false, null, null, null);

        when(permissionService.checkPermission(eq(testRequest)))
            .thenReturn(CompletableFuture.completedFuture(deniedResponse));

        // When & Then
        mockMvc.perform(post("/permission/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.has_permission").value(false))
            .andExpect(jsonPath("$.role_uuid").isEmpty())
            .andExpect(jsonPath("$.role_name").isEmpty())
            .andExpect(jsonPath("$.granted_scope").isEmpty());
    }

    @Test
    @DisplayName("Should return internal server error when permission service throws exception")
    void testCheckPermission_ServiceThrowsException() throws Exception {
        // Given
        when(permissionService.checkPermission(eq(testRequest)))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Database error")));

        // When & Then
        mockMvc.perform(post("/permission/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should check permission via legacy endpoint successfully")
    void testHasPermission_Success() throws Exception {
        // Given
        when(permissionService.checkPermission(eq(testRequest)))
            .thenReturn(CompletableFuture.completedFuture(testResponse));

        // When & Then
        mockMvc.perform(post("/has-permission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.has_permission").value(true))
            .andExpect(jsonPath("$.role_uuid").value(testResponse.getRoleUuid()));
    }

    @Test
    @DisplayName("Should return internal server error when legacy endpoint service throws exception")
    void testHasPermission_ServiceThrowsException() throws Exception {
        // Given
        when(permissionService.checkPermission(eq(testRequest)))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Database error")));

        // When & Then
        mockMvc.perform(post("/has-permission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should check permission by endpoint successfully")
    void testCheckPermissionByEndpoint_Success() throws Exception {
        // Given
        EndpointPermissionCheckRequest endpointRequest = new EndpointPermissionCheckRequest();
        // userUuid and organizationUuid now come from headers
        endpointRequest.setEndpoint("GET /tasks");
        
        // Convert to PermissionCheckRequest for the service (matching controller logic)
        PermissionCheckRequest serviceRequest = new PermissionCheckRequest();
        // userUuid and organizationUuid now come from headers
        serviceRequest.setEndpoint(endpointRequest.getEndpoint());
        serviceRequest.setResourceId(endpointRequest.getResourceId());
        
        when(permissionService.checkPermissionByEndpoint(eq(testUserUuid), eq(testOrganizationUuid), eq(serviceRequest)))
            .thenReturn(CompletableFuture.completedFuture(testResponse));

        // When & Then
        mockMvc.perform(post("/check-permission")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-app-user-uuid", testUserUuid)
                .header("x-app-org-uuid", testOrganizationUuid)
                .content(objectMapper.writeValueAsString(endpointRequest)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.has_permission").value(true))
            .andExpect(jsonPath("$.role_uuid").value(testResponse.getRoleUuid()));
    }

    @Test
    @DisplayName("Should deny permission for unauthorized endpoint")
    void testCheckPermissionByEndpoint_Denied() throws Exception {
        // Given
        EndpointPermissionCheckRequest endpointRequest = new EndpointPermissionCheckRequest();
        // userUuid and organizationUuid now come from headers
        endpointRequest.setEndpoint("DELETE /users/123");
        
        // Convert to PermissionCheckRequest for the service (matching controller logic)
        PermissionCheckRequest serviceRequest = new PermissionCheckRequest();
        // userUuid and organizationUuid now come from headers
        serviceRequest.setEndpoint(endpointRequest.getEndpoint());
        serviceRequest.setResourceId(endpointRequest.getResourceId());
        
        PermissionCheckResponse deniedResponse = new PermissionCheckResponse(false, null, null, null);
        
        when(permissionService.checkPermissionByEndpoint(eq(testUserUuid), eq(testOrganizationUuid), eq(serviceRequest)))
            .thenReturn(CompletableFuture.completedFuture(deniedResponse));

        // When & Then
        mockMvc.perform(post("/check-permission")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-app-user-uuid", testUserUuid)
                .header("x-app-org-uuid", testOrganizationUuid)
                .content(objectMapper.writeValueAsString(endpointRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.has_permission").value(false));
    }

    @Test
    @DisplayName("Should handle unknown endpoint gracefully")
    void testCheckPermissionByEndpoint_UnknownEndpoint() throws Exception {
        // Given
        EndpointPermissionCheckRequest endpointRequest = new EndpointPermissionCheckRequest();
        // userUuid and organizationUuid now come from headers
        endpointRequest.setEndpoint("GET /unknown");
        
        // Convert to PermissionCheckRequest for the service (matching controller logic)
        PermissionCheckRequest serviceRequest = new PermissionCheckRequest();
        // userUuid and organizationUuid now come from headers
        serviceRequest.setEndpoint(endpointRequest.getEndpoint());
        serviceRequest.setResourceId(endpointRequest.getResourceId());
        
        PermissionCheckResponse deniedResponse = new PermissionCheckResponse(false, null, null, null);
        
        when(permissionService.checkPermissionByEndpoint(eq(testUserUuid), eq(testOrganizationUuid), eq(serviceRequest)))
            .thenReturn(CompletableFuture.completedFuture(deniedResponse));

        // When & Then
        mockMvc.perform(post("/check-permission")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-app-user-uuid", testUserUuid)
                .header("x-app-org-uuid", testOrganizationUuid)
                .content(objectMapper.writeValueAsString(endpointRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.has_permission").value(false));
    }

    @Test
    @DisplayName("Should return internal server error when endpoint-based check service throws exception")
    void testCheckPermissionByEndpoint_ServiceThrowsException() throws Exception {
        // Given
        EndpointPermissionCheckRequest endpointRequest = new EndpointPermissionCheckRequest();
        // userUuid and organizationUuid now come from headers
        endpointRequest.setEndpoint("GET /tasks");
        
        // Convert to PermissionCheckRequest for the service (matching controller logic)
        PermissionCheckRequest serviceRequest = new PermissionCheckRequest();
        // userUuid and organizationUuid now come from headers
        serviceRequest.setEndpoint(endpointRequest.getEndpoint());
        serviceRequest.setResourceId(endpointRequest.getResourceId());
        
        when(permissionService.checkPermissionByEndpoint(eq(testUserUuid), eq(testOrganizationUuid), eq(serviceRequest)))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Database error")));

        // When & Then
        mockMvc.perform(post("/check-permission")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-app-user-uuid", testUserUuid)
                .header("x-app-org-uuid", testOrganizationUuid)
                .content(objectMapper.writeValueAsString(endpointRequest)))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should handle different HTTP methods in endpoint-based checks")
    void testCheckPermissionByEndpoint_DifferentHttpMethods() throws Exception {
        // Given
        String[] endpoints = {"GET /tasks", "POST /tasks", "PUT /tasks/123", "DELETE /tasks/123"};
        
        for (String endpoint : endpoints) {
            EndpointPermissionCheckRequest endpointRequest = new EndpointPermissionCheckRequest();
            endpointRequest.setEndpoint(endpoint);
            
            // Convert to PermissionCheckRequest for the service (matching controller logic)
            PermissionCheckRequest serviceRequest = new PermissionCheckRequest();
            serviceRequest.setEndpoint(endpointRequest.getEndpoint());
            serviceRequest.setResourceId(endpointRequest.getResourceId());
            
            when(permissionService.checkPermissionByEndpoint(eq(testUserUuid), eq(testOrganizationUuid), eq(serviceRequest)))
                .thenReturn(CompletableFuture.completedFuture(testResponse));

            // Test different HTTP methods
            mockMvc.perform(post("/check-permission")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("x-app-user-uuid", testUserUuid)
                    .header("x-app-org-uuid", testOrganizationUuid)
                    .content(objectMapper.writeValueAsString(endpointRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.has_permission").value(true));
        }
    }

    @Test
    @DisplayName("Should handle missing resource_id gracefully")
    void testCheckPermission_MissingResourceId() throws Exception {
        // Given
        when(permissionService.checkPermission(eq(testRequest)))
            .thenReturn(CompletableFuture.completedFuture(testResponse));

        // When & Then - Request without resource_id
        mockMvc.perform(post("/permission/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.has_permission").value(true));
    }
}
