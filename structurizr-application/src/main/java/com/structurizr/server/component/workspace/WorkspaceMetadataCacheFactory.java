package com.structurizr.server.component.workspace;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.Server;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

final class WorkspaceMetadataCacheFactory {

    private static final Log log = LogFactory.getLog(Server.class);

    static Map<String,Class<? extends WorkspaceMetadataCache>> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put(StructurizrProperties.CACHE_VARIANT_NONE, NoOpWorkspaceMetadataCache.class);
    }

    static WorkspaceMetadataCache create() {
        String implementation = Configuration.getInstance().getProperty(StructurizrProperties.CACHE_IMPLEMENTATION);

        try {
            Class<? extends WorkspaceMetadataCache> clazz = REGISTRY.get(implementation);
            if (clazz != null) {
                return clazz.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.fatal(e);
        }

        log.fatal("Workspace metadata cache has not been configured: " +
                StructurizrProperties.CACHE_IMPLEMENTATION +
                "=" +
                implementation +
                " is not supported in this build");

        return null;
    }

}