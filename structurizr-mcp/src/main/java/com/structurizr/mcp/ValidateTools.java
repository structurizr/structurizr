package com.structurizr.mcp;

import com.structurizr.dsl.StructurizrDslParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("validate")
public class ValidateTools extends AbstractService {

    private static final Log log = LogFactory.getLog(ValidateTools.class);

    public ValidateTools() {
        log.info("Registering");
    }

    @McpTool(description = "Validates a Structurizr DSL workspace")
    public String validate(
            @McpToolParam(description = "DSL", required = true) String dsl
    ) throws Exception {
        log.info("Validating DSL workspace");

        StructurizrDslParser parser = createStructurizrDslParser();
        parser.parse(dsl);

        return "OK";
    }

}