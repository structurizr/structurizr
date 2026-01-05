package com.structurizr.server.component.search;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.Server;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

final class SearchAdapterFactory {

    private static final Log log = LogFactory.getLog(Server.class);

    static Map<String,Class<? extends SearchAdapter>> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put(StructurizrProperties.SEARCH_VARIANT_LUCENE, ApacheLuceneSearchAdapter.class);
        REGISTRY.put(StructurizrProperties.SEARCH_VARIANT_NONE, NoOpSearchAdapter.class);
    }

    static SearchAdapter create() {
        String implementation = Configuration.getInstance().getProperty(StructurizrProperties.SEARCH_IMPLEMENTATION);

        try {
            Class<? extends SearchAdapter> clazz = REGISTRY.get(implementation);
            if (clazz != null) {
                return clazz.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            log.error(e);
        }

        log.fatal("Search has not been configured: " +
                StructurizrProperties.SEARCH_IMPLEMENTATION +
                "=" +
                implementation +
                " is not supported in this build");

        return null;
    }

}