package com.structurizr.dsl.plugin.documentation;

import com.structurizr.Workspace;
import com.structurizr.documentation.Decision;
import com.structurizr.documentation.Documentable;
import com.structurizr.documentation.Format;
import com.structurizr.documentation.Section;
import com.structurizr.dsl.StructurizrDslPluginContext;
import com.structurizr.importer.diagrams.plantuml.PlantUMLEncoder;
import com.structurizr.util.StringUtils;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.structurizr.importer.diagrams.plantuml.PlantUMLImporter.PLANTUML_FORMAT_PROPERTY;
import static com.structurizr.importer.diagrams.plantuml.PlantUMLImporter.PLANTUML_URL_PROPERTY;

public class PlantUML extends AbstractDiagramsAsCodePlugin {

    private static final String PLANTUML = "plantuml";
    private static final Pattern PLANTUML_IN_MARKDOWN_PATTERN = Pattern.compile(String.format(DIAGRAM_IN_MARKDOWN_PATTERN, PLANTUML), Pattern.MULTILINE);
    private static final Pattern PLANTUML_IN_ASCIIDOC_PATTERN = Pattern.compile(String.format(DIAGRAM_IN_ASCIIDOC_PATTERN, PLANTUML), Pattern.MULTILINE);

    @Override
    public void run(StructurizrDslPluginContext context) {
        Workspace workspace = context.getWorkspace();
        Set<Documentable> documentables = workspace.getModel().getElements().stream().filter(e -> e instanceof Documentable).map(e -> (Documentable)e).collect(Collectors.toSet());
        documentables.add(workspace);
        documentables.forEach(documentable -> encodePlantUML(context, documentable));
    }

    private void encodePlantUML(StructurizrDslPluginContext context, Documentable documentable) {
        try {
            for (Section section : documentable.getDocumentation().getSections()) {
                if (section.getFormat() == Format.Markdown) {
                    section.setContent(encodePlantUMLInMarkdown(context, section.getContent()));
                } else {
                    section.setContent(encodePlantUMLInAsciiDoc(context, section.getContent()));
                }
            }
            for (Decision decision : documentable.getDocumentation().getDecisions()) {
                if (decision.getFormat() == Format.Markdown) {
                    decision.setContent(encodePlantUMLInMarkdown(context, decision.getContent()));
                } else {
                    decision.setContent(encodePlantUMLInAsciiDoc(context, decision.getContent()));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String encodePlantUMLInMarkdown(StructurizrDslPluginContext context, String content) throws Exception {
        String result = content;
        String url = url(context);
        String format = format(context);

        Matcher matcher = PLANTUML_IN_MARKDOWN_PATTERN.matcher(content);
        while (matcher.find()) {
            String sourceWithMarkers = matcher.group(DIAGRAM_IN_MARKDOWN_SOURCE_WITH_MARKERS_GROUP);
            String sourceWithoutMarkers = matcher.group(DIAGRAM_IN_MARKDOWN_SOURCE_WITHOUT_MARKERS_GROUP);

            result = result.replace(
                    sourceWithMarkers,
                    String.format(MARKDOWN_IMAGE_TEMPLATE, url, format, new PlantUMLEncoder().encode(sourceWithoutMarkers))
            );
        }

        return result;
    }

    private String encodePlantUMLInAsciiDoc(StructurizrDslPluginContext context, String content) throws Exception {
        String result = content;
        String url = url(context);
        String format = format(context);

        Matcher matcher = PLANTUML_IN_ASCIIDOC_PATTERN.matcher(content);
        while (matcher.find()) {
            String sourceWithMarkers = matcher.group(DIAGRAM_IN_ASCIIDOC_SOURCE_WITH_MARKERS_GROUP);
            String sourceWithoutMarkers = matcher.group(DIAGRAM_IN_ASCIIDOC_SOURCE_WITHOUT_MARKERS_GROUP);

            result = result.replace(
                    sourceWithMarkers,
                    String.format(ASCIIDOC_IMAGE_TEMPLATE, url, format, new PlantUMLEncoder().encode(sourceWithoutMarkers))
            );
        }

        return result;
    }

    private String url(StructurizrDslPluginContext context) {
        String url = context.getWorkspace().getViews().getConfiguration().getProperties().get(PLANTUML_URL_PROPERTY);
        if (StringUtils.isNullOrEmpty(url)) {
            throw new IllegalArgumentException("Please define a viewset property named " + PLANTUML_URL_PROPERTY + " to specify your PlantUML server");
        }

        return url;
    }

    private String format(StructurizrDslPluginContext context) {
        String format = context.getWorkspace().getViews().getConfiguration().getProperties().get(PLANTUML_FORMAT_PROPERTY);
        if (StringUtils.isNullOrEmpty(format)) {
            format = SVG_FORMAT;
        }

        if (!format.equals(PNG_FORMAT) && !format.equals(SVG_FORMAT)) {
            throw new IllegalArgumentException(String.format("Expected a format of %s or %s", PNG_FORMAT, SVG_FORMAT));
        }

        return format;
    }

}