package com.structurizr.command;

import com.structurizr.server.Server;

public class ServerCommand extends AbstractCommand {

    public ServerCommand() {
    }

    public void run(String... args) throws Exception {
        Server.main(args);
    }

}