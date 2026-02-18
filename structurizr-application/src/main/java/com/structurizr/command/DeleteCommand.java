package com.structurizr.command;

import com.structurizr.api.AdminApiClient;
import com.structurizr.api.WorkspaceApiClient;
import com.structurizr.util.StringUtils;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Scanner;

public class DeleteCommand extends AbstractCommand {

    private static final Log log = LogFactory.getLog(DeleteCommand.class);

    public DeleteCommand() {
        super("delete");
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

        option = new Option("branch", "branch", true, "Branch name");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("f", "force", false, "Force deletion (non-interactive/quiet mode)");
        option.setRequired(false);
        options.addOption(option);

        CommandLineParser commandLineParser = new DefaultParser();

        String apiUrl = "";
        long workspaceId = 1;
        String apiKey = "";
        String branch = "";
        boolean force = false;

        try {
            CommandLine cmd = commandLineParser.parse(options, args);

            apiUrl = cmd.getOptionValue("apiUrl");
            workspaceId = Long.parseLong(cmd.getOptionValue("workspaceId"));
            apiKey = cmd.getOptionValue("apiKey");
            branch = cmd.getOptionValue("branch");
            force = cmd.hasOption("force");
        } catch (ParseException e) {
            log.error(e.getMessage());
            showHelp(options);
            System.exit(1);
        }

        if (StringUtils.isNullOrEmpty(branch)) {
            if (!force) {
                System.out.println("Delete workspace " + workspaceId  + " at " + apiUrl + "? (y/n)");
                Scanner scanner = new Scanner(System.in);
                String answer = scanner.nextLine();
                if (!answer.equalsIgnoreCase("y")) {
                    System.exit(1);
                }
            }

            log.debug("Deleting workspace " + workspaceId + " at " + apiUrl);

            AdminApiClient client = new AdminApiClient(apiUrl, apiKey);
            client.setAgent(getAgent());

            boolean deleted = client.deleteWorkspace(workspaceId);
            log.debug(" - " + deleted);
            System.exit(deleted ? 0 : 1);
        } else {
            if (!force) {
                System.out.println("Delete branch " + branch + " from workspace " + workspaceId + " at " + apiUrl + "? (y/n)");
                Scanner scanner = new Scanner(System.in);
                String answer = scanner.nextLine();
                if (!answer.equalsIgnoreCase("y")) {
                    System.exit(1);
                }
            }

            log.debug("Deleting branch " + branch + " from workspace " + workspaceId + " at " + apiUrl);

            WorkspaceApiClient client = new WorkspaceApiClient(apiUrl, workspaceId, apiKey);
            client.setAgent(getAgent());

            boolean deleted = client.deleteBranch(branch);
            log.debug(" - " + deleted);
            System.exit(deleted ? 0 : 1);
        }

    }

}