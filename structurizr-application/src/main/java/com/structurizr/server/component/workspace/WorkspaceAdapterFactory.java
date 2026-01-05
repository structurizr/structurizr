package com.structurizr.server.component.workspace;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.StructurizrProperties;

import java.util.HashMap;
import java.util.Map;

final class WorkspaceAdapterFactory {

    static Map<String,Class<? extends WorkspaceAdapter>> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put(StructurizrProperties.DATA_STORAGE_VARIANT_FILE, ServerFileSystemWorkspaceAdapter.class);
    }

    static WorkspaceAdapter create() {
        try {
            String implementation = Configuration.getInstance().getProperty(StructurizrProperties.DATA_STORAGE_IMPLEMENTATION);
            Class<? extends WorkspaceAdapter> clazz = REGISTRY.get(implementation);
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new WorkspaceComponentException("Could not instantiate workspace adapter", e);
        }
    }

}