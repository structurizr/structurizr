package com.structurizr.command;

import com.structurizr.server.Server;

public class ServerCommand extends AbstractCommand {

    public ServerCommand() {
        super("server");
    }

    public boolean run(String... args) throws Exception {
        Server.main(args);
        return true;
    }

}