package com.structurizr.server.component.workspace;

import com.structurizr.server.domain.Image;
import com.structurizr.server.domain.InputStreamAndContentLength;
import com.structurizr.server.domain.User;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

abstract class AbstractFileSystemWorkspaceDao extends AbstractWorkspaceDao {

    private static final Log log = LogFactory.getLog(WorkspaceDao.class);

    protected static final String DSL_FILE_EXTENSION = ".dsl";
    protected static final String JSON_FILE_EXTENSION = ".json";

    protected static final String WORKSPACE_JSON_FILENAME = "workspace" + JSON_FILE_EXTENSION;
    protected static final String IMAGES_DIRECTORY_NAME = "images";
    protected static final String PNG_FILENAME_REGEX = ".*\\.png";
    protected static final String BRANCHES_DIRECTORY_NAME = "branches";

    protected final File dataDirectory;

    AbstractFileSystemWorkspaceDao(File dataDirectory) {
        this.dataDirectory = dataDirectory;
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
    public boolean putImage(long workspaceId, String branch, String filename, File file) {
        try {
            File imagesDirectory = getPathToWorkspaceImages(workspaceId, branch);
            File destination = new File(imagesDirectory, filename);
            Files.move(file.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public InputStreamAndContentLength getImage(long workspaceId, String branch, String filename) {
        try {
            File imagesDirectory = getPathToWorkspaceImages(workspaceId, branch);
            File file = new File(imagesDirectory, filename);
            if (file.exists()) {
                return new InputStreamAndContentLength(new FileInputStream(file), file.length());
            }
        } catch (Exception e) {
            String message = "Could not get " + filename + " for workspace " + workspaceId;
            log.warn(e.getMessage() + " - " + message);
        }

        return null;
    }

    @Override
    public List<Image> getImages(long workspaceId) {
        List<Image> images = new LinkedList<>();
        File imagesDirectory = getPathToWorkspaceImages(workspaceId, WorkspaceBranch.NO_BRANCH);

        File[] files = imagesDirectory.listFiles((dir, name) -> name.matches(PNG_FILENAME_REGEX));

        if (files != null) {
            for (File file : files) {
                images.add(new Image(file.getName(), file.length(), new Date(file.lastModified())));
            }
        }

        return images;
    }

    @Override
    public boolean deleteImages(long workspaceId) {
        File imagesDirectory = getPathToWorkspaceImages(workspaceId, WorkspaceBranch.NO_BRANCH);
        File[] files = imagesDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }

        return imagesDirectory.delete();
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

}