package com.structurizr.server.web.security;

import com.structurizr.server.domain.Role;
import com.structurizr.server.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UsernamePasswordAuthenticationTests {

    @Test
    void getUser_WithUsernameAndPasswordAuthentication() {
        Set<Role> roles = new HashSet<>();
        roles.add(new Role("role1"));
        roles.add(new Role("role2"));
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("user", "password", roles);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, roles);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = new UsernamePasswordAuthenticationExtractor().extract(authentication);
        assertEquals("user", user.getUsername());
        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().contains("role1"));
        assertTrue(user.getRoles().contains("role2"));
        assertTrue(user.isAuthenticated());
    }

}