package com.structurizr.server.component.workspace;

import java.io.File;

public class WorkspaceNotFoundException extends RuntimeException {

    WorkspaceNotFoundException(File directory, String filename) {
        super(
                directory.exists()
                ?
                String.format("No %s.dsl or %s.json file was found in %s.", filename, filename, directory.getAbsolutePath())
                :
                String.format("The workspace directory %s does not exist.", directory.getAbsolutePath())
        );
    }

}