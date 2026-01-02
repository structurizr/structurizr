package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.domain.WorkspaceMetaData;

@FunctionalInterface
public interface AuthenticatedViewFunction {

    void run(WorkspaceMetaData workspaceMetaData);

}