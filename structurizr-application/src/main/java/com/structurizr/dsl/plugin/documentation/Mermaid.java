package com.structurizr.dsl.plugin.documentation;

import com.structurizr.Workspace;
import com.structurizr.documentation.Decision;
import com.structurizr.documentation.Documentable;
import com.structurizr.documentation.Format;
import com.structurizr.documentation.Section;
import com.structurizr.dsl.StructurizrDslPluginContext;
import com.structurizr.importer.diagrams.mermaid.MermaidEncoder;
import com.structurizr.util.StringUtils;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.structurizr.importer.diagrams.mermaid.MermaidImporter.*;

public class Mermaid extends AbstractDiagramsAsCodePlugin {

    private static final String MERMAID = "mermaid";
    private static final Pattern MERMAID_IN_MARKDOWN_PATTERN = Pattern.compile(String.format(DIAGRAM_IN_MARKDOWN_PATTERN, MERMAID), Pattern.MULTILINE);
    private static final Pattern MERMAID_IN_ASCIIDOC_PATTERN = Pattern.compile(String.format(DIAGRAM_IN_ASCIIDOC_PATTERN, MERMAID), Pattern.MULTILINE);

    @Override
    public void run(StructurizrDslPluginContext context) {
        Workspace workspace = context.getWorkspace();
        Set<Documentable> documentables = workspace.getModel().getElements().stream().filter(e -> e instanceof Documentable).map(e -> (Documentable)e).collect(Collectors.toSet());
        documentables.add(workspace);
        documentables.forEach(documentable -> encodeMermaid(context, documentable));
    }

    private void encodeMermaid(StructurizrDslPluginContext context, Documentable documentable) {
        for (Section section : documentable.getDocumentation().getSections()) {
            if (section.getFormat() == Format.Markdown) {
                section.setContent(encodeMermaidInMarkdown(context, section.getContent()));
            } else {
                section.setContent(encodeMermaidInAsciiDoc(context, section.getContent()));
            }
        }
        for (Decision decision : documentable.getDocumentation().getDecisions()) {
            if (decision.getFormat() == Format.Markdown) {
                decision.setContent(encodeMermaidInMarkdown(context, decision.getContent()));
            } else {
                decision.setContent(encodeMermaidInAsciiDoc(context, decision.getContent()));
            }
        }
    }

    private String encodeMermaidInMarkdown(StructurizrDslPluginContext context, String content) {
        String result = content;
        String url = url(context);
        String format = format(context);
        boolean compress = compress(context);

        Matcher matcher = MERMAID_IN_MARKDOWN_PATTERN.matcher(content);
        while (matcher.find()) {
            String sourceWithMarkers = matcher.group(DIAGRAM_IN_MARKDOWN_SOURCE_WITH_MARKERS_GROUP);
            String sourceWithoutMarkers = matcher.group(DIAGRAM_IN_MARKDOWN_SOURCE_WITHOUT_MARKERS_GROUP);

            result = result.replace(
                            sourceWithMarkers,
                            String.format(MARKDOWN_IMAGE_TEMPLATE, url, format, new MermaidEncoder().encode(sourceWithoutMarkers, compress))
                        );
        }

        return result;
    }

    private String encodeMermaidInAsciiDoc(StructurizrDslPluginContext context, String content) {
        String result = content;
        String url = url(context);
        String format = format(context);
        boolean compress = compress(context);

        Matcher matcher = MERMAID_IN_ASCIIDOC_PATTERN.matcher(content);
        while (matcher.find()) {
            String sourceWithMarkers = matcher.group(DIAGRAM_IN_ASCIIDOC_SOURCE_WITH_MARKERS_GROUP);
            String sourceWithoutMarkers = matcher.group(DIAGRAM_IN_ASCIIDOC_SOURCE_WITHOUT_MARKERS_GROUP);

            result = result.replace(
                        sourceWithMarkers,
                        String.format(ASCIIDOC_IMAGE_TEMPLATE, url, format, new MermaidEncoder().encode(sourceWithoutMarkers, compress))
                    );
        }

        return result;
    }

    private String url(StructurizrDslPluginContext context) {
        String url = context.getWorkspace().getViews().getConfiguration().getProperties().get(MERMAID_URL_PROPERTY);
        if (StringUtils.isNullOrEmpty(url)) {
            throw new IllegalArgumentException("Please define a viewset property named " + MERMAID_URL_PROPERTY + " to specify your Mermaid server");
        }

        return url;
    }

    private String format(StructurizrDslPluginContext context) {
        String format = context.getWorkspace().getViews().getConfiguration().getProperties().get(MERMAID_FORMAT_PROPERTY);
        if (StringUtils.isNullOrEmpty(format)) {
            format = SVG_FORMAT;
        }

        if (!format.equals(PNG_FORMAT) && !format.equals(SVG_FORMAT)) {
            throw new IllegalArgumentException(String.format("Expected a format of %s or %s", PNG_FORMAT, SVG_FORMAT));
        }

        return format;
    }

    boolean compress(StructurizrDslPluginContext context) {
        String compress = context.getWorkspace().getViews().getConfiguration().getProperties().get(MERMAID_COMPRESS_PROPERTY);
        if (StringUtils.isNullOrEmpty(compress)) {
            compress = "true";
        }

        return compress.equalsIgnoreCase("true");
    }

}