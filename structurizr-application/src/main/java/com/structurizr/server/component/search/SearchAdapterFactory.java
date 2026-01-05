package com.structurizr.server.component.search;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.component.workspace.WorkspaceComponentException;

import java.util.HashMap;
import java.util.Map;

final class SearchAdapterFactory {

    static Map<String,Class<? extends SearchAdapter>> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put(StructurizrProperties.SEARCH_VARIANT_LUCENE, ApacheLuceneSearchAdapter.class);
        REGISTRY.put(StructurizrProperties.SEARCH_VARIANT_NONE, NoOpSearchAdapter.class);
    }

    static SearchAdapter create() {
        try {
            String implementation = Configuration.getInstance().getProperty(StructurizrProperties.SEARCH_IMPLEMENTATION);
            Class<? extends SearchAdapter> clazz = REGISTRY.get(implementation);
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new WorkspaceComponentException("Could not instantiate search adapter", e);
        }
    }

}