package com.structurizr.mcp;

import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.util.WorkspaceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("parse")
public class ParseTools extends AbstractService {

    private static final Log log = LogFactory.getLog(ParseTools.class);

    public ParseTools() {
        log.info("Registering");
    }

    @McpTool(description = "Parses a Structurizr DSL workspace")
    public String parse(
            @McpToolParam(description = "DSL", required = true) String dsl
    ) throws Exception {
        log.info("Validating DSL workspace");

        try {
            StructurizrDslParser parser = createStructurizrDslParser();
            parser.parse(dsl);

            return WorkspaceUtils.toJson(parser.getWorkspace(), false);
        } catch (Exception e) {
            log.error(e);
            return e.getMessage();
        }
    }

}