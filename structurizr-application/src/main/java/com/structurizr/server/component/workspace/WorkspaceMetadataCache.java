package com.structurizr.server.component.workspace;

import com.structurizr.server.domain.WorkspaceMetadata;

interface WorkspaceMetadataCache {

    WorkspaceMetadata get(long workspaceId);

    void put(WorkspaceMetadata workspaceMetadata);

    void stop();

}