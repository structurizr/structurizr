package com.structurizr.server.component.workspace;

public final class WorkspaceBranchNotFoundException extends WorkspaceComponentException {

    private final long workspaceId;
    private final String branch;

    WorkspaceBranchNotFoundException(long workspaceId, String branch) {
        super("Branch \"" + branch + "\" does not exist for workspace " + workspaceId);

        this.workspaceId = workspaceId;
        this.branch = branch;
    }

    public long getWorkspaceId() {
        return workspaceId;
    }

    public String getBranch() {
        return branch;
    }

}