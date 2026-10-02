package com.structurizr.command;

import com.structurizr.server.Local;

public class LocalCommand extends AbstractCommand {

    public LocalCommand() {
        super("local");
    }

    public boolean run(String... args) throws Exception {
        Local.main(args);
        return true;
    }

}