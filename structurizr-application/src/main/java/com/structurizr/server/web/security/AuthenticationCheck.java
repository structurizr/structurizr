package com.structurizr.server.web.security;

import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.Server;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;

@Configuration
@Profile("command-server")
public class AuthenticationCheck {

    private static final Log log = LogFactory.getLog(Server.class);

    @EventListener
    public void onApplicationEvent(final ServletWebServerInitializedEvent event) {
        if (!SecurityUtils.isAuthenticationConfigured()) {
            log.fatal("Authentication has not been configured: " +
                    StructurizrProperties.AUTHENTICATION_IMPLEMENTATION +
                    "=" +
                    com.structurizr.configuration.Configuration.getInstance().getProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION) +
                    " is not supported in this build");
            System.exit(1);
        }
    }

}