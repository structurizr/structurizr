package com.structurizr.mcp;

import com.structurizr.Workspace;
import com.structurizr.api.AdminApiClient;
import com.structurizr.api.WorkspaceApiClient;
import com.structurizr.api.WorkspaceMetadata;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Profile("server")
public class ServerTools {

    private static final String API_PATH = "api";
    private static final String WORKSPACE_PATH = "workspace";

    private static final Log log = LogFactory.getLog(ServerTools.class);

    public ServerTools() {
        log.info("Registering");
    }

    @McpTool(
            name = "getWorkspace",
            description = "Gets a single workspace from a Structurizr server",
            title = "Get workspace from a Structurizr server"
    )
    public Workspace getWorkspace(
            @McpToolParam(description = "URL", required = true) String url,
            @McpToolParam(description = "Workspace ID", required = true) long workspaceId,
            @McpToolParam(description = "API key", required = false) String apiKey
    ) throws Exception {
        if (!url.endsWith("/")) {
            url = url + "/";
        }

        log.info("Getting workspace " + workspaceId + " from " + url);

        String apiUrl = url + API_PATH;
        String workspaceApi = url + WORKSPACE_PATH + "/" + workspaceId;

        WorkspaceApiClient workspaceApiClient = new WorkspaceApiClient(apiUrl, workspaceId, apiKey);
        Workspace workspace = workspaceApiClient.getWorkspace();
        workspace.addProperty("structurizr.url", workspaceApi);

        return workspace;
    }

    @McpTool(
            name = "getWorkspaces",
            description = "Gets all workspaces from a Structurizr server",
            title = "Get all workspaces from a Structurizr server"
    )
    public Collection<Workspace> getWorkspaces(
            @McpToolParam(description = "URL", required = true) String url,
            @McpToolParam(description = "API key", required = false) String apiKey
    ) throws Exception {
        List<Workspace> workspaces = new ArrayList<>();

        if (!url.endsWith("/")) {
            url = url + "/";
        }

        log.info("Getting all workspaces from " + url);

        String apiUrl = url + API_PATH;

        AdminApiClient adminApiClient = new AdminApiClient(apiUrl, apiKey);
        List<WorkspaceMetadata> workspaceMetadataList = adminApiClient.getWorkspaces();

        for (WorkspaceMetadata workspaceMetadata : workspaceMetadataList) {
            WorkspaceApiClient workspaceApiClient = new WorkspaceApiClient(apiUrl, workspaceMetadata.getId(), apiKey);
            Workspace workspace = workspaceApiClient.getWorkspace();
            workspace.addProperty("structurizr.url", workspaceMetadata.getPrivateUrl());

            workspaces.add(workspace);
        }

        return workspaces;
    }

}