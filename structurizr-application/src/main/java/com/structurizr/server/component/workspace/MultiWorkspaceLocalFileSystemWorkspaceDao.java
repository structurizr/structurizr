package com.structurizr.server.component.workspace;

import java.io.File;

import static com.structurizr.server.component.workspace.WorkspaceDirectory.parseWorkspaceId;

class MultiWorkspaceLocalFileSystemWorkspaceDao extends LocalFileSystemWorkspaceDao {

    MultiWorkspaceLocalFileSystemWorkspaceDao(File dataDirectory) {
        super(dataDirectory);
    }

    protected File getDataDirectory(long workspaceId) {
        File directory = new File(dataDirectory, "" + workspaceId);
        if (!directory.exists()) {
            File[] files = dataDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory() && parseWorkspaceId(file.getName()) == workspaceId) {
                        return file;
                    }
                }
            }
        }

        return directory;
    }

}