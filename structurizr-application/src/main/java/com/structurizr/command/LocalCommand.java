package com.structurizr.command;

import com.structurizr.server.LocalServer;

public class LocalCommand extends AbstractCommand {

    public LocalCommand() {
    }

    public void run(String... args) throws Exception {
        LocalServer.main(args);
    }

}