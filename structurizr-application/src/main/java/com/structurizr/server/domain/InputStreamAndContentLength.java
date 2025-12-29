package com.structurizr.server.domain;

import java.io.InputStream;

public final class InputStreamAndContentLength {

    private final InputStream inputStream;
    private final long contentLength;

    public InputStreamAndContentLength(InputStream inputStream, long contentLength) {
        this.inputStream = inputStream;
        this.contentLength = contentLength;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public long getContentLength() {
        return contentLength;
    }

}