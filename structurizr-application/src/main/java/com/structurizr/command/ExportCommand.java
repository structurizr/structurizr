package com.structurizr.command;

import com.structurizr.Workspace;
import com.structurizr.export.*;
import com.structurizr.export.mermaid.MermaidDiagramExporter;
import com.structurizr.export.plantuml.C4PlantUMLExporter;
import com.structurizr.export.plantuml.StructurizrPlantUMLExporter;
import com.structurizr.export.websequencediagrams.WebSequenceDiagramsExporter;
import com.structurizr.http.HttpClient;
import com.structurizr.util.StringUtils;
import com.structurizr.view.ColorScheme;
import com.structurizr.view.ThemeUtils;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ExportCommand extends AbstractCommand {

    private static final Log log = LogFactory.getLog(ExportCommand.class);

    private static final String JSON_FORMAT = "json";
    private static final String THEME_FORMAT = "theme";
    private static final String LIGHT = "light";
    private static final String DARK = "dark";
    private static final String PLANTUML_FORMAT = "plantuml";
    private static final String PLANTUML_C4PLANTUML_SUBFORMAT = "c4plantuml";
    private static final String PLANTUML_STRUCTURIZR_SUBFORMAT = "structurizr";
    private static final String WEBSEQUENCEDIAGRAMS_FORMAT = "websequencediagrams";
    private static final String MERMAID_FORMAT = "mermaid";
    private static final String STATIC_FORMAT = "static";
    private static final String PNG_FORMAT = "png";
    private static final String SVG_FORMAT = "svg";
    private static final String CUSTOM_FORMAT = "fqcn";

    private static final Map<String,Exporter> EXPORTERS = new HashMap<>();

    static {
        EXPORTERS.put(JSON_FORMAT, new JsonWorkspaceExporter());

        EXPORTERS.put(THEME_FORMAT, new JsonWorkspaceThemeExporter());

        EXPORTERS.put(PLANTUML_FORMAT, new StructurizrPlantUMLExporter());
        EXPORTERS.put(PLANTUML_FORMAT + "-" + LIGHT, new StructurizrPlantUMLExporter(ColorScheme.Light));
        EXPORTERS.put(PLANTUML_FORMAT + "-" + DARK, new StructurizrPlantUMLExporter(ColorScheme.Dark));

        EXPORTERS.put(PLANTUML_FORMAT + "/" + PLANTUML_STRUCTURIZR_SUBFORMAT, new StructurizrPlantUMLExporter());
        EXPORTERS.put(PLANTUML_FORMAT + "/" + PLANTUML_STRUCTURIZR_SUBFORMAT + "-" + LIGHT, new StructurizrPlantUMLExporter(ColorScheme.Light));
        EXPORTERS.put(PLANTUML_FORMAT + "/" + PLANTUML_STRUCTURIZR_SUBFORMAT + "-" + DARK, new StructurizrPlantUMLExporter(ColorScheme.Dark));

        EXPORTERS.put(PLANTUML_FORMAT + "/" + PLANTUML_C4PLANTUML_SUBFORMAT, new C4PlantUMLExporter());
        EXPORTERS.put(PLANTUML_FORMAT + "/" + PLANTUML_C4PLANTUML_SUBFORMAT + "-" + LIGHT, new C4PlantUMLExporter(ColorScheme.Light));
        EXPORTERS.put(PLANTUML_FORMAT + "/" + PLANTUML_C4PLANTUML_SUBFORMAT + "-" + DARK, new C4PlantUMLExporter(ColorScheme.Dark));

        EXPORTERS.put(MERMAID_FORMAT, new MermaidDiagramExporter());
        EXPORTERS.put(WEBSEQUENCEDIAGRAMS_FORMAT, new WebSequenceDiagramsExporter());
    }

    public ExportCommand() {
        super("export");
    }

    public void run(String... args) throws Exception {
        Options options = new Options();

        Option option = new Option("w", "workspace", true, "Path or URL to the workspace JSON file/DSL file(s)");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("f", "format", true, String.format("Export format: %s[/%s|%s]|%s|%s|%s|%s|%s|%s", PLANTUML_FORMAT, PLANTUML_STRUCTURIZR_SUBFORMAT, PLANTUML_C4PLANTUML_SUBFORMAT, WEBSEQUENCEDIAGRAMS_FORMAT, MERMAID_FORMAT, JSON_FORMAT, THEME_FORMAT, STATIC_FORMAT, CUSTOM_FORMAT));
        option.setRequired(true);
        options.addOption(option);

        option = new Option("o", "output", true, "Path to an output directory");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("t", "themes", true, "Path to themes");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("url", "url", true, "Structurizr diagram page URL (for PNG/SVG exports)");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("animation", "animation", true, "Animation: true|false");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("mode", "mode", true, "Rendering mode: light|dark (for PNG/SVG exports)");
        option.setRequired(false);
        options.addOption(option);

        CommandLineParser commandLineParser = new DefaultParser();

        String workspacePathAsString = null;
        File workspacePath = null;
        long workspaceId = 1;
        String format = "";
        String mode = "";
        String url = null;
        boolean animation = false;
        String outputPath = null;

        try {
            CommandLine cmd = commandLineParser.parse(options, args);

            workspacePathAsString = cmd.getOptionValue("workspace");
            format = cmd.getOptionValue("format").toLowerCase();
            mode = cmd.getOptionValue("mode", "light").toLowerCase();
            url = cmd.getOptionValue("url");
            animation = Boolean.parseBoolean(cmd.getOptionValue("animation"));
            outputPath = cmd.getOptionValue("output");
        } catch (ParseException e) {
            log.error(e.getMessage());
            showHelp(options);
            System.exit(1);
        }

        if (PNG_FORMAT.equals(format) || SVG_FORMAT.equals(format)) {
            if ("false".equalsIgnoreCase(System.getProperty("structurizr.playwright"))) {
                log.fatal("Exporting to PNG/SVG is not supported in this environment");
                System.exit(1);
            }

            if (StringUtils.isNullOrEmpty(workspacePathAsString) && StringUtils.isNullOrEmpty(url)) {
                log.fatal("One of url or workspace must be provided");
                System.exit(1);
            }

            ColorScheme colorScheme = null;
            if (ColorScheme.Light.toString().equalsIgnoreCase(mode)) {
                colorScheme = ColorScheme.Light;
            } else if (ColorScheme.Dark.toString().equalsIgnoreCase(mode)) {
                colorScheme = ColorScheme.Dark;
            } else {
                log.fatal("Invalid mode " + mode + " - expected light or dark");
                System.exit(1);
            }

            if (!StringUtils.isNullOrEmpty(workspacePathAsString)) {
                try {
                    Workspace workspace = loadWorkspace(workspacePathAsString);
                    File tempDir = Files.createTempDirectory("structurizr").toFile();
                    tempDir.deleteOnExit();

                    if (workspacePathAsString.startsWith("http://") || workspacePathAsString.startsWith("https://")) {
                        workspacePath = new File(".");
                    } else {
                        workspacePath = new File(workspacePathAsString);
                    }

                    if (outputPath == null) {
                        outputPath = new File(workspacePath.getCanonicalPath()).getParent();
                    }

                    File outputDir = new File(outputPath);
                    outputDir.mkdirs();

                    new StaticSiteExporter().run(workspace, tempDir);
                    new PlaywrightExporter().run("file://" + new File(tempDir, "index.html").getAbsolutePath(), format, colorScheme, animation, outputDir);
                    return;
                } catch (Exception e) {
                    log.error(e.getMessage());
                    System.exit(1);
                }
            } else {
                if (outputPath == null) {
                    outputPath = ".";
                }

                File outputDir = new File(outputPath);
                outputDir.mkdirs();

                new PlaywrightExporter().run(url, format, colorScheme, animation, outputDir);
                return;
            }
        }

        if (StringUtils.isNullOrEmpty(workspacePathAsString)) {
            log.fatal("The workspace path parameter must not be null or empty");
            System.exit(1);
        }

        log.info("Exporting workspace from " + workspacePathAsString);

        Workspace workspace = loadWorkspace(workspacePathAsString);

        if (workspacePathAsString.startsWith("http://") || workspacePathAsString.startsWith("https://")) {
            workspacePath = new File(".");
        } else {
            workspacePath = new File(workspacePathAsString);
        }

        if (outputPath == null) {
            outputPath = new File(workspacePath.getCanonicalPath()).getParent();
        }

        File outputDir = new File(outputPath);
        outputDir.mkdirs();

        workspaceId = workspace.getId();

        if (STATIC_FORMAT.equals(format)) {
            new StaticSiteExporter().run(workspace, outputDir);
        } else {
            Exporter exporter = findExporter(format, workspacePath);
            if (exporter == null) {
                log.info("Unknown export format: " + format);
            } else {
                log.info("Exporting with " + exporter.getClass().getSimpleName());

                if (exporter instanceof DiagramExporter) {
                    HttpClient httpClient = new HttpClient();
                    httpClient.allow(".*");

                    // load the themes so that the styles can be applied to the diagram exports
                    ThemeUtils.loadThemes(workspace, httpClient);

                    DiagramExporter diagramExporter = (DiagramExporter)exporter;

                    if (workspace.getViews().isEmpty()) {
                        log.error("The workspace contains no views");
                    } else {
                        Collection<Diagram> diagrams = diagramExporter.export(workspace);

                        for (Diagram diagram : diagrams) {
                            File file = new File(outputPath, String.format("%s-%s.%s", prefix(workspaceId), diagram.getKey(), diagram.getFileExtension()));
                            writeToFile(file, diagram.getDefinition());

                            if (diagram.getLegend() != null) {
                                file = new File(outputPath, String.format("%s-%s-key.%s", prefix(workspaceId), diagram.getKey(), diagram.getFileExtension()));
                                writeToFile(file, diagram.getLegend().getDefinition());
                            }

                            if (!diagram.getFrames().isEmpty()) {
                                int index = 1;
                                for (Diagram frame : diagram.getFrames()) {
                                    file = new File(outputPath, String.format("%s-%s-%s.%s", prefix(workspaceId), diagram.getKey(), index, diagram.getFileExtension()));
                                    writeToFile(file, frame.getDefinition());
                                    index++;
                                }
                            }
                        }
                    }
                } else if (exporter instanceof WorkspaceExporter) {
                    WorkspaceExporter workspaceExporter = (WorkspaceExporter) exporter;
                    WorkspaceExport export = workspaceExporter.export(workspace);

                    String filename;

                    if (THEME_FORMAT.equalsIgnoreCase(format)) {
                        filename = workspacePath.getName().substring(0, workspacePath.getName().lastIndexOf('.')) + "-theme";
                    } else {
                        filename = workspacePath.getName().substring(0, workspacePath.getName().lastIndexOf('.'));
                    }

                    File file = new File(outputPath, String.format("%s.%s", filename, export.getFileExtension()));
                    writeToFile(file, export.getDefinition());
                }
            }
        }

        log.info("Finished");
    }


    private Exporter findExporter(String format, File workspacePath) {
        if (EXPORTERS.containsKey(format.toLowerCase())) {
            return EXPORTERS.get(format.toLowerCase());
        }

        try {
            Class<?> clazz = loadClass(format, workspacePath);
            if (Exporter.class.isAssignableFrom(clazz)) {
                return (Exporter) clazz.getDeclaredConstructor().newInstance();
            }
        } catch (ClassNotFoundException e) {
            log.error("Unknown export format: " + format);
        } catch (Exception e) {
            log.error("Error creating instance of " + format, e);
        }

        return null;
    }

    private String prefix(long workspaceId) {
        if (workspaceId > 0) {
            return "structurizr-" + workspaceId;
        } else {
            return "structurizr";
        }
    }

    private void writeToFile(File file, String content) throws Exception {
        log.info("Writing " + file.getCanonicalPath());

        BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
        writer.write(content);
        writer.flush();
        writer.close();
    }

}