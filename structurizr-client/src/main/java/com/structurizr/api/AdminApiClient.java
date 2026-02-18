package com.structurizr.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * A client for the Structurizr Admin API.
 */
public class AdminApiClient extends AbstractApiClient {

    private static final Log log = LogFactory.getLog(AdminApiClient.class);

    /**
     * Creates a new admin API client.
     *
     * @param url       the URL of your Structurizr instance
     * @param apiKey    the admin API key
     */
    public AdminApiClient(String url, String apiKey) {
        super(url, apiKey);
    }

    /**
     * Gets a list of all workspaces.
     *
     * @return  a List of WorkspaceMetadata objects
     * @throws StructurizrClientException   if an error occurs
     */
    public List<WorkspaceMetadata> getWorkspaces() throws StructurizrClientException {
        try (CloseableHttpClient httpClient = HttpClients.createSystem()) {
            log.debug("Getting workspaces");

            HttpUriRequestBase httpRequest;

            httpRequest = new HttpGet(url + WORKSPACE_PATH);

            addHeaders(httpRequest, "");
            debugRequest(httpRequest, null);

            HttpClientResult result = httpClient.execute(httpRequest, response -> {
                String json = EntityUtils.toString(response.getEntity());
                debugResponse(response, json);

                return new HttpClientResult(response.getCode() == HttpStatus.SC_OK, json);
            });

            checkResponseIsJson(result.getContent());

            if (result.isSuccess()) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                Workspaces workspaces = objectMapper.readValue(result.getContent(), Workspaces.class);
                return workspaces.getWorkspaces();
            } else {
                ApiResponse apiResponse = ApiResponse.parse(result.getContent());
                throw new StructurizrClientException(apiResponse.getMessage());
            }

        } catch (Exception e) {
            log.error(e);
            throw new StructurizrClientException(e);
        }
    }

    /**
     * Creates a new workspace.
     *
     * @return  a WorkspaceMetadata object representing the new workspace
     * @throws StructurizrClientException   if an error occurs
     */
    public WorkspaceMetadata createWorkspace() throws StructurizrClientException {
        try (CloseableHttpClient httpClient = HttpClients.createSystem()) {
            log.debug("Creating workspace");

            HttpUriRequestBase httpRequest = new HttpPost(url + WORKSPACE_PATH);

            addHeaders(httpRequest, "");
            debugRequest(httpRequest, null);

            HttpClientResult result = httpClient.execute(httpRequest, response -> {
                String json = EntityUtils.toString(response.getEntity());
                debugResponse(response, json);

                return new HttpClientResult(response.getCode() == HttpStatus.SC_OK, json);
            });

            checkResponseIsJson(result.getContent());
            ApiResponse apiResponse = ApiResponse.parse(result.getContent());

            if (result.isSuccess()) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return objectMapper.readValue(result.getContent(), WorkspaceMetadata.class);
            } else {
                throw new StructurizrClientException(apiResponse.getMessage());
            }
        } catch (Exception e) {
            log.error(e);
            throw new StructurizrClientException(e);
        }
    }

    /**
     * Deletes a workspace.
     *
     * @param workspaceId       the ID of the workspace to delete
     * @return  true if successful, false otherwise
     * @throws StructurizrClientException   if an error occurs
     */
    public boolean deleteWorkspace(long workspaceId) throws StructurizrClientException {
        try (CloseableHttpClient httpClient = HttpClients.createSystem()) {
            log.debug("Deleting workspace " + workspaceId);

            HttpUriRequestBase httpRequest = new HttpDelete(url + WORKSPACE_PATH + "/" + workspaceId);

            addHeaders(httpRequest, "");
            debugRequest(httpRequest, null);

            HttpClientResult result = httpClient.execute(httpRequest, response -> {
                String json = EntityUtils.toString(response.getEntity());
                debugResponse(response, json);

                return new HttpClientResult(response.getCode() == HttpStatus.SC_OK, json);
            });

            checkResponseIsJson(result.getContent());
            ApiResponse apiResponse = ApiResponse.parse(result.getContent());

            if (result.isSuccess()) {
                return apiResponse.isSuccess();
            } else {
                throw new StructurizrClientException(apiResponse.getMessage());
            }
        } catch (Exception e) {
            log.error(e);
            throw new StructurizrClientException(e);
        }
    }

}