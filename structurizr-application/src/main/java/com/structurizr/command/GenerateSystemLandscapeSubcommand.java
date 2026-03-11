package com.structurizr.command;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.dsl.StructurizrDslParserException;
import com.structurizr.util.StringUtils;
import com.structurizr.util.SystemLandscapeGenerator;
import com.structurizr.util.WorkspaceUtils;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.*;

public class GenerateSystemLandscapeSubcommand extends AbstractCommand {

    private static final Log log = LogFactory.getLog(GenerateSystemLandscapeSubcommand.class);

    private static final String DEFAULT_INCLUDE_FILTER_REGEX = ".*workspace.(json|dsl)";

    public GenerateSystemLandscapeSubcommand() {
        super("generate");
    }

    public void run(String... args) throws Exception {
        Options options = new Options();

        Option option = new Option("i", "input", true, "Path to the workspace JSON/DSL files");
        option.setRequired(true);
        options.addOption(option);

        option = new Option("include", "include", true, "Include filename filter as a regex");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("exclude", "exclude", true, "Exclude filename filter as a regex");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("relationships", "relationships", true, "Relationships strategy (first|all)");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("o", "output", true, "Path of output workspace");
        option.setRequired(true);
        options.addOption(option);

        CommandLineParser commandLineParser = new DefaultParser();

        try {
            CommandLine cmd = commandLineParser.parse(options, args);

            File inputPath = new File(cmd.getOptionValue("input"));
            if (!inputPath.exists()) {
                log.fatal("The input path does not exist at " + inputPath.getAbsolutePath());
                System.exit(1);
            }
            log.info("Loading workspaces from " + inputPath.getAbsolutePath());

            String includeFilter = cmd.getOptionValue("include");
            if (StringUtils.isNullOrEmpty(includeFilter)) {
                includeFilter = DEFAULT_INCLUDE_FILTER_REGEX;
            }
            log.info(" - include: " + includeFilter);

            String excludeFilter = cmd.getOptionValue("exclude");
            log.info(" - exclude: " + excludeFilter);

            String relationships = cmd.getOptionValue("relationships");

            File outputFile = new File(cmd.getOptionValue("output"));
            if (outputFile.exists() && !outputFile.isFile()) {
                outputFile = new File(outputFile, "workspace.json");
            }

            Collection<Workspace> workspaces = loadWorkspaces(inputPath, includeFilter, excludeFilter);
            SystemLandscapeGenerator systemLandscapeGenerator = createSystemLandscapeGenerator(relationships);
            Workspace workspace = systemLandscapeGenerator.generate(workspaces);

            log.info("Writing system landscape workspace to " + outputFile.getAbsolutePath());
            WorkspaceUtils.saveWorkspaceToJson(workspace, outputFile);
        } catch (ParseException e) {
            log.error(e.getMessage());
            showHelp(options);
            System.exit(1);
        }
    }

    private Collection<Workspace> loadWorkspaces(File inputPath, String includeFilter, String excludeFilter) {
        List<Workspace> workspaces = new ArrayList<>();
        File[] files = inputPath.listFiles();
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));

            for (File file : files) {
                if (file.isDirectory()) {
                    workspaces.addAll(loadWorkspaces(file, includeFilter, excludeFilter));
                } else {
                    boolean include = file.getAbsolutePath().matches(includeFilter);
                    boolean exclude = !StringUtils.isNullOrEmpty(excludeFilter) && file.getAbsolutePath().matches(excludeFilter);
                    if (include && !exclude) {
                        if (file.getName().endsWith(".json")) {
                            try {
                                log.info(" - found: " + file.getAbsolutePath());
                                Workspace workspace = WorkspaceUtils.loadWorkspaceFromJson(file);
                                workspaces.add(workspace);
                            } catch (Exception e) {
                                log.warn("Could not load workspace from JSON file: " + file.getAbsolutePath());
                            }
                        } else if (file.getName().endsWith(".dsl")) {
                            try {
                                log.info(" - found: " + file.getAbsolutePath());
                                StructurizrDslParser parser = new StructurizrDslParser();
                                parser.parse(file);
                                workspaces.add(parser.getWorkspace());
                            } catch (StructurizrDslParserException e) {
                                log.warn("Could not parse DSL file: " + file.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        }

        return workspaces;
    }

    private SystemLandscapeGenerator createSystemLandscapeGenerator(String relationships) {
        SystemLandscapeGenerator systemLandscapeGenerator = new SystemLandscapeGenerator();
        if (!StringUtils.isNullOrEmpty(relationships)) {
            if (SystemLandscapeGenerator.RelationshipsStrategy.First.name().equalsIgnoreCase(relationships)) {
                systemLandscapeGenerator.setRelationshipStrategy(SystemLandscapeGenerator.RelationshipsStrategy.First);
            } else if (SystemLandscapeGenerator.RelationshipsStrategy.All.name().equalsIgnoreCase(relationships)) {
                systemLandscapeGenerator.setRelationshipStrategy(SystemLandscapeGenerator.RelationshipsStrategy.All);
            } else {
                log.fatal("Invalid relationships strategy " + relationships + " (expected first or all)");
                System.exit(1);
            }
        }

        return systemLandscapeGenerator;
    }

}