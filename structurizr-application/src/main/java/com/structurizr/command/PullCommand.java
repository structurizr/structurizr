package com.structurizr.command;

import com.structurizr.Workspace;
import com.structurizr.api.AdminApiClient;
import com.structurizr.api.WorkspaceApiClient;
import com.structurizr.api.WorkspaceMetadata;
import com.structurizr.encryption.AesEncryptionStrategy;
import com.structurizr.util.StringUtils;
import com.structurizr.util.WorkspaceUtils;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Collection;

public class PullCommand extends AbstractCommand {

    private static final String ALL_WORKSPACES = "*";

    private static final Log log = LogFactory.getLog(PullCommand.class);

    private String apiUrl = "";
    private String workspaceId = "";
    private String apiKey = "";
    private String branch = "";
    private String passphrase = "";
    private File outputDir;
    private boolean debug = false;

    public PullCommand() {
        super("pull");
    }

    public void run(String... args) throws Exception {
        Options options = new Options();

        Option option = new Option("url", "apiUrl", true, "Structurizr API URL");
        option.setRequired(true);
        options.addOption(option);

        option = new Option("id", "workspaceId", true, "Workspace ID");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("key", "apiKey", true, "Workspace API key");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("branch", "branch", true, "Branch name");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("passphrase", "passphrase", true, "Client-side encryption passphrase");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("o", "output", true, "Path to an output directory");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("debug", "debug", false, "Enable debug logging");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("json", "json", false, "Output JSON");
        option.setRequired(false);
        options.addOption(option);

        CommandLineParser commandLineParser = new DefaultParser();

        String outputPath = null;
        boolean json = false;

        try {
            CommandLine cmd = commandLineParser.parse(options, args);

            apiUrl = cmd.getOptionValue("apiUrl");
            workspaceId = cmd.getOptionValue("workspaceId");
            apiKey = cmd.getOptionValue("apiKey");
            branch = cmd.getOptionValue("branch");
            passphrase = cmd.getOptionValue("passphrase");
            outputPath = cmd.getOptionValue("output");
            debug = cmd.hasOption("debug");
            json = cmd.hasOption("json");
        } catch (ParseException e) {
            log.error(e.getMessage());
            showHelp(options);
            System.exit(1);
        }

        if (StringUtils.isNullOrEmpty(outputPath)) {
            outputPath = ".";
        }
        outputDir = new File(outputPath);

        if (debug) {
            configureDebugLogging();
        }

        if (StringUtils.isNullOrEmpty(workspaceId)) {
            log.info("Pulling all workspaces from " + apiUrl);
            AdminApiClient adminApiClient = new AdminApiClient(apiUrl, apiKey);
            Collection<WorkspaceMetadata> workspaces = adminApiClient.getWorkspaces();
            for (WorkspaceMetadata workspace : workspaces) {
                pullWorkspace(workspace.getId(), true);
            }
        } else {
            pullWorkspace(Long.parseLong(workspaceId), !json);
        }
    }

    private void pullWorkspace(long workspaceId, boolean store) throws Exception {
        WorkspaceApiClient client = new WorkspaceApiClient(apiUrl, workspaceId, apiKey);
        client.setWorkspaceArchiveLocation(outputDir);
        client.setBranch(branch);
        client.setAgent(getAgent());

        if (!StringUtils.isNullOrEmpty(passphrase)) {
            log.info(" - using client-side encryption");
            client.setEncryptionStrategy(new AesEncryptionStrategy(passphrase));
        }

        Workspace workspace = client.getWorkspace();

        if (store) {
            outputDir.mkdirs();

            File file;
            if (StringUtils.isNullOrEmpty(branch)) {
                log.info("Pulling workspace " + workspaceId + " from " + apiUrl);
                file = new File(outputDir, "structurizr-" + workspaceId + "-workspace.json");
            } else {
                log.info("Pulling workspace " + workspaceId + " from " + apiUrl + " (branch=" + branch + ")");
                file = new File(outputDir, "structurizr-" + workspaceId + "-" + branch + "-workspace.json");
            }

            WorkspaceUtils.saveWorkspaceToJson(workspace, file);
            log.info(" - workspace saved as " + file.getCanonicalPath());
        } else {
            System.out.println(WorkspaceUtils.toJson(workspace, false));
        }
    }

}