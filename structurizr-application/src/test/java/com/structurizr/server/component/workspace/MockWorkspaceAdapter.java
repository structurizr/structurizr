package com.structurizr.server.component.workspace;

import com.structurizr.server.domain.Image;
import com.structurizr.server.domain.InputStreamAndContentLength;
import com.structurizr.server.domain.WorkspaceMetadata;

import java.io.File;
import java.util.List;

class MockWorkspaceAdapter implements WorkspaceAdapter {

    @Override
    public List<Long> getWorkspaceIds() {
        return List.of();
    }

    @Override
    public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
        return null;
    }

    @Override
    public void putWorkspaceMetadata(WorkspaceMetadata workspaceMetaData) {

    }

    @Override
    public boolean deleteWorkspace(long workspaceId) {
        return false;
    }

    @Override
    public String getWorkspace(long workspaceId, String branch, String version) {
        return null;
    }

    @Override
    public void putWorkspace(WorkspaceMetadata workspaceMetaData, String json, String branch) {

    }

    @Override
    public List<WorkspaceVersion> getWorkspaceVersions(long workspaceId, String branch, int maxVersions) {
        return List.of();
    }

    @Override
    public List<WorkspaceBranch> getWorkspaceBranches(long workspaceId) {
        return List.of();
    }

    @Override
    public boolean deleteBranch(long workspaceId, String branch) {
        return false;
    }

    @Override
    public boolean putImage(long workspaceId, String branch, String filename, File file) {
        return false;
    }

    @Override
    public List<Image> getImages(long workspaceId, String branch) {
        return List.of();
    }

    @Override
    public InputStreamAndContentLength getImage(long workspaceId, String branch, String filename) {
        return null;
    }

    @Override
    public boolean deleteImages(long workspaceId, String branch) {
        return false;
    }

    @Override
    public long getLastModifiedDate() {
        return 0;
    }

    @Override
    public void removeOldWorkspaceVersions() {
    }

}