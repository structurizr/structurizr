package com.structurizr.command;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GenerateCommand extends AbstractCommand {

    private static final Log log = LogFactory.getLog(GenerateCommand.class);

    private static final String SYSTEM_LANDSCAPE_SUBCOMMAND = "system-landscape";

    public GenerateCommand() {
        super("generate");
    }

    public void run(String... args) throws Exception {
        if (args.length == 1) {
            log.fatal("Missing subcommand");
            log.fatal("Expected one of: " + SYSTEM_LANDSCAPE_SUBCOMMAND);
            System.exit(1);
        }

        String subcommand = args[1];

        if (SYSTEM_LANDSCAPE_SUBCOMMAND.equals(subcommand)) {
            new GenerateSystemLandscapeSubcommand().run(args);
        } else {
            log.fatal("Unexpected subcommand: " + subcommand);
            log.fatal("Expected one of: " + SYSTEM_LANDSCAPE_SUBCOMMAND);
            System.exit(1);
        }
    }

}