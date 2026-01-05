package com.structurizr.server.component.workspace;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.Server;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

final class WorkspaceAdapterFactory {

    private static final Log log = LogFactory.getLog(Server.class);

    static Map<String,Class<? extends WorkspaceAdapter>> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put(StructurizrProperties.DATA_STORAGE_VARIANT_FILE, ServerFileSystemWorkspaceAdapter.class);
    }

    static WorkspaceAdapter create() {
        String implementation = Configuration.getInstance().getProperty(StructurizrProperties.DATA_STORAGE_IMPLEMENTATION);

        try {
            Class<? extends WorkspaceAdapter> clazz = REGISTRY.get(implementation);
            if (clazz != null) {
                return clazz.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            log.fatal(e);
        }

        log.fatal("Workspace data storage has not been configured: " +
                StructurizrProperties.DATA_STORAGE_IMPLEMENTATION +
                "=" +
                implementation +
                " is not supported in this build");

        return null;
    }

}