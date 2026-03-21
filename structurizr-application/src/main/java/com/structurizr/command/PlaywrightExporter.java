package com.structurizr.command;

import com.microsoft.playwright.*;
import com.structurizr.util.StringUtils;
import com.structurizr.view.ColorScheme;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

class PlaywrightExporter {

    void run(String url, String format, ColorScheme colorScheme, boolean animation, File outputDir) {
        List<Diagram> diagrams = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext(new Browser.NewContextOptions().setBypassCSP(true));

            Page page = context.newPage();
            page.navigate(url);

            page.addScriptTag(new Page.AddScriptTagOptions().setContent("""
                var finished = false;
                
                function exportFinished() {
                    finished = true;
                }
                """));

            page.exposeBinding("exportDiagram", (source, args) -> {
                // (view, diagram, diagramKey, download)
                HashMap<String,String> diagramMap = (HashMap<String,String>)args[1];
                DiagramFile diagram = new DiagramFile(diagramMap.get("filename"), diagramMap.get("content"));
                DiagramFile key = null;

                if (args[2] != null) {
                    HashMap<String,String> diagramKeyMap = (HashMap<String,String>)args[2];
                    key = new DiagramFile(diagramKeyMap.get("filename"), diagramKeyMap.get("content"));
                }

                diagrams.add(new Diagram(diagram, key));

                return true;
            });

            page.waitForFunction("structurizr.scripting && structurizr.scripting.isDiagramRendered() === true");

            page.evaluate(String.format("""
                structurizr.scripting.setDarkMode(%s);
                const views = structurizr.scripting.getViews().map(function(view) { return view.key; });
                structurizr.scripting.exportViews(views, { format: '%s', prefix: '', publish: true, metadata: true, animation: %s }, window.exportDiagram, window.exportFinished);
                """, colorScheme == ColorScheme.Dark, format, animation));

            page.waitForFunction("finished === true");

            browser.close();
        }

        for (Diagram diagram : diagrams) {
            save(diagram.getDiagram(), outputDir);

            if (diagram.hasKey()) {
                save(diagram.getKey(), outputDir);
            }
        }
    }

    private void save(DiagramFile diagram, File outputDir) {
        if (diagram != null && !StringUtils.isNullOrEmpty(diagram.getContent())) {
            try {
                File file = new File(outputDir, diagram.getFilename());
                System.out.println(" - " + file.getAbsolutePath());
                String base64Image = diagram.getContent().split(",")[1];
                byte[] decodedImage = Base64.getDecoder().decode(base64Image.getBytes(StandardCharsets.UTF_8));
                Files.write(file.toPath(), decodedImage);
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    static class Diagram {

        private final DiagramFile diagram;
        private final DiagramFile key;

        Diagram(DiagramFile diagram, DiagramFile key) {
            this.diagram = diagram;
            this.key = key;
        }

        DiagramFile getDiagram() {
            return diagram;
        }

        DiagramFile getKey() {
            return key;
        }

        boolean hasKey() {
            return key != null;
        }

    }

    static class DiagramFile {

        private final String filename;
        private final String content;

        DiagramFile(String filename, String content) {
            this.filename = filename;
            this.content = content;
        }

        String getFilename() {
            return filename;
        }

        String getContent() {
            return content;
        }

    }

}