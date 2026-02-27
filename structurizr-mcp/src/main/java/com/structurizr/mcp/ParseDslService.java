package com.structurizr.mcp;

import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.util.WorkspaceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

@Service
public class ParseDslService {

    private static final Log log = LogFactory.getLog(ParseDslService.class);

    public ParseDslService() {
    }

    @McpTool(description = "Parses a Structurizr DSL workspace")
    public String parse(
            @McpToolParam(description = "DSL", required = true) String dsl
    ) throws Exception {
        log.info("Validating DSL workspace");

        try {
            StructurizrDslParser parser = new StructurizrDslParser();
            parser.parse(dsl);

            return WorkspaceUtils.toJson(parser.getWorkspace(), false);
        } catch (Exception e) {
            log.error(e);
            return e.getMessage();
        }
    }

}