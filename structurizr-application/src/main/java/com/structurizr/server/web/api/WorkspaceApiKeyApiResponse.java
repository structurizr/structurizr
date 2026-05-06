package com.structurizr.server.web.api;

class WorkspaceApiKeyApiResponse {

    private final long id;
    private final String apiKey;

    WorkspaceApiKeyApiResponse(long id, String apiKey) {
        this.id = id;
        this.apiKey = apiKey;
    }

    public long getId() {
        return id;
    }

    public String getApiKey() {
        return apiKey;
    }

}