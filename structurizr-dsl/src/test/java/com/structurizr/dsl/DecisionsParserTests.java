package com.structurizr.dsl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecisionsParserTests extends AbstractTests {

    private final DecisionsParser parser = new DecisionsParser();

    @Test
    void parse_ThrowsAnException_WhenThereAreTooManyTokens() {
        try {
            parser.parse(new DecisionsDslContext(null, null), tokens("decisions", "path", "fqn", "extra"));
            fail();
        } catch (Exception e) {
            assertEquals("Too many tokens, expected: !decisions <path> <type|fqn>", e.getMessage());
        }
    }

    @Test
    void parseExclude_ThrowsAnException_WhenThereAreTooFewTokens() {
        try {
            parser.parseExclude(new DecisionsDslContext(null, null), tokens("exclude"));
            fail();
        } catch (Exception e) {
            assertEquals("Expected: exclude <filename|regex> [filename|regex]", e.getMessage());
        }
    }

    @Test
    void parseExclude_WithSingleRegex() {
        DecisionsDslContext context = new DecisionsDslContext(null, null);
        parser.parseExclude(context, tokens("exclude", "regex"));

        assertTrue(context.getExcludes().contains("regex"));
    }

    @Test
    void parseExclude_WithMultipleRegexes() {
        DecisionsDslContext context = new DecisionsDslContext(null, null);
        parser.parseExclude(context, tokens("exclude", "regex1", "regex2"));

        assertTrue(context.getExcludes().contains("regex1"));
        assertTrue(context.getExcludes().contains("regex2"));
    }

}