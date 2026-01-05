package com.structurizr.server.component.search;

import com.structurizr.Workspace;
import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
class SearchComponentImpl implements SearchComponent {

    private final SearchAdapter searchAdapter;

    SearchComponentImpl() {
        if (Configuration.getInstance().getProfile() == Profile.Local) {
            searchAdapter = new ApacheLuceneSearchAdapter();
        } else {
            searchAdapter = SearchAdapterFactory.create();
        }

        if (searchAdapter == null) {
            System.exit(1);
        }

        searchAdapter.start();
    }

    SearchComponentImpl(SearchAdapter searchAdapter) {
        this.searchAdapter = searchAdapter;
    }

    @Override
    public void start() {
        searchAdapter.start();
    }

    @Override
    @PreDestroy
    public void stop() {
        searchAdapter.stop();
    }

    @Override
    public void index(Workspace workspace) throws SearchComponentException {
        searchAdapter.index(workspace);
    }

    @Override
    public List<SearchResult> search(String query, String type, Set<Long> workspaceIds) throws SearchComponentException {
        return searchAdapter.search(query, type, workspaceIds);
    }

    @Override
    public void delete(long workspaceId) throws SearchComponentException {
        searchAdapter.delete(workspaceId);
    }

}