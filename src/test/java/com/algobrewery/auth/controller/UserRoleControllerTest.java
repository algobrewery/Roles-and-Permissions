package com.algobrewery.auth.controller;

import com.algobrewery.auth.dto.UserRoleAssignmentRequest;
import com.algobrewery.auth.dto.UserRoleAssignmentResponse;
import com.algobrewery.auth.service.UserRoleService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("UserRoleController Integration Tests")
class UserRoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRoleService userRoleService;

    @Autowired
    private ObjectMapper objectMapper;

    private String testUserUuid;
    private String testRoleUuid;
    private String testOrganizationUuid;
    private String testAssignerUuid;
    private UserRoleAssignmentRequest testRequest;
    private UserRoleAssignmentResponse testResponse;

    @BeforeEach
    void setUp() {
        testUserUuid = UUID.randomUUID().toString();
        testRoleUuid = UUID.randomUUID().toString();
        testOrganizationUuid = UUID.randomUUID().toString();
        testAssignerUuid = UUID.randomUUID().toString();

        // Create test request (without organization context - now comes from headers)
        testRequest = new UserRoleAssignmentRequest();
        testRequest.setRoleUuid(testRoleUuid);

        // Create test response
        testResponse = new UserRoleAssignmentResponse();
        testResponse.setUserRoleUuid(UUID.randomUUID());
        testResponse.setUserUuid(testUserUuid);
        testResponse.setRoleUuid(testRoleUuid);
        testResponse.setOrganizationUuid(testOrganizationUuid);
        testResponse.setCreatedAt(Instant.now());
    }

    @Test
    @DisplayName("Should assign role to user successfully")
    void testAssignRoleToUser_Success() throws Exception {
        // Given
        when(userRoleService.assignRoleToUser(eq(testUserUuid), eq(testRoleUuid), eq(testOrganizationUuid), eq(testAssignerUuid)))
            .thenReturn(CompletableFuture.completedFuture(testResponse));

        // When & Then
        mockMvc.perform(post("/user/{userUuid}/roles", testUserUuid)
                .header("x-app-user-uuid", testAssignerUuid)
                .header("x-app-org-uuid", testOrganizationUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.user_uuid").value(testUserUuid))
            .andExpect(jsonPath("$.role_uuid").value(testRoleUuid))
            .andExpect(jsonPath("$.organization_uuid").value(testOrganizationUuid));
    }

    @Test
    @DisplayName("Should return bad request when missing required headers")
    void testAssignRoleToUser_MissingHeaders() throws Exception {
        // Given - Valid request but missing headers
        String validJson = "{\"role_uuid\":\"" + testRoleUuid + "\"}";

        // When & Then - Missing user header
        mockMvc.perform(post("/user/{userUuid}/roles", testUserUuid)
                .header("x-app-org-uuid", testOrganizationUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
            .andExpect(status().isBadRequest());

        // When & Then - Missing organization header
        mockMvc.perform(post("/user/{userUuid}/roles", testUserUuid)
                .header("x-app-user-uuid", testAssignerUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
            .andExpect(status().isBadRequest());

        // When & Then - Missing both headers
        mockMvc.perform(post("/user/{userUuid}/roles", testUserUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when assigning role with invalid request")
    void testAssignRoleToUser_InvalidRequest() throws Exception {
        // Given - Create invalid request (missing required fields)
        String invalidJson = "{\"role_uuid\":\"" + testRoleUuid + "\"}";

        // When & Then
        mockMvc.perform(post("/user/{userUuid}/roles", testUserUuid)
                .header("x-app-user-uuid", testAssignerUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when assigning role without assigner header")
    void testAssignRoleToUser_MissingAssignerHeader() throws Exception {
        // When & Then
        mockMvc.perform(post("/user/{userUuid}/roles", testUserUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when service throws IllegalArgumentException")
    void testAssignRoleToUser_ServiceThrowsIllegalArgumentException() throws Exception {
        // Given
        when(userRoleService.assignRoleToUser(eq(testUserUuid), eq(testRoleUuid), eq(testOrganizationUuid), eq(testAssignerUuid)))
            .thenReturn(CompletableFuture.failedFuture(new IllegalArgumentException("Role not found")));

        // When & Then
        mockMvc.perform(post("/user/{userUuid}/roles", testUserUuid)
                .header("x-app-user-uuid", testAssignerUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return internal server error when service throws exception")
    void testAssignRoleToUser_ServiceThrowsException() throws Exception {
        // Given
        when(userRoleService.assignRoleToUser(eq(testUserUuid), eq(testRoleUuid), eq(testOrganizationUuid), eq(testAssignerUuid)))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Database error")));

        // When & Then
        mockMvc.perform(post("/user/{userUuid}/roles", testUserUuid)
                .header("x-app-user-uuid", testAssignerUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should remove role from user successfully")
    void testRemoveRoleFromUser_Success() throws Exception {
        // Given
        when(userRoleService.removeRoleFromUser(eq(testUserUuid), eq(testRoleUuid), eq(testOrganizationUuid)))
            .thenReturn(CompletableFuture.completedFuture(null));

        // When & Then
        mockMvc.perform(delete("/user/{userUuid}/roles/{roleUuid}", testUserUuid, testRoleUuid)
                .param("organization_uuid", testOrganizationUuid))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return bad request when removing role with missing organization parameter")
    void testRemoveRoleFromUser_MissingOrganizationParameter() throws Exception {
        // When & Then
        mockMvc.perform(delete("/user/{userUuid}/roles/{roleUuid}", testUserUuid, testRoleUuid))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when removing non-existent role assignment")
    void testRemoveRoleFromUser_RoleAssignmentNotFound() throws Exception {
        // Given
        when(userRoleService.removeRoleFromUser(eq(testUserUuid), eq(testRoleUuid), eq(testOrganizationUuid)))
            .thenReturn(CompletableFuture.failedFuture(new IllegalArgumentException("Role assignment not found")));

        // When & Then
        mockMvc.perform(delete("/user/{userUuid}/roles/{roleUuid}", testUserUuid, testRoleUuid)
                .param("organization_uuid", testOrganizationUuid))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return internal server error when removing role throws exception")
    void testRemoveRoleFromUser_ServiceThrowsException() throws Exception {
        // Given
        when(userRoleService.removeRoleFromUser(eq(testUserUuid), eq(testRoleUuid), eq(testOrganizationUuid)))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Database error")));

        // When & Then
        mockMvc.perform(delete("/user/{userUuid}/roles/{roleUuid}", testUserUuid, testRoleUuid)
                .param("organization_uuid", testOrganizationUuid))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should get user roles successfully")
    void testGetUserRoles_Success() throws Exception {
        // Given
        List<UserRoleAssignmentResponse> userRoles = List.of(testResponse);
        when(userRoleService.getUserRoles(eq(testUserUuid), eq(testOrganizationUuid)))
            .thenReturn(CompletableFuture.completedFuture(userRoles));

        // When & Then
        mockMvc.perform(get("/user/{userUuid}/roles", testUserUuid)
                .param("organization_uuid", testOrganizationUuid))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].user_uuid").value(testUserUuid))
            .andExpect(jsonPath("$[0].role_uuid").value(testRoleUuid))
            .andExpect(jsonPath("$[0].organization_uuid").value(testOrganizationUuid));
    }

    @Test
    @DisplayName("Should return empty list when user has no roles")
    void testGetUserRoles_NoRoles() throws Exception {
        // Given
        when(userRoleService.getUserRoles(eq(testUserUuid), eq(testOrganizationUuid)))
            .thenReturn(CompletableFuture.completedFuture(List.of()));

        // When & Then
        mockMvc.perform(get("/user/{userUuid}/roles", testUserUuid)
                .param("organization_uuid", testOrganizationUuid))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Should return bad request when getting user roles with missing organization parameter")
    void testGetUserRoles_MissingOrganizationParameter() throws Exception {
        // When & Then
        mockMvc.perform(get("/user/{userUuid}/roles", testUserUuid))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return internal server error when getting user roles throws exception")
    void testGetUserRoles_ServiceThrowsException() throws Exception {
        // Given
        when(userRoleService.getUserRoles(eq(testUserUuid), eq(testOrganizationUuid)))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Database error")));

        // When & Then
        mockMvc.perform(get("/user/{userUuid}/roles", testUserUuid)
                .param("organization_uuid", testOrganizationUuid))
            .andExpect(status().isInternalServerError());
    }
}
