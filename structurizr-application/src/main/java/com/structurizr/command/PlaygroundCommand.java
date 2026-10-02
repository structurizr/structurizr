package com.structurizr.command;

import com.structurizr.playground.Server;

public class PlaygroundCommand extends AbstractCommand {

    public PlaygroundCommand() {
        super("playground");
    }

    public boolean run(String... args) throws Exception {
        Server.main(args);
        return true;
    }

}