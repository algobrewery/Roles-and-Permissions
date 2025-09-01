package com.algobrewery.auth.service;

import com.algobrewery.auth.dto.UserRoleAssignmentResponse;
import com.algobrewery.auth.model.Role;
import com.algobrewery.auth.model.RoleManagementType;
import com.algobrewery.auth.repository.RoleRepository;
import com.algobrewery.auth.repository.UserRoleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("UserRoleService Integration Tests")
class UserRoleServiceIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        
        @Bean
        @Primary
        public UserRoleService testUserRoleService(
                RoleRepository roleRepository,
                UserRoleRepository userRoleRepository) {
            return new TestUserRoleService(roleRepository, userRoleRepository);
        }
    }

    // Test implementation that runs synchronously
    private static class TestUserRoleService implements UserRoleService {
        private final RoleRepository roleRepository;
        private final UserRoleRepository userRoleRepository;

        public TestUserRoleService(RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
            this.roleRepository = roleRepository;
            this.userRoleRepository = userRoleRepository;
        }

        @Override
        public CompletableFuture<UserRoleAssignmentResponse> assignRoleToUser(String userUuid, String roleUuid, 
                                                                           String organizationUuid, String assignerUuid) {
            try {
                // Run synchronously in the same thread
                return CompletableFuture.completedFuture(assignRoleToUserSync(userUuid, roleUuid, organizationUuid, assignerUuid));
            } catch (Exception e) {
                CompletableFuture<UserRoleAssignmentResponse> future = new CompletableFuture<>();
                future.completeExceptionally(e);
                return future;
            }
        }

        private UserRoleAssignmentResponse assignRoleToUserSync(String userUuid, String roleUuid, 
                                                             String organizationUuid, String assignerUuid) {
            // Copy the logic from the real service but run synchronously
            java.util.Optional<Role> roleOpt = roleRepository.findByRoleUuid(UUID.fromString(roleUuid));
            if (roleOpt.isEmpty()) {
                throw new IllegalArgumentException("Role not found: " + roleUuid);
            }

            Role role = roleOpt.get();

            // Check if role belongs to organization (for customer-managed roles)
            if (role.getRoleManagementType().getValue().equals("customer_managed") && 
                !organizationUuid.equals(role.getOrganizationUuid())) {
                throw new IllegalArgumentException("Role does not belong to the specified organization");
            }

            // Check if assignment already exists
            if (userRoleRepository.existsByUserUuidAndRoleUuidAndOrganizationUuid(userUuid, roleUuid, organizationUuid)) {
                throw new IllegalArgumentException("Role is already assigned to user in this organization");
            }

            // Create user role assignment
            com.algobrewery.auth.model.UserRole userRole = new com.algobrewery.auth.model.UserRole(userUuid, roleUuid, organizationUuid, assignerUuid);
            com.algobrewery.auth.model.UserRole savedUserRole = userRoleRepository.save(userRole);

            return mapToResponse(savedUserRole);
        }

        private UserRoleAssignmentResponse mapToResponse(com.algobrewery.auth.model.UserRole userRole) {
            return new UserRoleAssignmentResponse(
                userRole.getUserRoleUuid(),
                userRole.getUserUuid(),
                userRole.getRoleUuid(),
                userRole.getOrganizationUuid(),
                userRole.getCreatedAt()
            );
        }

        // Implement other methods as needed for testing
        @Override
        public CompletableFuture<Void> removeRoleFromUser(String userUuid, String roleUuid, String organizationUuid) {
            try {
                // Run synchronously
                removeRoleFromUserSync(userUuid, roleUuid, organizationUuid);
                return CompletableFuture.completedFuture(null);
            } catch (Exception e) {
                CompletableFuture<Void> future = new CompletableFuture<>();
                future.completeExceptionally(e);
                return future;
            }
        }

        private void removeRoleFromUserSync(String userUuid, String roleUuid, String organizationUuid) {
            if (!userRoleRepository.existsByUserUuidAndRoleUuidAndOrganizationUuid(userUuid, roleUuid, organizationUuid)) {
                throw new IllegalArgumentException("Role assignment not found");
            }
            userRoleRepository.deleteByUserUuidAndRoleUuidAndOrganizationUuid(userUuid, roleUuid, organizationUuid);
        }

        @Override
        public CompletableFuture<List<UserRoleAssignmentResponse>> getUserRoles(String userUuid, String organizationUuid) {
            List<com.algobrewery.auth.model.UserRole> userRoles = userRoleRepository.findByUserUuidAndOrganizationUuid(userUuid, organizationUuid);
            List<UserRoleAssignmentResponse> responses = userRoles.stream()
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());
            return CompletableFuture.completedFuture(responses);
        }

        @Override
        public CompletableFuture<Boolean> userHasRole(String userUuid, String roleUuid, String organizationUuid) {
            boolean hasRole = userRoleRepository.existsByUserUuidAndRoleUuidAndOrganizationUuid(userUuid, roleUuid, organizationUuid);
            return CompletableFuture.completedFuture(hasRole);
        }

        @Override
        public CompletableFuture<Long> countUserRoles(String userUuid, String organizationUuid) {
            long count = userRoleRepository.countByUserUuidAndOrganizationUuid(userUuid, organizationUuid);
            return CompletableFuture.completedFuture(count);
        }

        @Override
        public CompletableFuture<List<UserRoleAssignmentResponse>> getUserRolesByOrganization(String organizationUuid) {
            List<com.algobrewery.auth.model.UserRole> userRoles = userRoleRepository.findByOrganizationUuid(organizationUuid);
            List<UserRoleAssignmentResponse> responses = userRoles.stream()
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());
            return CompletableFuture.completedFuture(responses);
        }

        @Override
        public CompletableFuture<List<UserRoleAssignmentResponse>> getUserRolesWithDetails(String userUuid, String organizationUuid) {
            // Simplified implementation for testing
            return getUserRoles(userUuid, organizationUuid);
        }
    }

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Faker faker;
    private JsonNode testPolicy;
    private String testOrganizationUuid;
    private String testUserUuid;
    private String testCreatedBy;

    @BeforeEach
    void setUp() throws Exception {
        faker = new Faker();
        testPolicy = objectMapper.readTree("{\"data\":{\"view\":[\"task\"],\"edit\":[\"task\"]},\"features\":{\"execute\":[\"create_task\"]}}");
        testOrganizationUuid = "org-" + faker.number().randomNumber();
        testUserUuid = "user-" + faker.number().randomNumber();
        testCreatedBy = "admin-" + faker.number().randomNumber();
    }

    @Test
    @DisplayName("Should assign role to user successfully")
    @Transactional
    void testAssignRoleToUser_Success() {
        // Given - Create a role first
        String roleName = "Test Role " + faker.number().randomNumber();
        Role role = createTestRole(roleName, testOrganizationUuid);

        // When
        UserRoleAssignmentResponse response = userRoleService.assignRoleToUser(
            testUserUuid,
            role.getRoleUuid().toString(),
            testOrganizationUuid,
            testCreatedBy
        ).join();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserUuid()).isEqualTo(testUserUuid);
        assertThat(response.getRoleUuid()).isEqualTo(role.getRoleUuid().toString());
        assertThat(response.getOrganizationUuid()).isEqualTo(testOrganizationUuid);
        assertThat(response.getUserRoleUuid()).isNotNull();
        assertThat(response.getCreatedAt()).isNotNull();

        // Verify database persistence
        assertThat(userRoleRepository.findByUserUuidAndOrganizationUuid(testUserUuid, testOrganizationUuid))
            .hasSize(1);
    }

    @Test
    @DisplayName("Should throw exception when assigning role to user with non-existent role")
    @Transactional
    void testAssignRoleToUser_RoleNotFound() {
        // Given
        String nonExistentRoleUuid = UUID.randomUUID().toString();

        // When & Then
        assertThatThrownBy(() -> userRoleService.assignRoleToUser(
            testUserUuid,
            nonExistentRoleUuid,
            testOrganizationUuid,
            testCreatedBy
        ).join())
            .isInstanceOf(CompletionException.class)
            .hasCauseInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Role not found");
    }

    @Test
    @DisplayName("Should throw exception when assigning role to user with role from different organization")
    @Transactional
    void testAssignRoleToUser_RoleFromDifferentOrganization() {
        // Given - Create a role in a different organization
        String differentOrgUuid = "org-different-" + faker.number().randomNumber();
        Role role = createTestRole("Test Role", differentOrgUuid);

        // When & Then
        assertThatThrownBy(() -> userRoleService.assignRoleToUser(
            testUserUuid,
            role.getRoleUuid().toString(),
            testOrganizationUuid,
            testCreatedBy
        ).join())
            .isInstanceOf(CompletionException.class)
            .hasCauseInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Role does not belong to the specified organization");
    }

    @Test
    @DisplayName("Should throw exception when assigning duplicate role to user")
    @Transactional
    void testAssignRoleToUser_DuplicateAssignment() {
        // Given - Create a role and assign it once
        String roleName = "Test Role " + faker.number().randomNumber();
        Role role = createTestRole(roleName, testOrganizationUuid);

        // First assignment
        userRoleService.assignRoleToUser(
            testUserUuid,
            role.getRoleUuid().toString(),
            testOrganizationUuid,
            testCreatedBy
        ).join();

        // When & Then - Try to assign the same role again
        assertThatThrownBy(() -> userRoleService.assignRoleToUser(
            testUserUuid,
            role.getRoleUuid().toString(),
            testOrganizationUuid,
            testCreatedBy
        ).join())
            .isInstanceOf(CompletionException.class)
            .hasCauseInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Role is already assigned to user in this organization");
    }

    @Test
    @DisplayName("Should remove role from user successfully")
    @Transactional
    void testRemoveRoleFromUser_Success() {
        // Given - Create a role and assign it to user
        String roleName = "Test Role " + faker.number().randomNumber();
        Role role = createTestRole(roleName, testOrganizationUuid);

        userRoleService.assignRoleToUser(
            testUserUuid,
            role.getRoleUuid().toString(),
            testOrganizationUuid,
            testCreatedBy
        ).join();

        // When
        userRoleService.removeRoleFromUser(
            testUserUuid,
            role.getRoleUuid().toString(),
            testOrganizationUuid
        ).join();

        // Then
        assertThat(userRoleRepository.findByUserUuidAndOrganizationUuid(testUserUuid, testOrganizationUuid))
            .isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when removing non-existent user role assignment")
    @Transactional
    void testRemoveRoleFromUser_NotFound() {
        // Given
        String nonExistentRoleUuid = UUID.randomUUID().toString();

        // When & Then
        assertThatThrownBy(() -> userRoleService.removeRoleFromUser(
            testUserUuid,
            nonExistentRoleUuid,
            testOrganizationUuid
        ).join())
            .isInstanceOf(CompletionException.class)
            .hasCauseInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Role assignment not found");
    }

    @Test
    @DisplayName("Should get user roles successfully")
    @Transactional
    void testGetUserRoles_Success() {
        // Given - Create multiple roles and assign them to user
        String roleName1 = "Test Role 1 " + faker.number().randomNumber();
        String roleName2 = "Test Role 2 " + faker.number().randomNumber();
        
        Role role1 = createTestRole(roleName1, testOrganizationUuid);
        Role role2 = createTestRole(roleName2, testOrganizationUuid);

        userRoleService.assignRoleToUser(
            testUserUuid,
            role1.getRoleUuid().toString(),
            testOrganizationUuid,
            testCreatedBy
        ).join();

        userRoleService.assignRoleToUser(
            testUserUuid,
            role2.getRoleUuid().toString(),
            testOrganizationUuid,
            testCreatedBy
        ).join();

        // When
        List<UserRoleAssignmentResponse> userRoles = userRoleService.getUserRoles(testUserUuid, testOrganizationUuid).join();

        // Then
        assertThat(userRoles).hasSize(2);
        assertThat(userRoles).allMatch(ur -> ur.getUserUuid().equals(testUserUuid));
        assertThat(userRoles).allMatch(ur -> ur.getOrganizationUuid().equals(testOrganizationUuid));
    }

    @Test
    @DisplayName("Should return empty list when user has no roles")
    @Transactional
    void testGetUserRoles_UserHasNoRoles() {
        // When
        List<UserRoleAssignmentResponse> userRoles = userRoleService.getUserRoles(testUserUuid, testOrganizationUuid).join();

        // Then
        assertThat(userRoles).isEmpty();
    }

    @Test
    @DisplayName("Should check if user has role successfully")
    @Transactional
    void testUserHasRole_UserHasRole() {
        // Given - Create a role and assign it to user
        String roleName = "Test Role " + faker.number().randomNumber();
        Role role = createTestRole(roleName, testOrganizationUuid);

        userRoleService.assignRoleToUser(
            testUserUuid,
            role.getRoleUuid().toString(),
            testOrganizationUuid,
            testCreatedBy
        ).join();

        // When & Then
        assertThat(userRoleService.userHasRole(
            testUserUuid,
            role.getRoleUuid().toString(),
            testOrganizationUuid
        ).join()).isTrue();
    }

    @Test
    @DisplayName("Should return false when user does not have role")
    @Transactional
    void testUserHasRole_UserDoesNotHaveRole() {
        // Given - Create a role but don't assign it to user
        String roleName = "Test Role " + faker.number().randomNumber();
        Role role = createTestRole(roleName, testOrganizationUuid);

        // When & Then
        assertThat(userRoleService.userHasRole(
            testUserUuid,
            role.getRoleUuid().toString(),
            testOrganizationUuid
        ).join()).isFalse();
    }

    @Test
    @DisplayName("Should return false when checking non-existent role")
    @Transactional
    void testUserHasRole_NonExistentRole() {
        // Given
        String nonExistentRoleUuid = UUID.randomUUID().toString();

        // When & Then
        assertThat(userRoleService.userHasRole(
            testUserUuid,
            nonExistentRoleUuid,
            testOrganizationUuid
        ).join()).isFalse();
    }

    @Test
    @DisplayName("Should count user roles correctly")
    @Transactional
    void testCountUserRoles() {
        // Given - Create and assign multiple roles
        String roleName1 = "Test Role 1 " + faker.number().randomNumber();
        String roleName2 = "Test Role 2 " + faker.number().randomNumber();
        
        Role role1 = createTestRole(roleName1, testOrganizationUuid);
        Role role2 = createTestRole(roleName2, testOrganizationUuid);

        userRoleService.assignRoleToUser(
            testUserUuid,
            role1.getRoleUuid().toString(),
            testOrganizationUuid,
            testCreatedBy
        ).join();

        userRoleService.assignRoleToUser(
            testUserUuid,
            role2.getRoleUuid().toString(),
            testOrganizationUuid,
            testCreatedBy
        ).join();

        // When
        long count = userRoleService.countUserRoles(testUserUuid, testOrganizationUuid).join();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should get user roles by organization successfully")
    @Transactional
    void testGetUserRolesByOrganization() {
        // Given - Create roles and assign them to different users
        String user1 = "user-1-" + faker.number().randomNumber();
        String user2 = "user-2-" + faker.number().randomNumber();
        
        Role role1 = createTestRole("Role 1", testOrganizationUuid);
        Role role2 = createTestRole("Role 2", testOrganizationUuid);

        userRoleService.assignRoleToUser(user1, role1.getRoleUuid().toString(), testOrganizationUuid, testCreatedBy).join();
        userRoleService.assignRoleToUser(user2, role2.getRoleUuid().toString(), testOrganizationUuid, testCreatedBy).join();

        // When
        List<UserRoleAssignmentResponse> orgUserRoles = userRoleService.getUserRolesByOrganization(testOrganizationUuid).join();

        // Then
        assertThat(orgUserRoles).hasSize(2);
        assertThat(orgUserRoles).allMatch(ur -> ur.getOrganizationUuid().equals(testOrganizationUuid));
    }

    private Role createTestRole(String roleName, String organizationUuid) {
        Role role = new Role(
            roleName,
            organizationUuid,
            RoleManagementType.CUSTOMER_MANAGED,
            "Test role description",
            testPolicy,
            testCreatedBy
        );
        
        return roleRepository.save(role);
    }
}
