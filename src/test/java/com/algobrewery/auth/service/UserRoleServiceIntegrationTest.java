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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("integration-test")
@TestPropertySource(locations = "classpath:application-integration-test.yml")
@Transactional
@DisplayName("UserRoleService Integration Tests")
class UserRoleServiceIntegrationTest {

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
        );

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
    void testAssignRoleToUser_RoleNotFound() {
        // Given
        String nonExistentRoleUuid = UUID.randomUUID().toString();

        // When & Then
        assertThatThrownBy(() -> userRoleService.assignRoleToUser(
            testUserUuid,
            nonExistentRoleUuid,
            testOrganizationUuid,
            testCreatedBy
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Role not found");
    }

    @Test
    @DisplayName("Should throw exception when assigning role to user with role from different organization")
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
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Role does not belong to the specified organization");
    }

    @Test
    @DisplayName("Should throw exception when assigning duplicate role to user")
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
        );

        // When & Then - Try to assign the same role again
        assertThatThrownBy(() -> userRoleService.assignRoleToUser(
            testUserUuid,
            role.getRoleUuid().toString(),
            testOrganizationUuid,
            testCreatedBy
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Role is already assigned to user in this organization");
    }

    @Test
    @DisplayName("Should remove role from user successfully")
    void testRemoveRoleFromUser_Success() {
        // Given - Create a role and assign it to user
        String roleName = "Test Role " + faker.number().randomNumber();
        Role role = createTestRole(roleName, testOrganizationUuid);

        userRoleService.assignRoleToUser(
            testUserUuid,
            role.getRoleUuid().toString(),
            testOrganizationUuid,
            testCreatedBy
        );

        // When
        userRoleService.removeRoleFromUser(
            testUserUuid,
            role.getRoleUuid().toString(),
            testOrganizationUuid
        );

        // Then
        assertThat(userRoleRepository.findByUserUuidAndOrganizationUuid(testUserUuid, testOrganizationUuid))
            .isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when removing non-existent user role assignment")
    void testRemoveRoleFromUser_NotFound() {
        // Given
        String nonExistentRoleUuid = UUID.randomUUID().toString();

        // When & Then
        assertThatThrownBy(() -> userRoleService.removeRoleFromUser(
            testUserUuid,
            nonExistentRoleUuid,
            testOrganizationUuid
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Role assignment not found");
    }

    @Test
    @DisplayName("Should get user roles successfully")
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
        );

        userRoleService.assignRoleToUser(
            testUserUuid,
            role2.getRoleUuid().toString(),
            testOrganizationUuid,
            testCreatedBy
        );

        // When
        List<UserRoleAssignmentResponse> userRoles = userRoleService.getUserRoles(testUserUuid, testOrganizationUuid);

        // Then
        assertThat(userRoles).hasSize(2);
        assertThat(userRoles).allMatch(ur -> ur.getUserUuid().equals(testUserUuid));
        assertThat(userRoles).allMatch(ur -> ur.getOrganizationUuid().equals(testOrganizationUuid));
    }

    @Test
    @DisplayName("Should return empty list when user has no roles")
    void testGetUserRoles_NoRoles() {
        // When
        List<UserRoleAssignmentResponse> userRoles = userRoleService.getUserRoles(testUserUuid, testOrganizationUuid);

        // Then
        assertThat(userRoles).isEmpty();
    }

    @Test
    @DisplayName("Should check if user has role successfully")
    void testUserHasRole_Success() {
        // Given - Create a role and assign it to user
        String roleName = "Test Role " + faker.number().randomNumber();
        Role role = createTestRole(roleName, testOrganizationUuid);

        userRoleService.assignRoleToUser(
            testUserUuid,
            role.getRoleUuid().toString(),
            testOrganizationUuid,
            testCreatedBy
        );

        // When & Then
        assertThat(userRoleService.userHasRole(
            testUserUuid,
            role.getRoleUuid().toString(),
            testOrganizationUuid
        )).isTrue();
    }

    @Test
    @DisplayName("Should return false when user does not have role")
    void testUserHasRole_UserDoesNotHaveRole() {
        // Given - Create a role but don't assign it to user
        String roleName = "Test Role " + faker.number().randomNumber();
        Role role = createTestRole(roleName, testOrganizationUuid);

        // When & Then
        assertThat(userRoleService.userHasRole(
            testUserUuid,
            role.getRoleUuid().toString(),
            testOrganizationUuid
        )).isFalse();
    }

    @Test
    @DisplayName("Should return false when checking non-existent role")
    void testUserHasRole_NonExistentRole() {
        // Given
        String nonExistentRoleUuid = UUID.randomUUID().toString();

        // When & Then
        assertThat(userRoleService.userHasRole(
            testUserUuid,
            nonExistentRoleUuid,
            testOrganizationUuid
        )).isFalse();
    }

    @Test
    @DisplayName("Should count user roles correctly")
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
        );

        userRoleService.assignRoleToUser(
            testUserUuid,
            role2.getRoleUuid().toString(),
            testOrganizationUuid,
            testCreatedBy
        );

        // When
        long count = userRoleService.countUserRoles(testUserUuid, testOrganizationUuid);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should get user roles by organization successfully")
    void testGetUserRolesByOrganization() {
        // Given - Create roles and assign them to different users
        String user1 = "user-1-" + faker.number().randomNumber();
        String user2 = "user-2-" + faker.number().randomNumber();
        
        Role role1 = createTestRole("Role 1", testOrganizationUuid);
        Role role2 = createTestRole("Role 2", testOrganizationUuid);

        userRoleService.assignRoleToUser(user1, role1.getRoleUuid().toString(), testOrganizationUuid, testCreatedBy);
        userRoleService.assignRoleToUser(user2, role2.getRoleUuid().toString(), testOrganizationUuid, testCreatedBy);

        // When
        List<UserRoleAssignmentResponse> orgUserRoles = userRoleService.getUserRolesByOrganization(testOrganizationUuid);

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
