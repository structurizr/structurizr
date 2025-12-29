package com.structurizr.server.component.workspace;

import com.structurizr.server.domain.WorkspaceMetaData;

/**
 * Workspace metadata cache implementation that does nothing.
 */
class NoOpWorkspaceMetadataCache implements WorkspaceMetadataCache {

    @Override
    public WorkspaceMetaData get(long workspaceId) {
        return null;
    }

    @Override
    public void put(WorkspaceMetaData workspaceMetaData) {
    }

    @Override
    public void stop() {
    }

}