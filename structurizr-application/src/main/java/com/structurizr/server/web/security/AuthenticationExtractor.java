package com.structurizr.server.web.security;

import com.structurizr.server.domain.User;
import org.springframework.security.core.Authentication;

interface AuthenticationExtractor {

    User extract(Authentication authentication);

}
