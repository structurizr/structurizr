package com.structurizr.mcp;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.inspection.DefaultInspector;
import com.structurizr.inspection.Inspector;
import com.structurizr.inspection.Violation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Profile("inspect")
public class InspectDslService extends AbstractService {

    private static final Log log = LogFactory.getLog(InspectDslService.class);

    public InspectDslService() {
        log.info("Registering tools");
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

}