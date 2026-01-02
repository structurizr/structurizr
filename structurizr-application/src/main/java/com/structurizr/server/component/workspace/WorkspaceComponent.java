package com.structurizr.server.component.workspace;

import com.structurizr.server.domain.InputStreamAndContentLength;
import com.structurizr.server.domain.User;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.domain.Image;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Provides access to workspace data.
 */
public interface WorkspaceComponent {

    List<WorkspaceMetaData> getWorkspaces();

    List<WorkspaceMetaData> getWorkspaces(User user);

    WorkspaceMetaData getWorkspaceMetaData(long workspaceId) throws WorkspaceComponentException;

    void putWorkspaceMetaData(WorkspaceMetaData workspaceMetaData);

    String getWorkspace(long workspaceId, String branch, String version);

    long createWorkspace(User user);

    boolean deleteWorkspace(long workspaceId);

    void putWorkspace(long workspaceId, String branch, String json);

    List<WorkspaceVersion> getWorkspaceVersions(long workspaceId, String branch);

    List<WorkspaceBranch> getWorkspaceBranches(long workspaceId);

    boolean deleteBranch(long workspaceId, String branch);

    boolean lockWorkspace(long workspaceId, String username, String agent);

    boolean unlockWorkspace(long workspaceId);

    boolean putImage(long workspaceId, String branch, String filename, File file);

    InputStreamAndContentLength getImage(long workspaceId, String branch, String filename);

    List<Image> getImages(long workspaceId);

    boolean deleteImages(long workspaceId);

    void makeWorkspacePublic(long workspaceId);

    void makeWorkspacePrivate(long workspaceId);

    void shareWorkspace(long workspaceId);

    void unshareWorkspace(long workspaceId);

    long getLastModifiedDate();

}