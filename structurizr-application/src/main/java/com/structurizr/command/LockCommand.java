package com.structurizr.command;

import com.structurizr.api.WorkspaceApiClient;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LockCommand extends AbstractCommand {

    private static final Log log = LogFactory.getLog(LockCommand.class);

    public LockCommand() {
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
        option.setRequired(true);
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
            formatter.printHelp("lock", options);

            System.exit(1);
        }

        log.info("Locking workspace " + workspaceId + " at " + apiUrl);
        WorkspaceApiClient client = new WorkspaceApiClient(apiUrl, workspaceId, apiKey);
        client.setAgent(getAgent());
        boolean locked = client.lockWorkspace();

        log.info(" - locked " + locked);
        log.info(" - finished");

        System.exit(locked ? 0 : 1);
    }

}