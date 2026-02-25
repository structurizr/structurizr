package com.structurizr.command;

import com.structurizr.Workspace;
import com.structurizr.api.WorkspaceApiClient;
import com.structurizr.encryption.AesEncryptionStrategy;
import com.structurizr.util.ImageUtils;
import com.structurizr.util.StringUtils;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

public class PushCommand extends AbstractCommand {

    private static final Log log = LogFactory.getLog(PushCommand.class);

    public PushCommand() {
        super("push");
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

        option = new Option("w", "workspace", true, "Path or URL to the workspace JSON/DSL file");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("i", "image", true, "Path to an PNG/JPG/SVG image");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("passphrase", "passphrase", true, "Client-side encryption passphrase");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("merge", "mergeFromRemote", true, "Whether to merge layout information from the remote workspace (default=true)");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("trim", "trim", true, "Whether to trim the workspace before pushing (default=false)");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("archive", "archive", true, "Stores the previous version of the remote workspace");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("debug", "debug", false, "Enable debug logging");
        option.setRequired(false);
        options.addOption(option);

        CommandLineParser commandLineParser = new DefaultParser();

        String apiUrl = "";
        long workspaceId = 1;
        String apiKey = "";
        String branch = "";
        String workspacePath = "";
        String imagePath = "";
        String passphrase = "";
        boolean mergeFromRemote = true;
        boolean trim = false;
        boolean archive = true;
        boolean debug = false;

        try {
            CommandLine cmd = commandLineParser.parse(options, args);

            apiUrl = cmd.getOptionValue("apiUrl");
            workspaceId = Long.parseLong(cmd.getOptionValue("workspaceId"));
            apiKey = cmd.getOptionValue("apiKey");
            branch = cmd.getOptionValue("branch");
            workspacePath = cmd.getOptionValue("workspace");
            imagePath = cmd.getOptionValue("image");
            passphrase = cmd.getOptionValue("passphrase");
            mergeFromRemote = Boolean.parseBoolean(cmd.getOptionValue("merge", "true"));
            trim = Boolean.parseBoolean(cmd.getOptionValue("trim", "false"));
            archive = Boolean.parseBoolean(cmd.getOptionValue("archive", "true"));
            debug = cmd.hasOption("debug");
        } catch (ParseException e) {
            log.error(e.getMessage());
            showHelp(options);
            System.exit(1);
        }

        if (StringUtils.isNullOrEmpty(workspacePath) && StringUtils.isNullOrEmpty(imagePath)) {
            log.error("One of -workspace or -image are required");
            System.exit(1);
        }

        if (debug) {
            configureDebugLogging();
        }

        WorkspaceApiClient client = new WorkspaceApiClient(apiUrl, workspaceId, apiKey);
        client.setBranch(branch);
        client.setAgent(getAgent());
        client.setWorkspaceArchiveLocation(null);

        if (!StringUtils.isNullOrEmpty(imagePath)) {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                log.fatal("Image file does not exist: " + imagePath);
                System.exit(1);
            }

            String filename = imageFile.getName();
            String base64DataUri = ImageUtils.getImageAsDataUri(new File(imagePath));
            client.putImage(filename, base64DataUri);
        } else {
            if (StringUtils.isNullOrEmpty(branch)) {
                log.info("Pushing workspace " + workspaceId + " to " + apiUrl);
            } else {
                log.info("Pushing workspace " + workspaceId + " to " + apiUrl + " (branch=" + branch + ")");
            }

            if (!StringUtils.isNullOrEmpty(passphrase)) {
                log.info(" - using client-side encryption");
                client.setEncryptionStrategy(new AesEncryptionStrategy(passphrase));
            }

            File archivePath = new File(".");

            File path = new File(workspacePath);
            archivePath = path.getParentFile();
            if (!path.exists()) {
                log.error(" - workspace path " + workspacePath + " does not exist");
                System.exit(1);
            }

            log.info(" - creating new workspace");
            log.info(" - parsing model and views from " + path.getCanonicalPath());

            Workspace workspace = loadWorkspace(workspacePath);

            if (trim) {
                log.info(" - trimming workspace");
                workspace.trim();
            }

            log.info(" - merge layout from remote: " + mergeFromRemote);
            client.setMergeFromRemote(mergeFromRemote);

            if (archive) {
                client.setWorkspaceArchiveLocation(archivePath);
                log.info(" - storing previous version of workspace in " + client.getWorkspaceArchiveLocation());
            }

            log.info(" - pushing workspace");
            client.putWorkspace(workspace);
        }

        log.info(" - finished");
    }

}