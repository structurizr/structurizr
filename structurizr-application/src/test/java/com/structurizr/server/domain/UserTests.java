package com.structurizr.server.domain;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import com.structurizr.configuration.StructurizrProperties;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserTests {

    private User user;

    @Test
    void construction_WithoutRoles() {
        user = new User("user@example.com", new HashSet<>(), AuthenticationMethod.LOCAL);

        assertEquals("user@example.com", user.getUsername());
        assertEquals(AuthenticationMethod.LOCAL, user.getAuthenticationMethod());

        assertEquals(0, user.getRoles().size());
    }

    @Test
    void construction_WithRoles() {
        Set<String> roles = new HashSet<>();
        roles.add("role1");
        roles.add("role2");
        roles.add("role3");
        user = new User("user@example.com", roles, AuthenticationMethod.LOCAL);

        assertEquals("user@example.com", user.getUsername());
        assertEquals(AuthenticationMethod.LOCAL, user.getAuthenticationMethod());

        assertEquals(3, user.getRoles().size());
        assertTrue(user.getRoles().contains("role1"));
        assertTrue(user.getRoles().contains("role2"));
        assertTrue(user.getRoles().contains("role3"));
    }

    @Test
    void isUserOrRole_ReturnsFalse_WhenTheListOfUsersAndRolesIsEmpty() {
        user = new User("user@example.com", new HashSet<>(), AuthenticationMethod.LOCAL);

        assertFalse(user.isUserOrRole(Collections.emptySet()));
    }

    @Test
    void isUserOrRole_ReturnsFalse_WhenTheListOfUsersAndRolesIsNull() {
        user = new User("user@example.com", new HashSet<>(), AuthenticationMethod.LOCAL);

        assertFalse(user.isUserOrRole(null));
    }

    @Test
    void isUserOrRole_ReturnsFalse_WhenUsernameIsNotInTheListOfUsersAndRoles() {
        user = new User("user@example.com", new HashSet<>(), AuthenticationMethod.LOCAL);

        // 1. case-insensitive match on username - false
        assertFalse(user.isUserOrRole(Collections.singleton("user@google.com")));
    }

    @Test
    void isUserOrRole_ReturnsTrue_WhenUsernameIsInTheListOfUsersAndRoles() {
        user = new User("user@example.com", new HashSet<>(), AuthenticationMethod.LOCAL);

        // 1. case-insensitive match on username - true
        assertTrue(user.isUserOrRole(Collections.singleton("user@example.com")));
    }

    @Test
    void isUserOrRole_ReturnsFalse_WhenUsernameDoesNotMatchARegex() {
        user = new User("user@example.com", new HashSet<>(), AuthenticationMethod.LOCAL);

        // 2. regex match on username - false
        assertFalse(user.isUserOrRole(Collections.singleton("^.*@google.com$")));
    }

    @Test
    void isUserOrRole_ReturnsTrue_WhenUsernameMatchesARegex() {
        user = new User("user@example.com", new HashSet<>(), AuthenticationMethod.LOCAL);

        // 2. regex match on username - true
        assertTrue(user.isUserOrRole(Collections.singleton("^.*@example.com$")));
    }

    @Test
    void isUserOrRole_ReturnsFalse_WhenARoleIsNotInTheListOfUsersAndRoles() {
        Set<String> roles = new HashSet<>();
        roles.add("role1");
        roles.add("role2");
        roles.add("role3");
        user = new User("user@example.com", roles, AuthenticationMethod.LOCAL);

        // 3. case-insensitive match on role - false
        assertFalse(user.isUserOrRole(Collections.singleton("role4")));
    }

    @Test
    void isUserOrRole_ReturnsTrue_WhenARoleIsInTheListOfUsersAndRoles() {
        Set<String> roles = new HashSet<>();
        roles.add("role1");
        roles.add("role2");
        roles.add("role3");
        user = new User("user@example.com", roles, AuthenticationMethod.LOCAL);

        // 3. case-insensitive match on role - true
        assertTrue(user.isUserOrRole(Collections.singleton("role2")));
    }

    @Test
    void isUserOrRole_ReturnsFalse_WhenRoleDoesNotMatchARegex() {
        Set<String> roles = new HashSet<>();
        roles.add("group2-subgroup1");
        user = new User("user@example.com", roles, AuthenticationMethod.LOCAL);

        // 4. regex match on role - false
        assertFalse(user.isUserOrRole(Collections.singleton("^group1-.*$")));
    }

    @Test
    void isUserOrRole_ReturnsTrue_WhenRoleMatchesARegex() {
        Set<String> roles = new HashSet<>();
        roles.add("group1-subgroup1");
        user = new User("user@example.com", roles, AuthenticationMethod.LOCAL);

        // 4. regex match on role - true
        assertTrue(user.isUserOrRole(Collections.singleton("^group1-.*$")));
    }

    @Test
    void isAdmin() {
        Set<String> roles = new HashSet<>();
        roles.add("role1");
        user = new User("user@example.com", roles, AuthenticationMethod.LOCAL);

        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "");
        Configuration.init(Profile.Server, properties);
        assertFalse(user.isAdmin());

        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "user@google.com");
        Configuration.init(Profile.Server, properties);
        assertFalse(user.isAdmin()); // not a named user

        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "user@example.com");
        Configuration.init(Profile.Server, properties);
        assertTrue(user.isAdmin()); // a named user

        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "role1");
        Configuration.init(Profile.Server, properties);
        assertTrue(user.isAdmin()); // a named role

        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "role2");
        Configuration.init(Profile.Server, properties);
        assertFalse(user.isAdmin()); // not a named role
    }

}