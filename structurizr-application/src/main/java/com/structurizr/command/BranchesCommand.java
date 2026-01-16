package com.structurizr.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurizr.api.WorkspaceApiClient;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BranchesCommand extends AbstractCommand {

    private static final Log log = LogFactory.getLog(BranchesCommand.class);

    public BranchesCommand() {
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
            formatter.printHelp("branches", options);

            System.exit(1);
        }

        log.debug("Getting branches for workspace " + workspaceId + " at " + apiUrl);
        WorkspaceApiClient client = new WorkspaceApiClient(apiUrl, workspaceId, apiKey);
        client.setAgent(getAgent());
        String[] branches = client.getBranches();

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(branches));
    }

}