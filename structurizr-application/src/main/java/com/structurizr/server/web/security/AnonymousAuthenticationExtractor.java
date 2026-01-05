package com.structurizr.server.web.security;

import com.structurizr.server.domain.AuthenticationMethod;
import com.structurizr.server.domain.User;
import com.structurizr.util.RandomGuidGenerator;
import org.springframework.security.core.Authentication;

import java.util.Collections;

class AnonymousAuthenticationExtractor implements AuthenticationExtractor {

    @Override
    public User extract(Authentication authentication) {
        return new User(new RandomGuidGenerator().generate().substring(0, 8), Collections.emptySet(), AuthenticationMethod.NONE);
    }

}