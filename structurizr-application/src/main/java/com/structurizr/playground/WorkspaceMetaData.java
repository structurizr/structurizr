package com.structurizr.playground;

import java.util.Date;

public class WorkspaceMetaData {

    private final Date lastModifiedDate = new Date();

    public long getId() {
        return 0;
    }

    public String getName() {
        return "";
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public boolean isEditable() {
        return true;
    }

    public String getBranch() {
        return null;
    }

}