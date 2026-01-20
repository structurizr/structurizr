package com.structurizr.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurizr.Workspace;
import com.structurizr.encryption.EncryptedWorkspace;
import com.structurizr.encryption.EncryptionLocation;
import com.structurizr.encryption.EncryptionStrategy;
import com.structurizr.io.json.EncryptedJsonReader;
import com.structurizr.io.json.EncryptedJsonWriter;
import com.structurizr.io.json.JsonReader;
import com.structurizr.io.json.JsonWriter;
import com.structurizr.model.IdGenerator;
import com.structurizr.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A client for the Structurizr workspace API that allows you to get and put Structurizr workspaces in a JSON format.
 */
public class WorkspaceApiClient extends AbstractApiClient {

    private static final Log log = LogFactory.getLog(WorkspaceApiClient.class);
    private static final String MAIN_BRANCH = "main";

    private String user;

    private final long workspaceId;
    private final String apiKey;
    private String branch = "";

    private EncryptionStrategy encryptionStrategy;

    private IdGenerator idGenerator = null;
    private boolean mergeFromRemote = true;
    private File workspaceArchiveLocation = new File(".");

    /**
     * Creates a new Structurizr client with the specified API URL, workspace ID, and API key.
     *
     * @param url           the URL of your Structurizr server
     * @param workspaceId   the workspace ID
     * @param apiKey        the API key of the workspace
     */
    public WorkspaceApiClient(String url, long workspaceId, String apiKey) {
        super(url);

        if (workspaceId <= 0) {
            throw new IllegalArgumentException("The workspace ID must be a positive integer");
        }

        this.workspaceId = workspaceId;
        this.apiKey = apiKey;
    }

    /**
     * Sets the ID generator to use when parsing a JSON workspace definition.
     *
     * @param idGenerator   an IdGenerator implementation
     */
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    /**
     * Gets the location where a copy of the workspace is archived when it is retrieved from the server.
     *
     * @return a File instance representing a directory, or null if this client instance is not archiving
     */
    public File getWorkspaceArchiveLocation() {
        return this.workspaceArchiveLocation;
    }

    /**
     * Sets the location where a copy of the workspace will be archived whenever it is retrieved from
     * the server. Set this to null if you don't want archiving.
     *
     * @param workspaceArchiveLocation a File instance representing a directory, or null if
     *                                 you don't want archiving
     */
    public void setWorkspaceArchiveLocation(File workspaceArchiveLocation) {
        this.workspaceArchiveLocation = workspaceArchiveLocation;
    }

    /**
     * Sets the encryption strategy for use when getting or putting workspaces.
     *
     * @param encryptionStrategy an EncryptionStrategy implementation
     */
    public void setEncryptionStrategy(EncryptionStrategy encryptionStrategy) {
        this.encryptionStrategy = encryptionStrategy;
    }

    /**
     * Specifies whether the layout of diagrams from a remote workspace should be retained when putting
     * a new version of the workspace.
     *
     * @param mergeFromRemote   true if layout information should be merged from the remote workspace, false otherwise
     */
    public void setMergeFromRemote(boolean mergeFromRemote) {
        this.mergeFromRemote = mergeFromRemote;
    }

    /**
     * Locks the workspace.
     *
     * @return                  true if the workspace could be locked, false otherwise
     * @throws StructurizrClientException   if there are problems related to the network, authorization, etc
     */
    public boolean lockWorkspace() throws StructurizrClientException {
        return manageLockForWorkspace(true);
    }

    /**
     * Unlocks the workspace.
     *
     * @return                  true if the workspace could be unlocked, false otherwise
     * @throws StructurizrClientException   if there are problems related to the network, authorization, etc
     */
    public boolean unlockWorkspace() throws StructurizrClientException {
        return manageLockForWorkspace(false);
    }

    private boolean manageLockForWorkspace(boolean lock) throws StructurizrClientException {
        try (CloseableHttpClient httpClient = HttpClients.createSystem()) {
            HttpUriRequestBase httpRequest;

            if (lock) {
                log.info("Locking workspace with ID " + workspaceId);
                httpRequest = new HttpPut(url + WORKSPACE_PATH + "/" + workspaceId + "/lock?user=" + getUser() + "&agent=" + agent);
            } else {
                log.info("Unlocking workspace with ID " + workspaceId);
                httpRequest = new HttpDelete(url + WORKSPACE_PATH + "/" + workspaceId + "/lock?user=" + getUser() + "&agent=" + agent);
            }

            addHeaders(httpRequest, "");
            debugRequest(httpRequest, null);

            try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
                String json = EntityUtils.toString(response.getEntity());
                debugResponse(response, json);

                ApiResponse apiResponse = ApiResponse.parse(json);

                if (response.getCode() == HttpStatus.SC_OK) {
                    return apiResponse.isSuccess();
                } else {
                    throw new StructurizrClientException(apiResponse.getMessage());
                }
            }
        } catch (Exception e) {
            log.error(e);
            throw new StructurizrClientException(e);
        }
    }

    /**
     * Gets the workspace.
     *
     * @return a Workspace instance
     * @throws StructurizrClientException   if there are problems related to the network, authorization, JSON deserialization, etc
     */
    public Workspace getWorkspace() throws StructurizrClientException {
        String json = getWorkspaceAsJson();
        checkResponseIsJson(json);

        try {
            if (encryptionStrategy == null) {
                if (json.contains("\"encryptionStrategy\"") && json.contains("\"ciphertext\"")) {
                    log.warn("The JSON may contain a client-side encrypted workspace, but no passphrase has been specified");
                }

                JsonReader jsonReader = new JsonReader();
                jsonReader.setIdGenerator(idGenerator);
                return jsonReader.read(new StringReader(json));
            } else {
                EncryptedWorkspace encryptedWorkspace = new EncryptedJsonReader().read(new StringReader(json));

                if (encryptedWorkspace.getEncryptionStrategy() != null) {
                    encryptedWorkspace.getEncryptionStrategy().setPassphrase(encryptionStrategy.getPassphrase());
                    return encryptedWorkspace.getWorkspace();
                } else {
                    // this workspace isn't encrypted, even though the client has an encryption strategy set
                    JsonReader jsonReader = new JsonReader();
                    jsonReader.setIdGenerator(idGenerator);
                    return jsonReader.read(new StringReader(json));
                }
            }
        } catch (Exception e) {
            log.error(e);
            throw new StructurizrClientException(e);
        }
    }

    /**
     * Gets the workspace, as a JSON string.
     *
     * @return a JSON string
     * @throws StructurizrClientException   if there are problems related to the network, authorization, JSON deserialization, etc
     */
    public String getWorkspaceAsJson() throws StructurizrClientException {
        if (workspaceId <= 0) {
            throw new IllegalArgumentException("The workspace ID must be a positive integer");
        }

        try (CloseableHttpClient httpClient = HttpClients.createSystem()) {
            log.info("Getting workspace with ID " + workspaceId);

            HttpGet httpGet;
            if (StringUtils.isNullOrEmpty(branch) || branch.equalsIgnoreCase(MAIN_BRANCH)) {
                httpGet = new HttpGet(url + WORKSPACE_PATH + "/" + workspaceId);
            } else {
                httpGet = new HttpGet(url + WORKSPACE_PATH + "/" + workspaceId + "/branch/" + branch);
            }

            addHeaders(httpGet, "");
            debugRequest(httpGet, null);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String json = EntityUtils.toString(response.getEntity());
                debugResponse(response, json);

                if (response.getCode() == HttpStatus.SC_OK) {
                    archiveWorkspace(workspaceId, json);

                    return json;
                } else {
                    ApiResponse apiResponse = ApiResponse.parse(json);
                    throw new StructurizrClientException(apiResponse.getMessage());
                }
            }
        } catch (Exception e) {
            log.error(e);
            throw new StructurizrClientException(e);
        }
    }

    /**
     * Updates the workspace.
     *
     * @param workspace   the workspace instance to update
     * @throws StructurizrClientException   if there are problems related to the network, authorization, JSON serialization, etc
     */
    public void putWorkspace(Workspace workspace) throws StructurizrClientException {
        if (workspace == null) {
            throw new IllegalArgumentException("The workspace must not be null");
        }

        try (CloseableHttpClient httpClient = HttpClients.createSystem()) {
            if (mergeFromRemote) {
                Workspace remoteWorkspace = getWorkspace();
                if (remoteWorkspace != null) {
                    workspace.getViews().copyLayoutInformationFrom(remoteWorkspace.getViews());
                    workspace.getViews().getConfiguration().copyConfigurationFrom(remoteWorkspace.getViews().getConfiguration());
                }
            }

            workspace.setId(workspaceId);
            workspace.setThumbnail(null);
            workspace.setLastModifiedDate(new Date());
            workspace.setLastModifiedAgent(agent);
            workspace.setLastModifiedUser(getUser());

            HttpPut httpPut;
            if (StringUtils.isNullOrEmpty(branch) || branch.equalsIgnoreCase(MAIN_BRANCH)) {
                httpPut = new HttpPut(url + WORKSPACE_PATH + "/" + workspaceId);
            } else {
                httpPut = new HttpPut(url + WORKSPACE_PATH + "/" + workspaceId + "/branch/" + branch);
            }

            StringWriter stringWriter = new StringWriter();
            if (encryptionStrategy == null) {
                JsonWriter jsonWriter = new JsonWriter(false);
                jsonWriter.write(workspace, stringWriter);
            } else {
                EncryptedWorkspace encryptedWorkspace = new EncryptedWorkspace(workspace, encryptionStrategy);
                encryptionStrategy.setLocation(EncryptionLocation.Client);
                EncryptedJsonWriter jsonWriter = new EncryptedJsonWriter(false);
                jsonWriter.write(encryptedWorkspace, stringWriter);
            }

            StringEntity stringEntity = new StringEntity(stringWriter.toString(), ContentType.APPLICATION_JSON);
            httpPut.setEntity(stringEntity);
            addHeaders(httpPut, ContentType.APPLICATION_JSON.toString());

            debugRequest(httpPut, EntityUtils.toString(stringEntity));

            log.info("Putting workspace with ID " + workspaceId);
            try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
                String json = EntityUtils.toString(response.getEntity());
                debugResponse(response, json);

                if (response.getCode() != HttpStatus.SC_OK) {
                    ApiResponse apiResponse = ApiResponse.parse(json);
                    throw new StructurizrClientException(apiResponse.getMessage());
                }
            }
        } catch (Exception e) {
            log.error(e);
            throw new StructurizrClientException(e);
        }
    }

    private void debugRequest(HttpUriRequestBase httpRequest, String content) {
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

    private void debugResponse(CloseableHttpResponse response, String content) {
        log.debug("Response");
        log.debug("HTTP status code: " + response.getCode());
        if (content != null) {
            log.debug("---Start content---");
            log.debug(content);
            log.debug("---End content---");
        }
    }

    private void addHeaders(HttpUriRequestBase httpRequest, String contentType) {
        String httpMethod = httpRequest.getMethod();

        httpRequest.addHeader(HttpHeaders.USER_AGENT, agent);

        if (!StringUtils.isNullOrEmpty(apiKey)) {
            httpRequest.addHeader(HttpHeaders.AUTHORIZATION, apiKey);
        }

        if (httpMethod.equals("PUT")) {
            httpRequest.addHeader(HttpHeaders.CONTENT_TYPE, contentType);
        }
    }

    private void archiveWorkspace(long workspaceId, String json) {
        if (this.workspaceArchiveLocation == null) {
            return;
        }

        File archiveFile = new File(workspaceArchiveLocation, createArchiveFileName(workspaceId));
        try (FileWriter fileWriter = new FileWriter(archiveFile)) {
            fileWriter.write(json);
            fileWriter.flush();

            debugArchivedWorkspaceLocation(archiveFile);
        } catch (Exception e) {
            log.warn("Could not archive JSON to " + archiveFile.getAbsolutePath());
        }
    }

    private void debugArchivedWorkspaceLocation(File archiveFile) {
        if (log.isDebugEnabled()) {
            try {
                log.debug("Workspace from server archived to " + archiveFile.getCanonicalPath());
            } catch (IOException ioe) {
                log.debug("Workspace from server archived to " + archiveFile.getAbsolutePath());
            }
        }
    }

    private String createArchiveFileName(long workspaceId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "structurizr-" + workspaceId + "-" + (StringUtils.isNullOrEmpty(branch) ? "" : (branch + "-")) + sdf.format(new Date()) + ".json";
    }

    public void setUser(String user) {
        this.user = user;
    }

    private String getUser() {
        if (!StringUtils.isNullOrEmpty(user)) {
            return user;
        } else {
            String username = System.getProperty("user.name");

            if (username.contains("@")) {
                return username;
            } else {
                String hostname = null;
                try {
                    hostname = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException uhe) {
                    // ignore
                }

                return username + (!StringUtils.isNullOrEmpty(hostname) ? "@" + hostname : "");
            }
        }
    }

    /**
     * Gets the list of branches.
     *
     * @return                  an array of branch names (String)
     * @throws StructurizrClientException   if there are problems related to the network, authorization, etc
     */
    public String[] getBranches() throws StructurizrClientException {
        try (CloseableHttpClient httpClient = HttpClients.createSystem()) {
            HttpUriRequestBase httpRequest;

            httpRequest = new HttpGet(url + WORKSPACE_PATH + "/" + workspaceId + "/branch");

            addHeaders(httpRequest, "");
            debugRequest(httpRequest, null);

            try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
                String json = EntityUtils.toString(response.getEntity());
                checkResponseIsJson(json);
                debugResponse(response, json);

                if (response.getCode() == HttpStatus.SC_OK) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    return objectMapper.readValue(json, String[].class);
                } else {
                    ApiResponse apiResponse = ApiResponse.parse(json);
                    throw new StructurizrClientException(apiResponse.getMessage());
                }
            }
        } catch (Exception e) {
            log.error(e);
            throw new StructurizrClientException(e);
        }
    }

    /**
     * Deletes a given branch.
     *
     * @return                  true if the branch was deleted, false otherwise
     * @throws StructurizrClientException   if there are problems related to the network, authorization, etc
     */
    public boolean deleteBranch(String branch) throws StructurizrClientException {
        if (workspaceId <= 0) {
            throw new IllegalArgumentException("The workspace ID must be a positive integer");
        }

        try (CloseableHttpClient httpClient = HttpClients.createSystem()) {
            HttpUriRequestBase httpRequest;

            httpRequest = new HttpDelete(url + WORKSPACE_PATH + "/" + workspaceId + "/branch/" + branch);

            addHeaders(httpRequest, "");
            debugRequest(httpRequest, null);

            try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
                String json = EntityUtils.toString(response.getEntity());
                checkResponseIsJson(json);
                debugResponse(response, json);

                if (response.getCode() == HttpStatus.SC_OK) {
                    ApiResponse apiResponse = ApiResponse.parse(json);
                    return apiResponse.isSuccess();
                } else {
                    ApiResponse apiResponse = ApiResponse.parse(json);
                    throw new StructurizrClientException(apiResponse.getMessage());
                }
            }
        } catch (Exception e) {
            log.error(e);
            throw new StructurizrClientException(e);
        }
    }

    private void checkResponseIsJson(String json) throws StructurizrClientException{
        if (!json.startsWith("{")) {
            log.error(json);
            throw new StructurizrClientException("The response is not a JSON object - is the API URL correct?");
        }
    }

}