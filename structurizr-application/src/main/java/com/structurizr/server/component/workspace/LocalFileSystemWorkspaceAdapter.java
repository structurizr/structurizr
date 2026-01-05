package com.structurizr.server.component.workspace;

import com.structurizr.Workspace;
import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.dsl.DslUtils;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.dsl.StructurizrDslParserException;
import com.structurizr.inspection.DefaultInspector;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.util.*;
import com.structurizr.validation.WorkspaceScopeValidationException;
import com.structurizr.validation.WorkspaceScopeValidatorFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

abstract class LocalFileSystemWorkspaceAdapter extends AbstractFileSystemWorkspaceAdapter {

    private static final Log log = LogFactory.getLog(LocalFileSystemWorkspaceAdapter.class);

    private final String API_KEY = new RandomGuidGenerator().generate();
    private final String API_SECRET = new RandomGuidGenerator().generate();

    protected String filename;

    private long lastModifiedDate = 0;

    LocalFileSystemWorkspaceAdapter(File dataDirectory) {
        super(dataDirectory);

        this.filename = Configuration.getInstance().getProperty(StructurizrProperties.WORKSPACE_FILENAME);
        this.lastModifiedDate = findLatestLastModifiedDate(dataDirectory);

        TimerTask task = new TimerTask() {
            public void run() {
                lastModifiedDate = findLatestLastModifiedDate(dataDirectory);
            }
        };
        Timer timer = new Timer("findLatestLastModifiedDateTimer");
        long delay = 1000L;
        long period = 1000L;
        timer.scheduleAtFixedRate(task, delay, period);
    }

    protected abstract File getDataDirectory(long workspaceId);

    private Workspace loadWorkspace(long workspaceId) throws WorkspaceComponentException {
        File workspaceDirectory = getDataDirectory(workspaceId);
        File dslFile = new File(workspaceDirectory, filename + DSL_FILE_EXTENSION);
        File jsonFile = new File(workspaceDirectory, filename + JSON_FILE_EXTENSION);

        if (jsonFile.exists() && jsonFile.lastModified() > lastModifiedDate) {
            try {
                return loadWorkspaceFromJson(workspaceId, jsonFile);
            } catch (Exception e) {
                throw new WorkspaceComponentException(e);
            }
        } else {
            if (dslFile.exists()) {
                try {
                    return loadWorkspaceFromDsl(workspaceId, dslFile, jsonFile);
                } catch (StructurizrDslParserException | WorkspaceScopeValidationException e) {
                    throw new WorkspaceComponentException(e);
                }
            } else if (jsonFile.exists()) {
                Workspace workspace = null;
                try {
                    workspace = loadWorkspaceFromJson(workspaceId, jsonFile);
                } catch (Exception e) {
                    throw new WorkspaceComponentException(e);
                }

                // if the JSON file exists and contains DSL, extract this and save it
                String embeddedDsl = DslUtils.getDsl(workspace);
                if (!StringUtils.isNullOrEmpty(embeddedDsl)) {
                    FileUtils.write(dslFile, embeddedDsl);
                }

                return workspace;
            } else {
                throw new WorkspaceNotFoundException(workspaceDirectory, filename);
            }
        }
    }

    private Workspace loadWorkspaceFromJson(long workspaceId, File jsonFile) throws Exception {
        Workspace workspace = null;

        if (jsonFile.exists()) {
            workspace = WorkspaceUtils.loadWorkspaceFromJson(jsonFile);
            workspace.setId(workspaceId);

            // validate workspace scope
            WorkspaceScopeValidatorFactory.getValidator(workspace).validate(workspace);

            // run default inspections
            new DefaultInspector(workspace);
        }

        return workspace;
    }

    private Workspace loadWorkspaceFromDsl(long workspaceId, File dslFile, File jsonFile) throws StructurizrDslParserException, WorkspaceScopeValidationException {
        Workspace workspace;

        StructurizrDslParser parser = new StructurizrDslParser();
        Configuration.getInstance().configure(parser.getHttpClient());
        parser.parse(dslFile);
        workspace = parser.getWorkspace();
        workspace.setId(workspaceId);

        // validate workspace scope
        WorkspaceScopeValidatorFactory.getValidator(workspace).validate(workspace);

        // run default inspections
        new DefaultInspector(workspace);

        if (!workspace.getModel().isEmpty() && workspace.getViews().isEmpty()) {
            workspace.getViews().createDefaultViews();
        }

        try {
            Workspace workspaceFromJson = loadWorkspaceFromJson(workspaceId, jsonFile);
            if (workspaceFromJson != null) {
                workspace.getViews().copyLayoutInformationFrom(workspaceFromJson.getViews());
                workspace.getViews().getConfiguration().copyConfigurationFrom(workspaceFromJson.getViews().getConfiguration());
            }
        } catch (Exception e) {
            throw new WorkspaceComponentException(e);
        }

        workspace.setLastModifiedDate(DateUtils.removeMilliseconds(DateUtils.getNow()));

        try {
            putWorkspace(new WorkspaceMetaData(workspaceId), WorkspaceUtils.toJson(workspace, false), null);
        } catch (Exception e) {
            log.warn(e);
        }

        return workspace;
    }

    @Override
    public String getWorkspace(long workspaceId, String branch, String version) {
        try {
            File jsonFile = new File(getDataDirectory(workspaceId), filename + JSON_FILE_EXTENSION);
            if (jsonFile.exists()) {
                return Files.readString(jsonFile.toPath());
            } else {
                throw new RuntimeException("Workspace " + workspaceId + " does not exist");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void putWorkspace(WorkspaceMetaData workspaceMetaData, String json, String branch) {
        try {
            File jsonFile = new File(getDataDirectory(workspaceMetaData.getId()), filename + JSON_FILE_EXTENSION);

            Workspace workspace = WorkspaceUtils.fromJson(json);
            workspace.setLastModifiedDate(DateUtils.removeMilliseconds(DateUtils.getNow()));
            WorkspaceUtils.saveWorkspaceToJson(workspace, jsonFile);
        } catch (Exception e) {
            log.error(e);
            throw new WorkspaceComponentException(e.getMessage());
        }
    }

    @Override
    public boolean deleteWorkspace(long workspaceId) {
        return true;
    }

    @Override
    public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
        WorkspaceMetaData wmd = new WorkspaceMetaData(workspaceId);
        wmd.setApiKey(API_KEY);
        wmd.setApiSecret(API_SECRET);

        Workspace workspace = loadWorkspace(workspaceId);
        if (workspace != null) {
            wmd.setName(workspace.getName());
            wmd.setDescription(workspace.getDescription());
        }

        return wmd;
    }

    @Override
    public void putWorkspaceMetaData(WorkspaceMetaData workspaceMetaData) {
        // no-op
    }

    private long findLatestLastModifiedDate(File directory) {
        long timestamp = 0;

        String dslFilename = filename + DSL_FILE_EXTENSION;
        String jsonFilename = filename + JSON_FILE_EXTENSION;

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith(".") || file.getName().equals(StructurizrProperties.CONFIGURATION_FILE_NAME)) {
                    // ignore
                } else if (file.isFile()) {
                    if (file.getName().equals(jsonFilename) && new File(file.getParentFile(), dslFilename).exists()) {
                        // ignore JSON file updates if the DSL is being used as the authoring method
                        // e.g. ignore workspace.json if workspace.dsl exists in the same directory
                    } else {
                        timestamp = Math.max(timestamp, file.lastModified());
                    }
                } else if (file.isDirectory()) {
                    timestamp = Math.max(timestamp, findLatestLastModifiedDate(file));
                }
            }
        }

        return timestamp;
    }

    @Override
    public List<WorkspaceVersion> getWorkspaceVersions(long workspaceId, String branch, int maxVersions) {
        return new ArrayList<>();
    }

    @Override
    public List<WorkspaceBranch> getWorkspaceBranches(long workspaceId) {
        return new ArrayList<>();
    }

    @Override
    public boolean deleteBranch(long workspaceId, String branch) {
        return false;
    }

    @Override
    protected File getPathToWorkspaceImages(long workspaceId, String branch) {
        File path = new File(new File(Configuration.getInstance().getWorkDirectory(), "" + workspaceId), IMAGES_DIRECTORY_NAME);
        if (!path.exists()) {
            try {
                Files.createDirectories(path.toPath());
            } catch (IOException e) {
                log.error(e);
            }
        }

        return path;
    }

    @Override
    public long getLastModifiedDate() {
        return lastModifiedDate;
    }

}