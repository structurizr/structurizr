package com.structurizr.server.component.workspace;

import com.structurizr.util.StringUtils;

import java.util.Date;

public final class WorkspaceVersion {

    public static final String LATEST_VERSION = "";

    private static final String VERSION_IDENTIFIER_REGEX = "[0-9a-zA-Z+-:_.]*";

    private String versionId;
    private final Date lastModifiedDate;

    public WorkspaceVersion(String versionId, Date lastModifiedDate) {
        this.versionId = versionId;
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getVersionId() {
        return versionId;
    }

    public void clearVersionId() {
        this.versionId = null;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public static void validateVersionIdentifier(String identifier) {
        if (!StringUtils.isNullOrEmpty(identifier) && !isValidBVersionIdentifier(identifier)) {
            throw new IllegalArgumentException("The version identifier \"" + identifier + "\" is invalid");
        }
    }

    public static boolean isValidBVersionIdentifier(String identifier) {
        return identifier.matches(VERSION_IDENTIFIER_REGEX);
    }

}