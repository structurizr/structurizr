package com.structurizr.server.web;

import com.structurizr.Workspace;
import com.structurizr.server.component.search.SearchComponent;
import com.structurizr.server.component.search.SearchResult;

import java.util.List;
import java.util.Set;

public class MockSearchComponent implements SearchComponent {

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

//    @Override
//    public boolean isEnabled() {
//        return true;
//    }

    @Override
    public void index(Workspace workspace) {
    }

    @Override
    public List<SearchResult> search(String query, String type, Set<Long> workspaceIds) {
        return List.of();
    }


    @Override
    public void delete(long workspaceId) {
    }

}