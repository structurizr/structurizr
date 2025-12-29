package com.structurizr.server.domain;

import com.structurizr.configuration.Configuration;

import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

/**
 * Represents a Structurizr user.
 */
public final class User {

    private final String username;
    private Set<String> roles = new HashSet<>();
    private AuthenticationMethod authenticationMethod = AuthenticationMethod.LOCAL;

    public User(String username, Set<String> roles, AuthenticationMethod authenticationMethod) {
        this.username = username;

        setRoles(roles);
        setAuthenticationMethod(authenticationMethod);
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

    private void setAuthenticationMethod(AuthenticationMethod authenticationMethod) {
        if (authenticationMethod != null) {
            this.authenticationMethod = authenticationMethod;
        } else {
            this.authenticationMethod = AuthenticationMethod.LOCAL;
        }
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
        return isUserOrRole(Configuration.getInstance().getAdminUsersAndRoles());
    }

    public String getTimeZone() {
        return TimeZone.getDefault().getID();
    }

    public String getName() {
        return getUsername();
    }

}