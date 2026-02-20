package com.structurizr.mcp;

import com.structurizr.Workspace;
import com.structurizr.api.AdminApiClient;
import com.structurizr.api.WorkspaceApiClient;
import com.structurizr.api.WorkspaceMetadata;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.inspection.DefaultInspector;
import com.structurizr.inspection.Inspector;
import com.structurizr.inspection.Violation;
import com.structurizr.util.WorkspaceUtils;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service
public class StructurizrMcpService {

    private static final String API_PATH = "api";
    private static final String WORKSPACE_PATH = "workspace";

    public StructurizrMcpService() {
    }



    @McpTool(description = "Gets a Structurizr software architecture JSON workspace from a file")
    public Workspace getJsonWorkspaceFromFile(
            @McpToolParam(description = "Filename", required = true) String filename
    ) throws Exception {
        return WorkspaceUtils.loadWorkspaceFromJson(new File(filename));
    }

    @McpTool(description = "Inspects a Structurizr software architecture JSON workspace from a file")
    public List<String> inspectJsonWorkspaceFromFile(
            @McpToolParam(description = "Filename", required = true) String filename
    ) throws Exception {
        Workspace workspace = getJsonWorkspaceFromFile(filename);
        return inspect(workspace);
    }



    @McpTool(description = "Parses a Structurizr DSL software architecture workspace from a file")
    public Workspace parseDslWorkspaceFromFile(
            @McpToolParam(description = "Filename", required = true) String filename
    ) throws Exception {
        StructurizrDslParser parser = new StructurizrDslParser();
        parser.getHttpClient().allow(".*");
        parser.parse(new File(filename));
        return parser.getWorkspace();
    }

    @McpTool(description = "Inspects a Structurizr DSL software architecture workspace from a file")
    public List<String> inspectDslWorkspaceFromFile(
            @McpToolParam(description = "Filename", required = true) String filename
    ) throws Exception {
        Workspace workspace = parseDslWorkspaceFromFile(filename);
        return inspect(workspace);
    }



    @McpTool(description = "Gets a single software architecture workspace from a Structurizr server")
    public Workspace getWorkspaceFromStructurizrServer(
            @McpToolParam(description = "URL", required = true) String url,
            @McpToolParam(description = "Workspace ID", required = true) long workspaceId,
            @McpToolParam(description = "API key", required = false) String apiKey
    ) throws Exception {
        if (!url.endsWith("/")) {
            url = url + "/";
        }

        String apiUrl = url + API_PATH;
        String workspaceApi = url + WORKSPACE_PATH + "/" + workspaceId;

        WorkspaceApiClient workspaceApiClient = new WorkspaceApiClient(apiUrl, workspaceId, apiKey);
        Workspace workspace = workspaceApiClient.getWorkspace();
        workspace.addProperty("structurizr.url", workspaceApi);

        return workspace;
    }

    @McpTool(description = "Inspects a single software architecture workspace from a Structurizr server")
    public List<String> inspectWorkspaceFromStructurizrServer(
            @McpToolParam(description = "URL", required = true) String url,
            @McpToolParam(description = "Workspace ID", required = true) long workspaceId,
            @McpToolParam(description = "API key", required = false) String apiKey
    ) throws Exception {
        Workspace workspace = getWorkspaceFromStructurizrServer(url, workspaceId, apiKey);
        return inspect(workspace);
    }



    @McpTool(description = "Gets all software architecture workspaces from a Structurizr server")
    public Collection<Workspace> getWorkspacesFromStructurizrServer(
            @McpToolParam(description = "URL", required = true) String url,
            @McpToolParam(description = "API key", required = false) String apiKey
    ) throws Exception {
        List<Workspace> workspaces = new ArrayList<>();

        if (!url.endsWith("/")) {
            url = url + "/";
        }

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

    private List<String> inspect(Workspace workspace) throws Exception {
        Inspector inspector = new DefaultInspector(workspace);

        List<String> inspections = new ArrayList<>();
        for (Violation violation : inspector.getViolations()) {
            inspections.add(violation.getSeverity() + ": " + violation.getMessage());
        }

        return inspections;
    }

}