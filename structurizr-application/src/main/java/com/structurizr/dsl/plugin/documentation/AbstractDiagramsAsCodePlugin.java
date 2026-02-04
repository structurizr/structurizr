package com.structurizr.dsl.plugin.documentation;

import com.structurizr.dsl.StructurizrDslPlugin;

abstract class AbstractDiagramsAsCodePlugin implements StructurizrDslPlugin {

    protected static final String DIAGRAM_IN_MARKDOWN_PATTERN = "((^```%s\\R)((^.*\\R)+?)(^```))";;
    protected static final int DIAGRAM_IN_MARKDOWN_SOURCE_WITH_MARKERS_GROUP = 1;
    protected static final int DIAGRAM_IN_MARKDOWN_SOURCE_WITHOUT_MARKERS_GROUP = 3;

    protected static final String DIAGRAM_IN_ASCIIDOC_PATTERN = "((^\\[%s.*?]\\R)(^\\.\\.\\.\\.\\R)((^.*\\R)+?)(^\\.\\.\\.\\.))";
    protected static final int DIAGRAM_IN_ASCIIDOC_SOURCE_WITH_MARKERS_GROUP = 1;
    protected static final int DIAGRAM_IN_ASCIIDOC_SOURCE_WITHOUT_MARKERS_GROUP = 4;

    protected static final String MARKDOWN_IMAGE_TEMPLATE = "![](%s/%s/%s)";
    protected static final String ASCIIDOC_IMAGE_TEMPLATE = "image::%s/%s/%s[]";

    protected static final String PNG_FORMAT = "png";
    protected static final String SVG_FORMAT = "svg";

    public AbstractDiagramsAsCodePlugin() {
    }

}