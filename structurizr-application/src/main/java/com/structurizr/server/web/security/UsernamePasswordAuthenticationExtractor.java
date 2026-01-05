package com.structurizr.server.web.security;

import com.structurizr.server.domain.AuthenticationMethod;
import com.structurizr.server.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Set;

class UsernamePasswordAuthenticationExtractor implements AuthenticationExtractor {

    @Override
    public User extract(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        UserDetails userDetails = (UserDetails) principal;
        String username = userDetails.getUsername();

        Set<String> roles = new HashSet<>();
        for (GrantedAuthority grantedAuthority : userDetails.getAuthorities()) {
            roles.add(grantedAuthority.getAuthority());
        }

        return new User(username, roles, AuthenticationMethod.LOCAL);
    }

}