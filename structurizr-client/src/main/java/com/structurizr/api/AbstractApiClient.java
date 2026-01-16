package com.structurizr.api;

import com.structurizr.util.StringUtils;

public abstract class AbstractApiClient {

    protected static final String VERSION = Package.getPackage("com.structurizr.api").getImplementationVersion();
    protected static final String STRUCTURIZR_FOR_JAVA_AGENT = "structurizr-java/" + VERSION;

    protected static final String WORKSPACE_PATH = "/workspace";

    protected final String url;
    protected String agent = STRUCTURIZR_FOR_JAVA_AGENT;

    protected AbstractApiClient(String url) {
        if (StringUtils.isNullOrEmpty(url)) {
            throw new IllegalArgumentException("The API URL must not be null or empty.");
        }

        if (url.endsWith("/")) {
            this.url = url.substring(0, url.length() - 1);
        } else {
            this.url = url;
        }
    }

    String getUrl() {
        return url;
    }

    /**
     * Gets the agent string used to identify this client instance.
     *
     * @return  "structurizr-java/{version}", unless overridden
     */
    public String getAgent() {
        return agent;
    }

    /**
     * Sets the agent string used to identify this client instance.
     *
     * @param agent     the agent string
     */
    public void setAgent(String agent) {
        if (StringUtils.isNullOrEmpty(agent)) {
            throw new IllegalArgumentException("An agent must be provided.");
        }

        this.agent = agent.trim();
    }

}