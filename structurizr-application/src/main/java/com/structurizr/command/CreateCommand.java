package com.structurizr.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurizr.api.AdminApiClient;
import com.structurizr.api.WorkspaceMetadata;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CreateCommand extends AbstractCommand {

    private static final Log log = LogFactory.getLog(CreateCommand.class);

    public CreateCommand() {
        super("create");
    }

    public void run(String... args) throws Exception {
        Options options = new Options();

        Option option = new Option("url", "apiUrl", true, "Structurizr API URL");
        option.setRequired(true);
        options.addOption(option);

        option = new Option("key", "apiKey", true, "Admin API key");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("json", "json", false, "Output JSON");
        option.setRequired(false);
        options.addOption(option);

        CommandLineParser commandLineParser = new DefaultParser();

        String apiUrl = "";
        String apiKey = "";
        boolean json = false;

        try {
            CommandLine cmd = commandLineParser.parse(options, args);

            apiUrl = cmd.getOptionValue("apiUrl");
            apiKey = cmd.getOptionValue("apiKey");
            json = cmd.hasOption("json");
        } catch (ParseException e) {
            log.error(e.getMessage());
            showHelp(options);
            System.exit(1);
        }

        log.debug("Creating workspace at " + apiUrl);

        AdminApiClient client = new AdminApiClient(apiUrl, apiKey);
        client.setAgent(getAgent());

        WorkspaceMetadata workspace = client.createWorkspace();

        if (json) {
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(workspace));
        } else {
            log.info("Workspace created:");
            log.info(" - ID: " + workspace.getId());
            log.info(" - Name: " + workspace.getName());
            log.info(" - Description: " + workspace.getDescription());
            log.info(" - API key: " + workspace.getApiKey());
            log.info(" - URL: " + apiUrl.substring(0, apiUrl.indexOf("/api")) + workspace.getPrivateUrl());
        }

        System.exit(workspace != null ? 0 : 1);
    }

}