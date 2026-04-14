package com.structurizr.dsl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocsParserTests extends AbstractTests {

    private final DocsParser parser = new DocsParser();

    @Test
    void parse_ThrowsAnException_WhenThereAreTooManyTokens() {
        try {
            parser.parse(new DocumentationDslContext(null, null), tokens("docs", "path", "fqn", "extra"));
            fail();
        } catch (Exception e) {
            assertEquals("Too many tokens, expected: !docs <path> <fqn>", e.getMessage());
        }
    }

    @Test
    void parseExclude_ThrowsAnException_WhenThereAreTooFewTokens() {
        try {
            parser.parseExclude(new DocumentationDslContext(null, null), tokens("exclude"));
            fail();
        } catch (Exception e) {
            assertEquals("Expected: exclude <regex> [regex]", e.getMessage());
        }
    }

    @Test
    void parseExclude_WithSingleRegex() {
        DocumentationDslContext context = new DocumentationDslContext(null, null);
        parser.parseExclude(context, tokens("exclude", "regex"));

        assertTrue(context.getExcludes().contains("regex"));
    }

    @Test
    void parseExclude_WithMultipleRegexes() {
        DocumentationDslContext context = new DocumentationDslContext(null, null);
        parser.parseExclude(context, tokens("exclude", "regex1", "regex2"));

        assertTrue(context.getExcludes().contains("regex1"));
        assertTrue(context.getExcludes().contains("regex2"));
    }

}