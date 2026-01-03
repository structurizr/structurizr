package com.structurizr.command;

import com.structurizr.server.Local;

public class LocalCommand extends AbstractCommand {

    public LocalCommand() {
    }

    public void run(String... args) throws Exception {
        Local.main(args);
    }

}