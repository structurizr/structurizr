package com.structurizr.server.web.search;

import com.structurizr.Workspace;
import com.structurizr.server.component.search.SearchComponent;
import com.structurizr.server.component.workspace.WorkspaceComponent;
import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.util.WorkspaceUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.structurizr.configuration.StructurizrProperties.SEARCH_IMPLEMENTATION;

@Component
final class SearchIndexer {

    private static final Log log = LogFactory.getLog(SearchIndexer.class);

    private static final int NUMBER_OF_THREADS = 20;

    @Autowired
    private WorkspaceComponent workspaceComponent;

    @Autowired
    private SearchComponent searchComponent;

    private final ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    @PostConstruct
    void rebuildSearchIndex() {
        // rebuild local (Lucene) search indexes on startup
        if (StructurizrProperties.SEARCH_VARIANT_LUCENE.equals(Configuration.getInstance().getProperty(SEARCH_IMPLEMENTATION))) {
            log.info("Rebuilding search index...");

            try {
                Collection<WorkspaceMetaData> workspaces = workspaceComponent.getWorkspaces();
                for (WorkspaceMetaData workspaceMetaData : workspaces) {
                    executorService.submit(() -> {
                        try {
                            if (!workspaceMetaData.isClientEncrypted()) {
                                log.info("Indexing workspace with ID " + workspaceMetaData.getId());
                                String json = workspaceComponent.getWorkspace(workspaceMetaData.getId(), null, null);
                                Workspace workspace = WorkspaceUtils.fromJson(json);
                                searchComponent.index(workspace);
                            } else {
                                log.debug("Skipping workspace with ID " + workspaceMetaData.getId() + " because it's client-side encrypted");
                            }
                        } catch (Exception e) {
                            log.warn("Error indexing workspace with ID " + workspaceMetaData.getId(), e);
                        }
                    });
                }
            } catch (Exception e) {
                log.error("Error rebuilding search index", e);
            }
        }
    }

    @PreDestroy
    void stop() {
        executorService.shutdownNow();
    }

}