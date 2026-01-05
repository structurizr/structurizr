package com.structurizr.server.component.workspace;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.StructurizrProperties;

import java.util.HashMap;
import java.util.Map;

final class WorkspaceMetadataCacheFactory {

    static Map<String,Class<? extends WorkspaceMetadataCache>> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put(StructurizrProperties.CACHE_VARIANT_NONE, NoOpWorkspaceMetadataCache.class);
    }

    static WorkspaceMetadataCache create() {
        try {
            String implementation = Configuration.getInstance().getProperty(StructurizrProperties.CACHE_IMPLEMENTATION);
            Class<? extends WorkspaceMetadataCache> clazz = REGISTRY.get(implementation);
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new WorkspaceComponentException("Could not instantiate workspace metadata cache", e);
        }
    }

}