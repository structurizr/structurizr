package com.structurizr.mcp;

import com.structurizr.api.AdminApiClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("server-delete")
public class ServerDeleteTools extends AbstractServerTools {

    private static final Log log = LogFactory.getLog(ServerDeleteTools.class);

    public ServerDeleteTools() {
        log.info("Registering");
    }

    @McpTool(
            name = "deleteWorkspace",
            description = "Deletes a workspace from a Structurizr server",
            title = "Delete a workspace from a Structurizr server"
    )
    public boolean deleteWorkspace(
            @McpToolParam(description = "URL", required = true) String url,
            @McpToolParam(description = "Workspace ID", required = true) long workspaceId,
            @McpToolParam(description = "API key", required = false) String apiKey
    ) throws Exception {

        log.info("Deleting workspace " + workspaceId + " from server at " + url);

        String apiUrl = apiUrl(url);

        AdminApiClient adminApiClient = new AdminApiClient(apiUrl, apiKey);
        return adminApiClient.deleteWorkspace(workspaceId);
    }

}