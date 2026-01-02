package com.structurizr.server.component.workspace;

public class WorkspaceComponentException extends RuntimeException {

    public WorkspaceComponentException(String message) {
        super(message);
    }

    public WorkspaceComponentException(Throwable cause) {
        super(cause);
    }

    public WorkspaceComponentException(String message, Throwable cause) {
        super(message, cause);
    }

}
