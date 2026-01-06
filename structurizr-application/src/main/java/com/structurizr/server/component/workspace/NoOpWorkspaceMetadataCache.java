package com.structurizr.server.component.workspace;

import com.structurizr.server.domain.WorkspaceMetadata;

/**
 * Workspace metadata cache implementation that does nothing.
 */
class NoOpWorkspaceMetadataCache implements WorkspaceMetadataCache {

    @Override
    public WorkspaceMetadata get(long workspaceId) {
        return null;
    }

    @Override
    public void put(WorkspaceMetadata workspaceMetadata) {
    }

    @Override
    public void stop() {
    }

}