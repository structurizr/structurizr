package com.structurizr.mcp;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class McpErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError() {
        return "error";
    }

}