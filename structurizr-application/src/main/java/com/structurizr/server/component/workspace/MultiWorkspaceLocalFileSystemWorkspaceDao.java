package com.structurizr.server.component.workspace;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.structurizr.server.component.workspace.WorkspaceDirectory.parseWorkspaceId;

class MultiWorkspaceLocalFileSystemWorkspaceDao extends LocalFileSystemWorkspaceDao {

    MultiWorkspaceLocalFileSystemWorkspaceDao(File dataDirectory) {
        super(dataDirectory);
    }

    @Override
    public List<Long> getWorkspaceIds() {
        File[] files = dataDirectory.listFiles();
        List<Long> workspaceIds = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file != null && file.isDirectory()) {
                    long id = parseWorkspaceId(file.getName());
                    if (id > 0) {
                        workspaceIds.add(id);
                    }
                }
            }
        }

        Collections.sort(workspaceIds);
        return workspaceIds;
    }

    protected File getDataDirectory(long workspaceId) {
        File directory = new File(dataDirectory, "" + workspaceId);
        if (!directory.exists() || !directory.isDirectory()) {
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