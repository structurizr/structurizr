package com.structurizr.server.component.workspace;

import com.structurizr.server.domain.Image;
import com.structurizr.server.domain.InputStreamAndContentLength;
import com.structurizr.server.domain.User;
import com.structurizr.server.domain.WorkspaceMetaData;

import java.io.File;
import java.util.List;

interface WorkspaceDao {

    List<Long> getWorkspaceIds();

    WorkspaceMetaData getWorkspaceMetaData(long workspaceId);

    void putWorkspaceMetaData(WorkspaceMetaData workspaceMetaData);

    long createWorkspace(User user) throws WorkspaceComponentException;

    boolean deleteWorkspace(long workspaceId);

    String getWorkspace(long workspaceId, String branch, String version);

    void putWorkspace(WorkspaceMetaData workspaceMetaData, String json, String branch);

    List<WorkspaceVersion> getWorkspaceVersions(long workspaceId, String branch, int maxVersions);

    List<WorkspaceBranch> getWorkspaceBranches(long workspaceId);

    boolean deleteBranch(long workspaceId, String branch);

    boolean putImage(long workspaceId, String branch, String filename, File file);

    List<Image> getImages(long workspaceId);

    InputStreamAndContentLength getImage(long workspaceId, String branch, String filename);

    boolean deleteImages(long workspaceId);

    long getLastModifiedDate();

}