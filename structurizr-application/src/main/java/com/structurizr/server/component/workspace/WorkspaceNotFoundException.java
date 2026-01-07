package com.structurizr.server.component.workspace;

import java.io.File;

public class WorkspaceNotFoundException extends RuntimeException {

    WorkspaceNotFoundException(File directory) {
        super(
                directory.exists()
                ?
                String.format("No %s or %s file was found in %s.",
                        AbstractWorkspaceAdapter.WORKSPACE_DSL_FILENAME,
                        AbstractWorkspaceAdapter.WORKSPACE_JSON_FILENAME,
                        directory.getAbsolutePath()
                )
                :
                String.format("The workspace directory %s does not exist.",
                        directory.getAbsolutePath()
                )
        );
    }

}