package com.structurizr.server.component.search;

import com.structurizr.Workspace;

import java.util.List;
import java.util.Set;

/**
 * Provides workspace search facilities using Apache Lucene.
 */
public interface SearchComponent {

    void start();

    void stop();

    void index(Workspace workspace);

    List<SearchResult> search(String query, String type, Set<Long> workspaceIds);

    void delete(long workspaceId);

}