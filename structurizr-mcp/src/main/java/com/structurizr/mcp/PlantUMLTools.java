package com.structurizr.mcp;

import com.structurizr.export.Diagram;
import com.structurizr.export.plantuml.C4PlantUMLExporter;
import com.structurizr.export.plantuml.StructurizrPlantUMLExporter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("plantuml")
public class PlantUMLTools extends AbstractExportTools {

    private static final Log log = LogFactory.getLog(PlantUMLTools.class);

    public PlantUMLTools() {
        log.info("Registering");
    }

    @McpTool(
            name = "export-plantuml",
            description = "Exports Structurizr views to PlantUML",
            title = "Exports Structurizr views to PlantUML"
    )
    public Diagram exportPlantUML(
            @McpToolParam(description = "DSL", required = true) String dsl,
            @McpToolParam(description = "View key", required = true) String viewKey
    ) {
        log.info("Exporting to PlantUML");

        return export(dsl, viewKey, new StructurizrPlantUMLExporter());
    }

    @McpTool(
            name = "export-c4plantuml",
            description = "Exports Structurizr views to C4-PlantUML",
            title = "Exports Structurizr views to C4-PlantUML"
    )
    public Diagram exportC4PlantUML(
            @McpToolParam(description = "DSL", required = true) String dsl,
            @McpToolParam(description = "View key", required = true) String viewKey
    ) {
        log.info("Exporting to C4-PlantUML");

        return export(dsl, viewKey, new C4PlantUMLExporter());
    }

}