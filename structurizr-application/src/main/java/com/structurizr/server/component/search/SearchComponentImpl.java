package com.structurizr.server.component.search;

import com.structurizr.Workspace;
import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import com.structurizr.configuration.StructurizrProperties;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.structurizr.configuration.StructurizrProperties.SEARCH_IMPLEMENTATION;

@Component
class SearchComponentImpl implements SearchComponent {

    private final SearchComponent searchComponent;

    SearchComponentImpl() {
        if (Configuration.getInstance().getProfile() == Profile.Local) {
            searchComponent = new ApacheLuceneSearchComponentImpl();
        } else {
            String searchImplementation = Configuration.getInstance().getProperty(SEARCH_IMPLEMENTATION);
            if (StructurizrProperties.SEARCH_VARIANT_NONE.equalsIgnoreCase(searchImplementation)) {
                searchComponent = new NoOpSearchComponentImpl();
            } else {
                searchComponent = new ApacheLuceneSearchComponentImpl();
            }
        }

        searchComponent.start();
    }

    SearchComponentImpl(SearchComponent searchComponent) {
        this.searchComponent = searchComponent;
    }

    @Override
    public void start() {
        searchComponent.start();
    }

    @Override
    @PreDestroy
    public void stop() {
        searchComponent.stop();
    }

    @Override
    public void index(Workspace workspace) throws SearchComponentException {
        searchComponent.index(workspace);
    }

    @Override
    public List<SearchResult> search(String query, String type, Set<Long> workspaceIds) throws SearchComponentException {
        return searchComponent.search(query, type, workspaceIds);
    }

    @Override
    public void delete(long workspaceId) throws SearchComponentException {
        searchComponent.delete(workspaceId);
    }

}