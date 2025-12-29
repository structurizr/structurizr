package com.structurizr.server.component.workspace;

import com.structurizr.server.domain.WorkspaceMetaData;

interface WorkspaceMetadataCache {

    WorkspaceMetaData get(long workspaceId);

    void put(WorkspaceMetaData workspaceMetaData);

    void stop();

}