package com.structurizr.server.component.workspace;

import com.structurizr.server.domain.Image;
import com.structurizr.server.domain.InputStreamAndContentLength;
import com.structurizr.server.domain.WorkspaceMetadata;

import java.io.File;
import java.util.List;

interface WorkspaceAdapter {

    List<Long> getWorkspaceIds();

    WorkspaceMetadata getWorkspaceMetadata(long workspaceId);

    void putWorkspaceMetadata(WorkspaceMetadata workspaceMetadata);

    boolean deleteWorkspace(long workspaceId);

    String getWorkspace(long workspaceId, String branch, String version);

    void putWorkspace(WorkspaceMetadata workspaceMetadata, String json, String branch);

    List<WorkspaceVersion> getWorkspaceVersions(long workspaceId, String branch, int maxVersions);

    List<WorkspaceBranch> getWorkspaceBranches(long workspaceId);

    boolean deleteBranch(long workspaceId, String branch);

    boolean putImage(long workspaceId, String branch, String filename, File file);

    List<Image> getImages(long workspaceId, String branch);

    InputStreamAndContentLength getImage(long workspaceId, String branch, String filename);

    boolean deleteImages(long workspaceId, String branch);

    long getLastModifiedDate();

    void removeOldWorkspaceVersions();

}