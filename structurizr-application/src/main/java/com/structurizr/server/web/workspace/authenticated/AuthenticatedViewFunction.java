package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.domain.WorkspaceMetadata;

@FunctionalInterface
public interface AuthenticatedViewFunction {

    void run(WorkspaceMetadata workspaceMetadata);

}