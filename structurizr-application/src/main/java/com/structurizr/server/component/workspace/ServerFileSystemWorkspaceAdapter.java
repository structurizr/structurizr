package com.structurizr.server.component.workspace;

import com.structurizr.configuration.Configuration;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.structurizr.util.DateUtils.UTC_TIME_ZONE;

/**
 * A workspace adapter implementation that uses the local file system.
 */
class ServerFileSystemWorkspaceAdapter extends AbstractFileSystemWorkspaceAdapter {

    private static final Log log = LogFactory.getLog(ServerFileSystemWorkspaceAdapter.class);

    ServerFileSystemWorkspaceAdapter() {
        super(Configuration.getInstance().getDataDirectory());
    }

    @Override
    public List<Long> getWorkspaceIds() {
        File[] files = dataDirectory.listFiles();
        List<Long> workspaceIds = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file != null && file.isDirectory() && file.getName().matches("\\d*")) {
                    long id = Long.parseLong(file.getName());
                    workspaceIds.add(id);
                }
            }
        }

        Collections.sort(workspaceIds);
        return workspaceIds;
    }

    protected File getPathToWorkspace(long workspaceId) {
        return getPathToWorkspace(workspaceId, null, true);
    }

    protected File getPathToWorkspace(long workspaceId, String branch, boolean createIfNotExists) {
        File path;

        if (StringUtils.isNullOrEmpty(branch)) {
            path = new File(dataDirectory, "" + workspaceId);
        } else {
            path = new File(dataDirectory, workspaceId + "/" + BRANCHES_DIRECTORY_NAME + "/" + branch);
        }

        if (!path.exists() && createIfNotExists) {
            try {
                Path directory = Files.createDirectories(path.toPath());
                if (!directory.toFile().exists()) {
                    log.error(path.getCanonicalFile().getAbsolutePath() + " could not be created.");
                }
            } catch (IOException e) {
                log.error(e);
            }
        }

        return path;
    }

    @Override
    public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
        WorkspaceMetadata workspace = new WorkspaceMetadata(workspaceId);

        File workspacePropertiesFile = new File(getPathToWorkspace(workspaceId), WORKSPACE_PROPERTIES_FILENAME);
        if (workspacePropertiesFile.exists()) {
            try {
                FileReader fileReader = new FileReader(workspacePropertiesFile);
                Properties properties = new Properties();
                properties.load(fileReader);
                fileReader.close();

                workspace = WorkspaceMetadata.fromProperties(workspaceId, properties);
            } catch (Exception e) {
                log.error(e);
            }
        } else {
            return null;
        }

        return workspace;
    }

    @Override
    public void putWorkspaceMetadata(WorkspaceMetadata workspaceMetadata) {
        try {
            File path = getPathToWorkspace(workspaceMetadata.getId());
            File workspacePropertiesFile = new File(path, WORKSPACE_PROPERTIES_FILENAME);

            Properties properties = workspaceMetadata.toProperties();

            FileWriter fileWriter = new FileWriter(workspacePropertiesFile);
            properties.store(fileWriter, null);
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            throw new WorkspaceComponentException(e.getMessage(), e);
        }
    }

    @Override
    public String getWorkspace(long workspaceId, String branch, String version) {
        try {
            File path = getPathToWorkspace(workspaceId, branch, false);
            File file;

            if (!StringUtils.isNullOrEmpty(version)) {
                file = new File(path, String.format(WORKSPACE_VERSION_JSON_FILENAME, version));
            } else {
                file = new File(path, WORKSPACE_JSON_FILENAME);
            }

            if (file.exists()) {
                return Files.readString(file.toPath());
            } else {
                return null;
            }
        } catch (IOException ioe) {
            throw new WorkspaceComponentException("Could not get workspace " + workspaceId, ioe);
        }
    }

    @Override
    public void putWorkspace(WorkspaceMetadata workspaceMetadata, String json, String branch) {
        try {
            // write the latest version to workspace.json
            File path = getPathToWorkspace(workspaceMetadata.getId(), branch, true);
            File file = new File(path, WORKSPACE_JSON_FILENAME);
            Files.writeString(file.toPath(), json);

            try {
                // and write a versioned workspace.json file too
                SimpleDateFormat sdf = new SimpleDateFormat(VERSION_TIMESTAMP_FORMAT);
                sdf.setTimeZone(TimeZone.getTimeZone(UTC_TIME_ZONE));
                Files.writeString(new File(path, "workspace-" + sdf.format(workspaceMetadata.getLastModifiedDate()) + ".json").toPath(), json);
            } catch (Exception e) {
                log.error(e);
            }
        } catch (Exception e) {
            throw new WorkspaceComponentException(e.getMessage(), e);
        }
    }

    @Override
    public List<WorkspaceVersion> getWorkspaceVersions(long workspaceId, String branch, int maxVersions) {
        List<WorkspaceVersion> versions = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat(VERSION_TIMESTAMP_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(UTC_TIME_ZONE));

        try {
            File workspaceDirectory = getPathToWorkspace(workspaceId, branch, false);
            if (workspaceDirectory.exists()) {
                File[] files = workspaceDirectory.listFiles((dir, name) -> name.matches(WORKSPACE_VERSION_JSON_FILENAME_REGEX));

                if (files != null) {
                    Arrays.sort(files, (f1, f2) -> f2.getName().compareTo(f1.getName()));

                    for (int i = 0; i < Math.min(maxVersions, files.length); i++) {
                        File file = files[i];
                        String versionId = file.getName().substring(file.getName().indexOf('-') + 1, file.getName().indexOf('.'));
                        Date versionDate = sdf.parse(versionId);
                        versions.add(new WorkspaceVersion(versionId, versionDate));
                    }
                }

                if (versions.size() > 0) {
                    versions.get(0).clearVersionId();
                }
            }
        } catch (Exception ioe) {
            log.error(ioe);
        }

        return versions;
    }

    @Override
    public boolean deleteWorkspace(long workspaceId) {
        File workspaceDirectory = getPathToWorkspace(workspaceId);
        deleteDirectory(workspaceDirectory);

        return !workspaceDirectory.exists();
    }

    // todo: add this to a schedule
    public void removeOldWorkspaceVersions(int maxWorkspaceVersions) {
        try {
            Collection<Long> workspaceIds = getWorkspaceIds();

            for (Long workspaceId : workspaceIds) {
                File workspaceDirectory = getPathToWorkspace(workspaceId);
                File[] files = workspaceDirectory.listFiles((dir, name) -> name.matches(WORKSPACE_VERSION_JSON_FILENAME_REGEX));

                if (files != null) {
                    Arrays.sort(files, (a,b) -> b.getName().compareTo(a.getName()));

                    if (files.length > maxWorkspaceVersions) {
                        for (int i = maxWorkspaceVersions; i < files.length; i++) {
                            File file = files[i];
                            file.delete();
                        }
                    }
                }
            }
        } catch (Throwable t) {
            log.error(t);
        }
    }

    @Override
    public List<WorkspaceBranch> getWorkspaceBranches(long workspaceId) {
        List<WorkspaceBranch> branches = new ArrayList<>();

        try {
            File branchesDirectory = new File(getPathToWorkspace(workspaceId), BRANCHES_DIRECTORY_NAME);
            if (branchesDirectory.exists()) {
                File[] files = branchesDirectory.listFiles((dir, name) -> WorkspaceBranch.isValidBranchName(name));

                if (files != null) {
                    Arrays.sort(files, Comparator.comparing(File::getName));

                    for (File file : files) {
                        branches.add(new WorkspaceBranch(file.getName()));
                    }
                }
            }
        } catch (Exception ioe) {
            log.error(ioe);
        }

        return branches;
    }

    @Override
    public boolean deleteBranch(long workspaceId, String branch) {
        File branchDirectory = getPathToWorkspace(workspaceId, branch, false);
        deleteDirectory(branchDirectory);

        return !branchDirectory.exists();
    }

    @Override
    protected File getPathToWorkspaceImages(long workspaceId, String branch) {
        File path = new File(getPathToWorkspace(workspaceId, branch, true), IMAGES_DIRECTORY_NAME);
        if (!path.exists()) {
            try {
                Files.createDirectories(path.toPath());
            } catch (IOException e) {
                log.error(e);
            }
        }

        return path;
    }

    protected void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }

        directory.delete();
    }

    @Override
    public long getLastModifiedDate() {
        return 0;
    }

}