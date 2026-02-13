package com.structurizr.api;

class HttpClientResult {

    final boolean success;
    final String content;

    HttpClientResult(boolean success, String content) {
        this.success = success;
        this.content = content;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getContent() {
        return content;
    }

}