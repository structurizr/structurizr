package com.structurizr.server.component.workspace;

import com.structurizr.server.domain.Image;
import com.structurizr.server.domain.InputStreamAndContentLength;
import com.structurizr.util.ImageUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

abstract class AbstractFileSystemWorkspaceAdapter extends AbstractWorkspaceAdapter {

    private static final Log log = LogFactory.getLog(WorkspaceAdapter.class);

    protected final File dataDirectory;

    AbstractFileSystemWorkspaceAdapter(File dataDirectory) {
        this.dataDirectory = dataDirectory;
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

        File[] files = imagesDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(ImageUtils.PNG_EXTENSION) || name.toLowerCase().endsWith(ImageUtils.SVG_EXTENSION));

        if (files != null) {
            for (File file : files) {
                images.add(new Image(file.getName(), file.length(), new Date(file.lastModified())));
            }
        }

        images.sort(Comparator.comparing(i -> i.getName().toLowerCase()));

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

    protected abstract File getPathToWorkspaceImages(long workspaceId, String branch);

}