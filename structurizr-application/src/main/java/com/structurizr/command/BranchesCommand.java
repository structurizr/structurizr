package com.structurizr.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurizr.api.WorkspaceApiClient;
import com.structurizr.api.WorkspaceBranches;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class BranchesCommand extends AbstractCommand {

    private static final Log log = LogFactory.getLog(BranchesCommand.class);

    public BranchesCommand() {
        super("branches");
    }

    public void run(String... args) throws Exception {
        Options options = new Options();

        Option option = new Option("url", "apiUrl", true, "Structurizr API URL");
        option.setRequired(true);
        options.addOption(option);

        option = new Option("id", "workspaceId", true, "Workspace ID");
        option.setRequired(true);
        options.addOption(option);

        option = new Option("key", "apiKey", true, "Workspace API key");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("json", "json", false, "Output JSON");
        option.setRequired(false);
        options.addOption(option);

        CommandLineParser commandLineParser = new DefaultParser();

        String apiUrl = "";
        long workspaceId = 1;
        String apiKey = "";
        boolean json = false;

        try {
            CommandLine cmd = commandLineParser.parse(options, args);

            apiUrl = cmd.getOptionValue("apiUrl");
            workspaceId = Long.parseLong(cmd.getOptionValue("workspaceId"));
            apiKey = cmd.getOptionValue("apiKey");
            json = cmd.hasOption("json");
        } catch (ParseException e) {
            log.error(e.getMessage());
            showHelp(options);
            System.exit(1);
        }

        WorkspaceApiClient client = new WorkspaceApiClient(apiUrl, workspaceId, apiKey);
        client.setAgent(getAgent());
        WorkspaceBranches branches = client.getBranches();

        if (json) {
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(branches));
        } else {
            log.info("Getting branches for workspace " + workspaceId + " at " + apiUrl);
            for (String branch : branches.getBranches()) {
                log.info(" - " + branch);
            }
        }
    }

}