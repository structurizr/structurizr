package com.structurizr.server.web.security;

import com.structurizr.server.domain.AuthenticationMethod;
import com.structurizr.server.domain.User;
import org.springframework.security.core.Authentication;

import java.util.Collections;

class AnonymousAuthenticationExtractor implements AuthenticationExtractor {

    @Override
    public User extract(Authentication authentication) {
        return new User("" + Math.abs(authentication.hashCode()), Collections.emptySet(), AuthenticationMethod.NONE);
    }

}