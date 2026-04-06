package com.structurizr.mcp;

import com.structurizr.api.WorkspaceApiClient;
import com.structurizr.dsl.StructurizrDslParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("server-update")
public class ServerUpdateTools extends AbstractServerTools {

    private static final Log log = LogFactory.getLog(ServerUpdateTools.class);

    public ServerUpdateTools() {
        log.info("Registering");
    }

    @McpTool(
            name = "updateWorkspace",
            description = "Updates a workspace on a Structurizr server",
            title = "Update a workspace on a Structurizr server"
    )
    public boolean updateWorkspace(
            @McpToolParam(description = "URL", required = true) String url,
            @McpToolParam(description = "Workspace ID", required = true) long workspaceId,
            @McpToolParam(description = "API key", required = false) String apiKey,
            @McpToolParam(description = "DSL", required = true) String dsl
    ) throws Exception {

        log.info("Updating workspace " + workspaceId + " on server at " + url);

        String apiUrl = apiUrl(url);

        StructurizrDslParser parser = createStructurizrDslParser();
        parser.parse(dsl);

        WorkspaceApiClient workspaceApiClient = new WorkspaceApiClient(apiUrl, workspaceId, apiKey);
        workspaceApiClient.putWorkspace(parser.getWorkspace());

        return true;
    }

}