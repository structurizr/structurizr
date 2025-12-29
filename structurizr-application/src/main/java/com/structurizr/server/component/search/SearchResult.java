package com.structurizr.server.component.search;

import com.structurizr.server.domain.WorkspaceMetaData;

public final class SearchResult {

    private final long workspaceId;
    private WorkspaceMetaData workspace;
    private final String url;
    private final String name;
    private final String description;
    private final String type;

    public SearchResult(long workspaceId, String url, String name, String description, String type) {
        this.workspaceId = workspaceId;
        if (url.startsWith("/" + workspaceId)) {
            this.url = url.substring(("/" + workspaceId).length());
        } else {
            this.url = url;
        }
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public long getWorkspaceId() {
        return workspaceId;
    }

    public WorkspaceMetaData getWorkspace() {
        return workspace;
    }

    public void setWorkspace(WorkspaceMetaData workspace) {
        this.workspace = workspace;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

}