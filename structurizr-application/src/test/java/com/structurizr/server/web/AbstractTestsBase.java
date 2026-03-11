package com.structurizr.server.web;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.domain.Role;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public abstract class AbstractTestsBase {

    protected File createTemporaryDirectory() throws Exception {
        File directory = Files.createTempDirectory(this.getClass().getSimpleName()).toFile();
        directory.mkdirs();
        directory.deleteOnExit();

        return directory;
    }

    protected void deleteDirectory(File directory) {
        FileSystemUtils.deleteRecursively(directory);
    }

    private void configureDataDirectory(Properties properties) {
        if (!properties.containsKey(StructurizrProperties.DATA_DIRECTORY)) {
            try {
                properties.setProperty(StructurizrProperties.DATA_DIRECTORY, createTemporaryDirectory().getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void configureAsLocal() {
        configureAsLocal(new Properties());
    }

    protected void configureAsLocal(Properties properties) {
        properties.setProperty(StructurizrProperties.AUTO_REFRESH_INTERVAL_PROPERTY, "12345");
        configureDataDirectory(properties);
        Configuration.initLocal(properties);
        clearUser();
    }

    protected void configureAsServerWithAuthenticationEnabled() {
        configureAsServerWithAuthenticationEnabled(new Properties());
    }

    protected void configureAsServerWithAuthenticationEnabled(Properties properties) {
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        configureAsServer(properties);
        clearUser();
    }

    protected void configureAsServerWithAuthenticationDisabled() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_NONE);
        configureAsServer(properties);
        clearUser();
    }

    protected void configureAsServer() {
        configureAsServer(new Properties());
    }

    protected void configureAsServer(Properties properties) {
        configureDataDirectory(properties);
        Configuration.initServer(properties);
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