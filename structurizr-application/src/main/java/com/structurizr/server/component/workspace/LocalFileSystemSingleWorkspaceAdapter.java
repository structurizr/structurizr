package com.structurizr.server.component.workspace;

import com.structurizr.configuration.Configuration;
import com.structurizr.util.DslTemplate;
import com.structurizr.util.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.List;

class LocalFileSystemSingleWorkspaceAdapter extends LocalFileSystemWorkspaceAdapter {

    private static final Log log = LogFactory.getLog(LocalFileSystemSingleWorkspaceAdapter.class);

    private static final long WORKSPACE_ID = 1;

    LocalFileSystemSingleWorkspaceAdapter() {
        super(Configuration.getInstance().getDataDirectory());

        createWorkspaceWhenDirectoryIsEmpty();
    }

    private void createWorkspaceWhenDirectoryIsEmpty() {
        File dsl = new File(getDataDirectory(WORKSPACE_ID), WORKSPACE_DSL_FILENAME);
        File json = new File(getDataDirectory(WORKSPACE_ID), WORKSPACE_JSON_FILENAME);

        if (!dsl.exists() && !json.exists()) {
            log.info("Creating " + dsl.getName());
            FileUtils.write(new File(dataDirectory, WORKSPACE_DSL_FILENAME), DslTemplate.generate("Name", "Description"));
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