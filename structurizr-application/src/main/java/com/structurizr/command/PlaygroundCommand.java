package com.structurizr.command;

import com.structurizr.playground.Server;

public class PlaygroundCommand extends AbstractCommand {

    public PlaygroundCommand() {
    }

    public void run(String... args) throws Exception {
        Server.main(args);
    }

}