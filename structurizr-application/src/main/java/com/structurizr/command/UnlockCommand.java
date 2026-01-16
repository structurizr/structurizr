package com.structurizr.command;

import com.structurizr.api.WorkspaceApiClient;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UnlockCommand extends AbstractCommand {

    private static final Log log = LogFactory.getLog(UnlockCommand.class);

    public UnlockCommand() {
    }

    public void run(String... args) throws Exception {
        Options options = new Options();

        Option option = new Option("url", "structurizrApiUrl", true, "Structurizr API URL");
        option.setRequired(true);
        options.addOption(option);

        option = new Option("id", "workspaceId", true, "Workspace ID");
        option.setRequired(true);
        options.addOption(option);

        option = new Option("key", "apiKey", true, "Workspace API key");
        option.setRequired(false);
        options.addOption(option);

        CommandLineParser commandLineParser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        String apiUrl = "";
        long workspaceId = 1;
        String apiKey = "";

        try {
            CommandLine cmd = commandLineParser.parse(options, args);

            apiUrl = cmd.getOptionValue("structurizrApiUrl", "https://api.structurizr.com");
            workspaceId = Long.parseLong(cmd.getOptionValue("workspaceId"));
            apiKey = cmd.getOptionValue("apiKey");
        } catch (ParseException e) {
            log.error(e.getMessage());
            formatter.printHelp("unlock", options);

            System.exit(1);
        }

        log.info("Unlocking workspace " + workspaceId + " at " + apiUrl);
        WorkspaceApiClient client = new WorkspaceApiClient(apiUrl, workspaceId, apiKey);
        client.setAgent(getAgent());
        boolean locked = client.unlockWorkspace();

        log.info(" - unlocked " + locked);
        log.info(" - finished");

        System.exit(locked ? 0 : 1);
    }

}