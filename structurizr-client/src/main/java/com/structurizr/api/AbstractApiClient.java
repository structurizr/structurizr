package com.structurizr.api;

import com.structurizr.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpResponse;

public abstract class AbstractApiClient {

    private static final Log log = LogFactory.getLog(AbstractApiClient.class);

    protected static final String VERSION = Package.getPackage("com.structurizr.api").getImplementationVersion();
    protected static final String STRUCTURIZR_FOR_JAVA_AGENT = "structurizr-java/" + VERSION;

    protected static final String WORKSPACE_PATH = "/workspace";

    protected final String url;
    protected final String apiKey;

    protected String agent = STRUCTURIZR_FOR_JAVA_AGENT;

    protected AbstractApiClient(String url, String apiKey) {
        if (StringUtils.isNullOrEmpty(url)) {
            throw new IllegalArgumentException("The API URL must not be null or empty.");
        }

        if (url.endsWith("/")) {
            this.url = url.substring(0, url.length() - 1);
        } else {
            this.url = url;
        }

        this.apiKey = apiKey;
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

    protected void addHeaders(HttpUriRequestBase httpRequest, String contentType) {
        String httpMethod = httpRequest.getMethod();

        httpRequest.addHeader(HttpHeaders.USER_AGENT, agent);

        if (!StringUtils.isNullOrEmpty(apiKey)) {
            httpRequest.addHeader(HttpHeaders.AUTHORIZATION, apiKey);
        }

        if (httpMethod.equals("PUT")) {
            httpRequest.addHeader(HttpHeaders.CONTENT_TYPE, contentType);
        }
    }

    protected void debugRequest(HttpUriRequestBase httpRequest, String content) {
        if (log.isDebugEnabled()) {
            log.debug("Request");
            log.debug("HTTP method: " + httpRequest.getMethod());
            log.debug("Path: " + httpRequest.getPath());
            Header[] headers = httpRequest.getHeaders();
            for (Header header : headers) {
                log.debug("Header: " + header.getName() + "=" + header.getValue());
            }
            if (content != null) {
                log.debug("---Start content---");
                log.debug(content);
                log.debug("---End content---");
            }
        }
    }

    protected void debugResponse(HttpResponse response, String content) {
        log.debug("Response");
        log.debug("HTTP status code: " + response.getCode());
        if (content != null) {
            log.debug("---Start content---");
            log.debug(content);
            log.debug("---End content---");
        }
    }

    protected void checkResponseIsJson(String json) throws StructurizrClientException{
        if (!json.startsWith("{")) {
            log.error(json);
            throw new StructurizrClientException("The response is not a JSON object - is the API URL correct?");
        }
    }

}