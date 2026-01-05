package com.structurizr.server.web.security;

import com.structurizr.server.domain.AuthenticationMethod;
import com.structurizr.server.domain.Role;
import com.structurizr.server.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AnonymousAuthenticationExtractorTests {

    @Test
    void getUser_WithAnonymousAuthentication() {
        Authentication authentication = new AnonymousAuthenticationToken("anonymous", "anonymous", Set.of(new Role("role_anonymous")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = new AnonymousAuthenticationExtractor().extract(authentication);
        assertTrue(user.getUsername().matches("[a-z0-9]{8}"));
        assertEquals(0, user.getRoles().size());
        assertEquals(AuthenticationMethod.NONE, user.getAuthenticationMethod());
        assertFalse(user.isAuthenticated());
    }

}