package com.structurizr.server.component.workspace;

import com.structurizr.util.DslTemplate;
import com.structurizr.util.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.List;

class SingleWorkspaceLocalFileSystemWorkspaceAdapter extends LocalFileSystemWorkspaceAdapter {

    private static final Log log = LogFactory.getLog(SingleWorkspaceLocalFileSystemWorkspaceAdapter.class);

    private static final long WORKSPACE_ID = 1;

    SingleWorkspaceLocalFileSystemWorkspaceAdapter(File dataDirectory) {
        super(dataDirectory);

        createWorkspaceWhenDirectoryIsEmpty();
    }

    private void createWorkspaceWhenDirectoryIsEmpty() {
        File dsl = new File(getDataDirectory(WORKSPACE_ID), filename + DSL_FILE_EXTENSION);
        File json = new File(getDataDirectory(WORKSPACE_ID), filename + JSON_FILE_EXTENSION);

        if (!dsl.exists() && !json.exists()) {
            log.info("Creating " + dsl.getName());
            FileUtils.write(new File(dataDirectory, filename + DSL_FILE_EXTENSION), DslTemplate.generate("Name", "Description"));
        }
    }

    @Override
    public List<Long> getWorkspaceIds() {
        return List.of(WORKSPACE_ID);
    }

    @Override
    protected File getDataDirectory(long workspaceId) {
        return dataDirectory;
    }

}