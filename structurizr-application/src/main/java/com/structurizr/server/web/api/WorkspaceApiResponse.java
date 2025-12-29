package com.structurizr.server.web.api;

public final class WorkspaceApiResponse {

    private long id;
    private String name;
    private String description;
    private String apiKey;
    private String apiSecret;
    private String privateUrl;
    private String publicUrl = "";
    private String shareableUrl = "";

    WorkspaceApiResponse() {
    }

    public long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    public String getApiKey() {
        return apiKey;
    }

    void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public String getPrivateUrl() {
        return privateUrl;
    }

    void setPrivateUrl(String privateUrl) {
        this.privateUrl = privateUrl;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    public String getShareableUrl() {
        return shareableUrl;
    }

    void setShareableUrl(String shareableUrl) {
        this.shareableUrl = shareableUrl;
    }

}