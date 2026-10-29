package com.structurizr.mcp;

abstract class AbstractServerTools extends AbstractTools {

    private static final String API_PATH = "api";
    private static final String WORKSPACE_PATH = "workspace";

    protected String apiUrl(String url) {
        if (!url.endsWith("/")) {
            url = url + "/";
        }

        return url + API_PATH;
    }

    protected String workspaceUrl(String url, long workspaceId) {
        if (!url.endsWith("/")) {
            url = url + "/";
        }

        return url + WORKSPACE_PATH + "/" + workspaceId;
    }

}