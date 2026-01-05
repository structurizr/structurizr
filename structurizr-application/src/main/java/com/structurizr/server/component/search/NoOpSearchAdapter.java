package com.structurizr.server.component.search;

import com.structurizr.Workspace;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A search adapter implementation that does nothing.
 */
class NoOpSearchAdapter implements SearchAdapter {

    NoOpSearchAdapter() {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void index(Workspace workspace) {
    }

    @Override
    public List<SearchResult> search(String query, String type, Set<Long> workspaceIds) {
        return new ArrayList<>();
    }

    @Override
    public void delete(long workspaceId) {
    }

}