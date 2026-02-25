package com.structurizr.api;

public class WorkspaceApiClientWhenAuthenticationDisabledIntegrationTests extends AbstractWorkspaceApiClientTests {

    @Override
    protected WorkspaceApiClient createWorkspaceApiClient(String apiUrl, long workspaceId) {
        return new WorkspaceApiClient(apiUrl, workspaceId, "");
    }

}