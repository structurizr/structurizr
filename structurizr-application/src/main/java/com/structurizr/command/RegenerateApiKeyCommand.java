package com.structurizr.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurizr.api.WorkspaceApiClient;
import com.structurizr.api.WorkspaceMetadata;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RegenerateApiKeyCommand extends AbstractCommand {

    private static final Log log = LogFactory.getLog(RegenerateApiKeyCommand.class);

    public RegenerateApiKeyCommand() {
        super("regenerate-apikey");
    }

    public void run(String... args) throws Exception {
        Options options = new Options();

        Option option = new Option("url", "apiUrl", true, "Structurizr API URL");
        option.setRequired(true);
        options.addOption(option);

        option = new Option("id", "workspaceId", true, "Workspace ID");
        option.setRequired(true);
        options.addOption(option);

        option = new Option("key", "apiKey", true, "Admin API key");
        option.setRequired(true);
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

        log.debug("Regenerating API key for workspace " + workspaceId + " at " + apiUrl);

        WorkspaceApiClient client = new WorkspaceApiClient(apiUrl, workspaceId, apiKey);
        client.setAgent(getAgent());

        WorkspaceMetadata response = client.regenerateApiKey();

        if (json) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
            System.out.println(mapper.writeValueAsString(response));
        } else {
            log.info(response.getApiKey());
        }

        System.exit(response != null ? 0 : 1);
    }

}