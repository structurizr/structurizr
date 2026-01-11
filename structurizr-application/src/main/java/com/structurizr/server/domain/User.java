package com.structurizr.server.domain;

import com.structurizr.configuration.Configuration;
import com.structurizr.util.DateUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a Structurizr user.
 */
public final class User {

    private final String username;
    private Set<String> roles = new HashSet<>();
    private AuthenticationMethod authenticationMethod = AuthenticationMethod.LOCAL;

    public User(String username) {
        this(username, Set.of(), AuthenticationMethod.LOCAL);
    }

    public User(String username, Set<String> roles, AuthenticationMethod authenticationMethod) {
        this.username = username;

        if (authenticationMethod == null) {
            throw new IllegalArgumentException("Authentication method cannot be null");
        }
        this.authenticationMethod = authenticationMethod;

        setRoles(roles);
    }

    public boolean isAuthenticated() {
        return authenticationMethod != AuthenticationMethod.NONE;
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getRoles() {
        return new HashSet<>(roles);
    }

    private void setRoles(Set<String> roles) {
        if (roles != null) {
            this.roles.addAll(roles);
        } else {
            this.roles = new HashSet<>();
        }
    }

    public AuthenticationMethod getAuthenticationMethod() {
        return authenticationMethod;
    }

    public boolean isUserOrRole(Set<String> usersAndRoles) {
        if (usersAndRoles == null) {
            usersAndRoles = new HashSet<>();
        }

        // 1. case-insensitive match on username
        if (usersAndRoles.contains(username.toLowerCase())) {
            return true;
        }

        // 2. regex match on username
        for (String userOrRole : usersAndRoles) {
            if (userOrRole.startsWith("^") && userOrRole.endsWith("$") && username.toLowerCase().matches(userOrRole)) {
                return true;
            }
        }

        // 3. case-insensitive match on role
        for (String role : roles) {
            if (usersAndRoles.contains(role.toLowerCase())) {
                return true;
            }
        }

        // 4. regex match on role
        for (String role : roles) {
            for (String userOrRole : usersAndRoles) {
                if (userOrRole.startsWith("^") && userOrRole.endsWith("$") && role.toLowerCase().matches(userOrRole)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isAdmin() {
        if (Configuration.getInstance().adminUsersEnabled()) {
            return isUserOrRole(Configuration.getInstance().getAdminUsersAndRoles());
        } else {
            return false;
        }
    }

    public String getTimeZone() {
        return DateUtils.UTC_TIME_ZONE;
    }

    public String getName() {
        return getUsername();
    }

}