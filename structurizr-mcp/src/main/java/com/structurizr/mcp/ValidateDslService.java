package com.structurizr.mcp;

import com.structurizr.dsl.StructurizrDslParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

@Service
public class ValidateDslService {

    private static final Log log = LogFactory.getLog(ValidateDslService.class);

    public ValidateDslService() {
    }

    @McpTool(description = "Validates a Structurizr DSL workspace")
    public String validate(
            @McpToolParam(description = "DSL", required = true) String dsl
    ) throws Exception {
        log.info("Validating DSL workspace");

        StructurizrDslParser parser = new StructurizrDslParser();
        parser.parse(dsl);

        return "OK";
    }

}