package com.structurizr.mcp;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.inspection.DefaultInspector;
import com.structurizr.inspection.Inspector;
import com.structurizr.inspection.Violation;
import com.structurizr.util.WorkspaceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Profile("dsl")
public class DslTools {

    private static final Log log = LogFactory.getLog(DslTools.class);

    public DslTools() {
        log.info("Registering");
    }

    @McpTool(description = "Validates a Structurizr DSL workspace")
    public String validate(
            @McpToolParam(description = "DSL", required = true) String dsl
    ) {
        log.info("Validating DSL");

        try {
            StructurizrDslParser parser = createStructurizrDslParser();
            parser.parse(dsl);

            return "OK";
        } catch (Exception e) {
            log.error(e);
            return e.getMessage();
        }
    }

    @McpTool(description = "Parses a Structurizr DSL workspace")
    public String parse(
            @McpToolParam(description = "DSL", required = true) String dsl
    ) {
        log.info("Parsing DSL");

        try {
            StructurizrDslParser parser = createStructurizrDslParser();
            parser.parse(dsl);

            return WorkspaceUtils.toJson(parser.getWorkspace(), false);
        } catch (Exception e) {
            log.error(e);
            return e.getMessage();
        }
    }

    @McpTool(description = "Inspects a Structurizr DSL workspace")
    public List<String> inspect(
            @McpToolParam(description = "DSL", required = true) String dsl
    ) throws Exception {
        log.info("Inspecting DSL workspace");

        StructurizrDslParser parser = createStructurizrDslParser();
        parser.parse(dsl);
        Workspace workspace = parser.getWorkspace();

        Inspector inspector = new DefaultInspector(workspace);

        List<String> inspections = new ArrayList<>();
        for (Violation violation : inspector.getViolations()) {
            inspections.add(violation.getSeverity() + ": " + violation.getMessage());
        }

        return inspections;
    }

    protected StructurizrDslParser createStructurizrDslParser() {
        StructurizrDslParser parser = new StructurizrDslParser();

        parser.getFeatures().configure(com.structurizr.dsl.Features.ENVIRONMENT, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.FILE_SYSTEM, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.PLUGINS, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.SCRIPTS, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.COMPONENT_FINDER, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.DOCUMENTATION, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.DECISIONS, false);

        return parser;
    }

}