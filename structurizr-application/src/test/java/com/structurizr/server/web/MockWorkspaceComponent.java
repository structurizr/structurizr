package com.structurizr.server.web;

import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.server.component.workspace.WorkspaceComponent;
import com.structurizr.server.component.workspace.WorkspaceVersion;
import com.structurizr.server.domain.Image;
import com.structurizr.server.domain.InputStreamAndContentLength;
import com.structurizr.server.domain.User;
import com.structurizr.server.domain.WorkspaceMetaData;

import java.io.File;
import java.util.List;

public abstract class MockWorkspaceComponent implements WorkspaceComponent {

    @Override
    public List<WorkspaceMetaData> getWorkspaces() {
        return null;
    }

    @Override
    public List<WorkspaceMetaData> getWorkspaces(User user) {
        return null;
    }

    @Override
    public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
        return null;
    }

    @Override
    public void putWorkspaceMetaData(WorkspaceMetaData workspaceMetaData) {

    }

    @Override
    public String getWorkspace(long workspaceId, String branch, String version) {
        return null;
    }

    @Override
    public long createWorkspace(User user) {
        return 0;
    }

    @Override
    public boolean deleteWorkspace(long workspaceId) {
        return false;
    }

    @Override
    public void putWorkspace(long workspaceId, String branch, String json) {

    }

    @Override
    public List<WorkspaceVersion> getWorkspaceVersions(long workspaceId, String branch) {
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
    public boolean lockWorkspace(long workspaceId, String username, String agent) {
        return false;
    }

    @Override
    public boolean unlockWorkspace(long workspaceId) {
        return false;
    }

    @Override
    public boolean putImage(long workspaceId, String branch, String filename, File file) {
        return false;
    }

    @Override
    public List<Image> getImages(long workspaceId) {
        return null;
    }

    @Override
    public InputStreamAndContentLength getImage(long workspaceId, String branch, String filename) {
        return null;
    }

    @Override
    public boolean deleteImages(long workspaceId) {
        return false;
    }

    @Override
    public void makeWorkspacePublic(long workspaceId) {

    }

    @Override
    public void makeWorkspacePrivate(long workspaceId) {

    }

    @Override
    public void shareWorkspace(long workspaceId) {

    }

    @Override
    public void unshareWorkspace(long workspaceId) {

    }

    @Override
    public long getLastModifiedDate() {
        return 0;
    }

}