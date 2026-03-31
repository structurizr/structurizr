package com.structurizr.mcp;

import com.structurizr.api.AdminApiClient;
import com.structurizr.api.WorkspaceMetadata;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("server-create")
public class ServerCreateTools extends AbstractServerTools {

    private static final Log log = LogFactory.getLog(ServerCreateTools.class);

    public ServerCreateTools() {
        log.info("Registering");
    }

    @McpTool(
            name = "createWorkspace",
            description = "Creates a workspace on a Structurizr server",
            title = "Create a workspace on a Structurizr server"
    )
    public WorkspaceMetadata createWorkspace(
            @McpToolParam(description = "URL", required = true) String url,
            @McpToolParam(description = "API key", required = false) String apiKey
    ) throws Exception {

        log.info("Creating workspace on server at " + url);

        String apiUrl = apiUrl(url);

        AdminApiClient adminApiClient = new AdminApiClient(apiUrl, apiKey);
        return adminApiClient.createWorkspace();
    }

}