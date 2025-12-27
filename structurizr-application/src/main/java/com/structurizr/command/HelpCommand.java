package com.structurizr.command;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HelpCommand extends AbstractCommand {

    private static final Log log = LogFactory.getLog(HelpCommand.class);

    public HelpCommand() {
    }

    public void run(String... args) throws Exception {
        log.info("Usage: structurizr push|pull|lock|unlock|export|merge|validate|inspect|list|version|help [options]");
    }

}