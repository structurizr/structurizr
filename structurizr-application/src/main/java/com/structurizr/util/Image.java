package com.structurizr.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLConnection;

public final class Image {

    private final File file;

    public Image(File file) {
        this.file = file;
    }

    public InputStream getInputStream() throws Exception {
        return new FileInputStream(file);
    }

    public long getContentLength() {
        return file.length();
    }

    public String getContentType() {
        return URLConnection.guessContentTypeFromName(file.getName());
    }

}