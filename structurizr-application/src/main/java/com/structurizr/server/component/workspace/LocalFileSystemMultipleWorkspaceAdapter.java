package com.structurizr.server.component.workspace;

import com.structurizr.configuration.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.structurizr.server.component.workspace.WorkspaceDirectory.parseWorkspaceId;

class LocalFileSystemMultipleWorkspaceAdapter extends LocalFileSystemWorkspaceAdapter {

    LocalFileSystemMultipleWorkspaceAdapter() {
        super(Configuration.getInstance().getDataDirectory());
    }

    @Override
    public List<Long> getWorkspaceIds() {
        File[] files = dataDirectory.listFiles();
        List<Long> workspaceIds = new ArrayList<>();

        if (files != null) {
            Arrays.sort(files);
            for (File file : files) {
                if (file != null && file.isDirectory()) {
                    long id = parseWorkspaceId(file.getName());
                    if (id > 0) {
                        if (workspaceIds.contains(id)) {
                            log.warn("A workspace with ID " + id + " already exists - ignoring " + file.getName());
                        } else {
                            workspaceIds.add(id);
                        }
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
                Arrays.sort(files);
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