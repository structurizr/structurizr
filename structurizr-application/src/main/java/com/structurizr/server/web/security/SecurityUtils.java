package com.structurizr.server.web.security;

import com.structurizr.server.domain.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility to extract user details (username/roles) from Spring Security.
 */
public final class SecurityUtils {

    private static final Log log = LogFactory.getLog(SecurityUtils.class);

    private static final Map<Class<? extends Authentication>,AuthenticationExtractor> extractors = new HashMap<>();
    private static boolean authenticationConfigured = false;

    static {
        registerAuthenticationExtractor(AnonymousAuthenticationToken.class, new AnonymousAuthenticationExtractor());
        registerAuthenticationExtractor(UsernamePasswordAuthenticationToken.class, new UsernamePasswordAuthenticationExtractor());
    }

    public static void registerAuthenticationExtractor(Class<? extends Authentication> clazz, AuthenticationExtractor extractor) {
        extractors.put(clazz, extractor);
    }

    public static void setAuthenticationConfigured(boolean b) {
        authenticationConfigured = b;
    }

    public static boolean isAuthenticationConfigured() {
        return authenticationConfigured;
    }

    public static User getUser() {
        return getUser(SecurityContextHolder.getContext().getAuthentication());
    }

    public static User getUser(Authentication authentication) {
        if (authentication != null) {
            AuthenticationExtractor extractor = extractors.get(authentication.getClass());
            if (extractor != null) {
                return extractor.extract(authentication);
            } else {
                log.error("No authentication extractor found for class: " + authentication.getClass());
            }
        }

        return null;
    }

}