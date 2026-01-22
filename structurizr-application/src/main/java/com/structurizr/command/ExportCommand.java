package com.structurizr.command;

import com.structurizr.Workspace;
import com.structurizr.documentation.Documentable;
import com.structurizr.export.*;
import com.structurizr.export.mermaid.MermaidDiagramExporter;
import com.structurizr.export.plantuml.C4PlantUMLExporter;
import com.structurizr.export.plantuml.StructurizrPlantUMLExporter;
import com.structurizr.export.websequencediagrams.WebSequenceDiagramsExporter;
import com.structurizr.http.HttpClient;
import com.structurizr.util.WorkspaceUtils;
import com.structurizr.view.ColorScheme;
import com.structurizr.view.ThemeUtils;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
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
        option.setRequired(true);
        options.addOption(option);

        option = new Option("f", "format", true, String.format("Export format: %s[/%s|%s]|%s|%s|%s|%s|%s|%s", PLANTUML_FORMAT, PLANTUML_STRUCTURIZR_SUBFORMAT, PLANTUML_C4PLANTUML_SUBFORMAT, WEBSEQUENCEDIAGRAMS_FORMAT, MERMAID_FORMAT, JSON_FORMAT, THEME_FORMAT, STATIC_FORMAT, CUSTOM_FORMAT));
        option.setRequired(true);
        options.addOption(option);

        option = new Option("o", "output", true, "Path to an output directory");
        option.setRequired(false);
        options.addOption(option);

        CommandLineParser commandLineParser = new DefaultParser();

        String workspacePathAsString = null;
        File workspacePath = null;
        long workspaceId = 1;
        String format = "";
        String outputPath = null;

        try {
            CommandLine cmd = commandLineParser.parse(options, args);

            workspacePathAsString = cmd.getOptionValue("workspace");
            format = cmd.getOptionValue("format");
            outputPath = cmd.getOptionValue("output");

        } catch (ParseException e) {
            log.error(e.getMessage());
            showHelp(options);
            System.exit(1);
        }

        log.info("Exporting workspace from " + workspacePathAsString);

        Workspace workspace = loadWorkspace(workspacePathAsString);

        if (workspacePathAsString.startsWith("http://") || workspacePathAsString.startsWith("https://")) {
            workspacePath = new File(".");
        } else {
            workspacePath = new File(workspacePathAsString);
        }

        workspaceId = workspace.getId();

        if (outputPath == null) {
            outputPath = new File(workspacePath.getCanonicalPath()).getParent();
        }
        
        File outputDir = new File(outputPath);
        outputDir.mkdirs();

        if (STATIC_FORMAT.equals(format)) {
            log.info(" - writing static site to " + outputDir.getAbsolutePath());
            writeStaticFile("static.html", outputDir, "index.html");

            ClassLoader cl = this.getClass().getClassLoader();
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);

            Resource[] resources = resolver.getResources("classpath*:/static/static/js/*.js") ;
            for (Resource resource: resources){
                writeStaticFile("js/" + resource.getFilename(), outputDir);
            }

            resources = resolver.getResources("classpath*:/static/static/css/*.css") ;
            for (Resource resource: resources){
                writeStaticFile("css/" + resource.getFilename(), outputDir);
            }

            resources = resolver.getResources("classpath*:/static/static/css/fonts/katex/*") ;
            for (Resource resource: resources){
                writeStaticFile("css/fonts/katex/" + resource.getFilename(), outputDir);
            }

            resources = resolver.getResources("classpath*:/static/static/img/*") ;
            for (Resource resource: resources){
                writeStaticFile("img/" + resource.getFilename(), outputDir);
            }

            // clear all documentation - this isn't supported by the static site
            workspace.getDocumentation().clear();
            workspace.getModel().getElements().stream().filter(e -> e instanceof Documentable).map(e -> (Documentable)e).forEach(e -> e.getDocumentation().clear());

            String json = WorkspaceUtils.toJson(workspace, false);
            String base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));

            writeToFile(
                    new File(outputDir, "workspace.js"),
                    String.format("const jsonAsString = '%s';", base64)
            );

        } else {
            if (!JSON_FORMAT.equalsIgnoreCase(format)) {
                // only inline the theme amd create default views if the user wants a diagram export
                HttpClient httpClient = new HttpClient();
                httpClient.allow(".*");

                ThemeUtils.loadThemes(workspace, httpClient);
            }

            Exporter exporter = findExporter(format, workspacePath);
            if (exporter == null) {
                log.info(" - unknown export format: " + format);
            } else {
                log.info(" - exporting with " + exporter.getClass().getSimpleName());

                if (exporter instanceof DiagramExporter) {
                    DiagramExporter diagramExporter = (DiagramExporter) exporter;

                    if (workspace.getViews().isEmpty()) {
                        log.info(" - the workspace contains no views");
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

        log.info(" - finished");
    }

    private void writeStaticFile(String filename, File outputDir) throws IOException {
        writeStaticFile(filename, outputDir, filename);
    }

    private void writeStaticFile(String filename, File outputDir, String outputFilename) throws IOException {
        InputStream in = getClass().getResourceAsStream("/static/static/" + filename);
        if (in != null) {
            File outputFile = new File(outputDir, outputFilename);
            outputFile.mkdirs();
            Files.copy(in, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else {
            log.error(String.format("Unable to find static file: %s", filename));
        }
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
            log.error(" - unknown export format: " + format);
        } catch (Exception e) {
            log.error(" - error creating instance of " + format, e);
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
        log.info(" - writing " + file.getCanonicalPath());

        BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
        writer.write(content);
        writer.flush();
        writer.close();
    }

}