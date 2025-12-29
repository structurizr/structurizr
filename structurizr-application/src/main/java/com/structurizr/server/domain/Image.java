package com.structurizr.server.domain;

import java.util.Date;

public class Image {

    private final String name;
    private final long size;
    private final Date lastModifiedDate;
    private String url;

    public Image(String name, long size, Date lastModifiedDate) {
        this.name = name;
        this.size = size;
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getName() {
        return name;
    }

    public long getSizeInKB() {
        return size / 1024;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}