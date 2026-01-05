package com.structurizr.server.web;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.domain.Role;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class ControllerTestsBase extends AbstractTestsBase {

    protected void configureAsLocal() {
        configureAsLocal(new Properties());
    }

    protected void configureAsLocal(Properties properties) {
        properties.setProperty(StructurizrProperties.AUTO_REFRESH_INTERVAL_PROPERTY, "12345");
        Configuration.init(Profile.Local, properties);
        clearUser();
    }

    protected void enableAuthentication() {
        enableAuthentication(new Properties());
    }

    protected void enableAuthentication(Properties properties) {
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);
        clearUser();
    }

    protected void disableAuthentication() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_NONE);
        Configuration.init(Profile.Server, properties);
        clearUser();
    }

    protected void clearUser() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("anonymous", "anonymous", Set.of(new Role("role_anonymous"))));
    }

    protected void setUser(String username, String... roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            roles.add(new Role(roleName));
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(username, "password", roles);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, roles);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
