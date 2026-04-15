package com.structurizr.documentation;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a piece of documentation content ... a section or a decision.
 */
public abstract class DocumentationContent {

    // ![alt](image.png)
    private static final Pattern IMAGE_IN_MARKDOWN_PATTERN = Pattern.compile("!\\[.*]\\((.*)\\)");

    // image::image.jpg[alt]
    // image:image.jpg[alt]
    private static final Pattern IMAGE_IN_ASCIIDOC_PATTERN = Pattern.compile("image:{1,2}(.*)\\[.*]");

    // elementId is here for backwards compatibility
    private String elementId;

    private String content;
    private Format format;

    DocumentationContent() {
    }

    /**
     * Gets the ID of the element that this documentation content is associated with.
     * Please note this is unused, and only here for backwards compatibility.
     *
     * @return      the element ID, as a String
     */
    public String getElementId() {
        return elementId;
    }

    void setElementId(String elementId) {
        this.elementId = elementId;
    }

    /**
     * Gets the content.
     *
     * @return      the content, as a String
     */
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        if (content != null) {
            this.content = content.replaceAll("\\r\\n|\\r|\\n", "\n");
        }
    }

    /**
     * Gets the format of this content.
     *
     * @return      Markdown or AsciiDoc
     */
    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    /**
     * Finds the images in this documentation content.
     *
     * @return      a Set of image names
     */
    public Set<String> findImages() {
        Set<String> images = new HashSet<>();

        Pattern pattern;
        if (format == Format.Markdown) {
            pattern = IMAGE_IN_MARKDOWN_PATTERN;
        } else {
            pattern = IMAGE_IN_ASCIIDOC_PATTERN;
        }

        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String image = matcher.group(1);
            images.add(image);
        }

        return images;
    }

}