package com.structurizr.mcp;

import com.structurizr.export.Diagram;
import com.structurizr.export.mermaid.MermaidDiagramExporter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("mermaid")
public class MermaidTools extends AbstractExportTools {

    private static final Log log = LogFactory.getLog(MermaidTools.class);

    public MermaidTools() {
        log.info("Registering");
    }

    @McpTool(
            name = "export-mermaid",
            description = "Exports Structurizr views to Mermaid",
            title = "Exports Structurizr views to Mermaid"
    )
    public Diagram exportMermaid(
            @McpToolParam(description = "DSL", required = true) String dsl,
            @McpToolParam(description = "View key", required = true) String viewKey
    ) {
        log.info("Exporting to Mermaid");

        return export(dsl, viewKey, new MermaidDiagramExporter());
    }

}