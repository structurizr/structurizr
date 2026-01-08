package com.structurizr.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HtmlUtilsTests {

    @Test
    void test_filterHTML() {
        assertEquals("Here is some text.", HtmlUtils.filterHtml("Here is <!-- <rdf>...</rdf> -->some text."));
        assertEquals("Here is some text.", HtmlUtils.filterHtml("Here is <!-- <rdf/> -->some text."));
        assertEquals("Here is some text.", HtmlUtils.filterHtml("<b>Here</b> is <i>some</i> text."));
        assertEquals("Here is a link.", HtmlUtils.filterHtml("Here is <a href=\"http://www.google.com\">a link</a>."));
        assertEquals("Here is a link.", HtmlUtils.filterHtml("Here is <a \nhref=\"http://www.google.com\">a link</a>."));
        assertEquals("Here is some text", HtmlUtils.filterHtml("Here is &lt;some&gt; text"));
        assertEquals("alert('hello')", HtmlUtils.filterHtml("<script>alert('hello')</script>"));
    }

}