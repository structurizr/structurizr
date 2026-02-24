package com.structurizr.server.component.search;

import com.structurizr.Workspace;
import com.structurizr.documentation.Decision;
import com.structurizr.model.Component;
import com.structurizr.model.Container;
import com.structurizr.model.Element;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;

abstract class AbstractSearchAdapter implements SearchAdapter {

    protected static final String DOCUMENTATION_PATH = "/documentation";
    protected static final String DIAGRAMS_PATH = "/diagrams";
    protected static final String DECISIONS_PATH = "/decisions";

    protected static final String MARKDOWN_SECTION_HEADING = "## ";
    protected static final String MARKDOWN_SUBSECTION_HEADING = "### ";

    protected static final String ASCIIDOC_SECTION_HEADING = "== ";
    protected static final String ASCIIDOC_SUBSECTION_HEADING = "=== ";

    protected static final String NEWLINE = "\n";

    protected static final int SNIPPET_LENGTH = 400;

    protected String urlEncode(String value) throws Exception {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
    }

    protected String calculateUrlForSection(Element element, int sectionNumber, int subSectionNumber) throws Exception {
        String url = "";
        if (element instanceof Component) {
            url = "/" + urlEncode(element.getParent().getParent().getName()) + "/" + urlEncode(element.getParent().getName()) + "/" + urlEncode(element.getName());
        } else if (element instanceof Container) {
            url = "/" + urlEncode(element.getParent().getName()) + "/" + urlEncode(element.getName());
        } else if (element instanceof SoftwareSystem) {
            url = "/" + urlEncode(element.getName());
        }

        if (sectionNumber > 0) {
            url = url + "#" + sectionNumber;
        }

        if (subSectionNumber > 0) {
            url = url + "." + subSectionNumber;
        }

        return url;
    }

    protected String calculateUrlForDecision(Element element, Decision decision) throws Exception {
        String url = "";
        if (element instanceof Component) {
            url = "/" + urlEncode(element.getParent().getParent().getName()) + "/" + urlEncode(element.getParent().getName()) + "/" + urlEncode(element.getName());
        } else if (element instanceof Container) {
            url = "/" + urlEncode(element.getParent().getName()) + "/" + urlEncode(element.getName());
        } else if (element instanceof SoftwareSystem) {
            url = "/" + urlEncode(element.getName());
        }

        url = url + "#" + decision.getId();

        return url;
    }

    protected String toString(long workspaceId) {
        // 1 -> 0000000000000001 ... this is done so that we can search for specific IDs, rather than all including '1'
        NumberFormat format = new DecimalFormat("0000000000000000");
        return format.format(workspaceId);
    }

    protected String toString(String s) {
        if (StringUtils.isNullOrEmpty(s)) {
            return "";
        } else {
            return s;
        }
    }

    protected void indexDocumentation(Workspace workspace, Element element, String documentationContent) throws Exception {
        // split the entire documentation content up into:
        // - sections (identified by a ## or == heading)
        // - sub-sections (identified by a ### or === heading)

        String title = "";
        StringBuilder content = new StringBuilder();
        String[] lines = documentationContent.split(NEWLINE);
        int sectionNumber = 0;
        int subSectionNumber = 0;

        for (String line : lines) {
            if (line.startsWith(MARKDOWN_SECTION_HEADING) || line.startsWith(ASCIIDOC_SECTION_HEADING)) {
                indexDocumentationSection(title, content.toString(), sectionNumber, subSectionNumber, workspace, element);
                title = line.substring(MARKDOWN_SECTION_HEADING.length() - 1).trim();
                content = new StringBuilder();
                sectionNumber++;
                subSectionNumber = 0;
            } else if (line.startsWith(MARKDOWN_SUBSECTION_HEADING) || line.startsWith(ASCIIDOC_SUBSECTION_HEADING)) {
                indexDocumentationSection(title, content.toString(), sectionNumber, subSectionNumber, workspace, element);
                title = line.substring(MARKDOWN_SUBSECTION_HEADING.length()-1).trim();
                content = new StringBuilder();
                subSectionNumber++;

            } else {
                content.append(line);
                content.append(NEWLINE);
            }
        }

        if (!content.isEmpty()) {
            indexDocumentationSection(title, content.toString(), sectionNumber, subSectionNumber, workspace, element);
        }
    }

    protected abstract void indexDocumentationSection(String title, String content, int sectionNumber, int subSectionNumber, Workspace workspace, Element element) throws Exception;

    protected String filterMarkup(String source) {
        source = source.replaceAll("#", "");
        source = source.replaceAll("=", "");

        return source;
    }

}