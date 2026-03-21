package com.structurizr.command;

import com.structurizr.Workspace;
import com.structurizr.documentation.Documentable;
import com.structurizr.util.WorkspaceUtils;
import com.structurizr.view.ThemeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

class StaticSiteExporter {

    private static final Log log = LogFactory.getLog(StaticSiteExporter.class);

    void run(Workspace workspace, File outputDir) throws Exception {
        ThemeUtils.inlineStylesUsedFromInstalledThemes(workspace);

        log.info(" - writing static site to " + outputDir.getAbsolutePath());
        writeStaticFile("static.html", outputDir, "index.html");

        writeStaticFile("js/jquery-3.7.1.min.js", outputDir);
        writeStaticFile("js/bootstrap-5.3.7.min.js", outputDir);
        writeStaticFile("js/crypto-js-4.1.1.min.js", outputDir);
        writeStaticFile("js/jointjs-Core-4.1.3.js", outputDir);
        writeStaticFile("js/jointjs-DirectedGraph-4.1.3.min.js", outputDir);
        writeStaticFile("js/dagre-1.1.8.js", outputDir);
        writeStaticFile("js/graphlib-2.2.4.min.js", outputDir);
        writeStaticFile("js/jointjs-DirectedGraph-4.1.3.min.js", outputDir);
        writeStaticFile("js/structurizr.js", outputDir);
        writeStaticFile("js/structurizr-util.js", outputDir);
        writeStaticFile("js/structurizr-ui.js", outputDir);
        writeStaticFile("js/structurizr-workspace.js", outputDir);
        writeStaticFile("js/structurizr-diagram.js", outputDir);
        writeStaticFile("js/structurizr-quick-navigation.js", outputDir);
        writeStaticFile("js/structurizr-navigation.js", outputDir);
        writeStaticFile("js/structurizr-tooltip.js", outputDir);
        writeStaticFile("js/structurizr-embed.js", outputDir);

        writeStaticFile("css/bootstrap-5.3.7.min.css", outputDir);
        writeStaticFile("css/structurizr.css", outputDir);
        writeStaticFile("css/structurizr-static.css", outputDir);
        writeStaticFile("css/structurizr-static-dark.css", outputDir);

        writeStaticFile("img/favicon.png", outputDir);
        writeStaticFile("img/structurizr-banner-light.png", outputDir);
        writeStaticFile("img/structurizr-banner-dark.png", outputDir);

        // clear all documentation - this isn't supported by the static site
        workspace.getDocumentation().clear();
        workspace.getModel().getElements().stream().filter(e -> e instanceof Documentable).map(e -> (Documentable)e).forEach(e -> e.getDocumentation().clear());

        String json = WorkspaceUtils.toJson(workspace, false);
        String base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));

        writeToFile(
                new File(outputDir, "workspace.js"),
                String.format("const jsonAsString = '%s';", base64)
        );
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

    private void writeToFile(File file, String content) throws Exception {
        log.info(" - writing " + file.getCanonicalPath());

        BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
        writer.write(content);
        writer.flush();
        writer.close();
    }

}
