package com.structurizr.server.component.workspace;

public class WorkspaceLockResponse {

    private final boolean success;
    private final boolean locked;
    private String message;

    public WorkspaceLockResponse(boolean success, boolean locked) {
        this.success = success;
        this.locked = locked;
    }

    public WorkspaceLockResponse(boolean success, boolean locked, String message) {
        this.success = success;
        this.locked = locked;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isLocked() {
        return locked;
    }

    public String getMessage() {
        return message;
    }

}