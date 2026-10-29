package com.structurizr.command;

import com.structurizr.mcp.Server;

public class McpCommand extends AbstractCommand {

    public McpCommand() {
        super("mcp");
    }

    public boolean run(String... args) throws Exception {
        Server.main(args);
        return true;
    }

}