package com.structurizr.server.component.workspace;

class WorkspaceDirectory {

    private static final String WORKSPACE_ID_REGEX = "\\d*";
    private static final String WORKSPACE_ID_NAME_SEPARATOR = "-";
    private static final String WORKSPACE_ID_AND_NAME_REGEX = "\\d*" + WORKSPACE_ID_NAME_SEPARATOR + "[a-zA-Z0-9_-]*";

    static long parseWorkspaceId(String directoryName) {
        long id = 0;

        if (directoryName.matches(WORKSPACE_ID_REGEX)) {
            id = Long.parseLong(directoryName);
        } else if (directoryName.matches(WORKSPACE_ID_AND_NAME_REGEX)) {
            id = Long.parseLong(directoryName.split(WORKSPACE_ID_NAME_SEPARATOR)[0]);
        }

        return id;
    }

}