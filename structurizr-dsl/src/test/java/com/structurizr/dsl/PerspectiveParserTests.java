package com.structurizr.dsl;

import com.structurizr.model.ModelItem;
import com.structurizr.model.Perspective;
import com.structurizr.model.SoftwareSystem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class PerspectiveParserTests extends AbstractTests {

    private final PerspectiveParser parser = new PerspectiveParser();

    @Test
    void test_parse_SingleLine_ThrowsAnException_WhenThereAreTooManyTokens() {
        try {
            PerspectivesDslContext context = new PerspectivesDslContext((ModelItem)null);
            parser.parse(context, tokens("name", "description", "value", "extra"));
            fail();
        } catch (Exception e) {
            assertEquals("Too many tokens, expected: <name> <description> [value]", e.getMessage());
        }
    }

    @Test
    void test_parse_SingleLine_ThrowsAnException_WhenNoNameIsSpecified() {
        try {
            SoftwareSystem softwareSystem = model.addSoftwareSystem("Name", "Description");
            PerspectivesDslContext context = new PerspectivesDslContext(softwareSystem);
            parser.parse(context, tokens());
            fail();
        } catch (Exception e) {
            assertEquals("Expected: <name> <description> [value]", e.getMessage());
        }
    }

    @Test
    void test_parse_SingleLine_ThrowsAnException_WhenNoDescriptionIsSpecified() {
        try {
            SoftwareSystem softwareSystem = model.addSoftwareSystem("Name", "Description");
            PerspectivesDslContext context = new PerspectivesDslContext(softwareSystem);
            parser.parse(context, tokens("name"));
            fail();
        } catch (Exception e) {
            assertEquals("Expected: <name> <description> [value]", e.getMessage());
        }
    }

    @Test
    void test_parse_SingleLine_AddsThePerspective_WhenADescriptionIsSpecified() {
        SoftwareSystem softwareSystem = model.addSoftwareSystem("Name", "Description");
        PerspectivesDslContext context = new PerspectivesDslContext(softwareSystem);
        parser.parse(context, tokens("Security", "Description"));

        Perspective perspective = softwareSystem.getPerspectives().stream().filter(p -> p.getName().equals("Security")).findFirst().get();
        assertEquals("Description", perspective.getDescription());
        assertEquals("", perspective.getValue());
    }

    @Test
    void test_parse_SingleLine_AddsThePerspective_WhenADescriptionAndValueIsSpecified() {
        SoftwareSystem softwareSystem = model.addSoftwareSystem("Name", "Description");
        PerspectivesDslContext context = new PerspectivesDslContext(softwareSystem);
        parser.parse(context, tokens("Security", "Description", "Value"));

        Perspective perspective = softwareSystem.getPerspectives().stream().filter(p -> p.getName().equals("Security")).findFirst().get();
        assertEquals("Description", perspective.getDescription());
        assertEquals("Value", perspective.getValue());
    }

    @Test
    void test_parse_MultiLine() {
        Perspective perspective = parser.parse(tokens("perspective", "Name"));

        assertEquals("Name", perspective.getName());
    }

    @Test
    void test_parseDescription_ThrowsException_WhenNoDescriptionIsSpecified() {
        try {
            Perspective perspective = new Perspective("Name");
            PerspectiveDslContext context = new PerspectiveDslContext(perspective, null);
            parser.parseDescription(context, tokens("description"));
            fail();
        } catch (Exception e) {
            assertEquals("Expected: description <description>", e.getMessage());
        }
    }

    @Test
    void test_parseDescription_ThrowsException_WhenThereAreTooManyTokens() {
        try {
            Perspective perspective = new Perspective("Name");
            PerspectiveDslContext context = new PerspectiveDslContext(perspective, null);
            parser.parseDescription(context, tokens("description", "Description", "extra"));
            fail();
        } catch (Exception e) {
            assertEquals("Too many tokens, expected: description <description>", e.getMessage());
        }
    }

    @Test
    void test_parseDescription() {
        Perspective perspective = new Perspective("Name");
        PerspectiveDslContext context = new PerspectiveDslContext(perspective, null);
        parser.parseDescription(context, tokens("description", "Description"));

        assertEquals("Description", perspective.getDescription());
    }

    @Test
    void test_parseValue_ThrowsException_WhenNoDescriptionIsSpecified() {
        try {
            Perspective perspective = new Perspective("Name");
            PerspectiveDslContext context = new PerspectiveDslContext(perspective, null);
            parser.parseValue(context, tokens("value"));
            fail();
        } catch (Exception e) {
            assertEquals("Expected: value <value>", e.getMessage());
        }
    }

    @Test
    void test_parseValue_ThrowsException_WhenThereAreTooManyTokens() {
        try {
            Perspective perspective = new Perspective("Name");
            PerspectiveDslContext context = new PerspectiveDslContext(perspective, null);
            parser.parseValue(context, tokens("value", "Value", "extra"));
            fail();
        } catch (Exception e) {
            assertEquals("Too many tokens, expected: value <value>", e.getMessage());
        }
    }

    @Test
    void test_parseValue() {
        Perspective perspective = new Perspective("Name");
        PerspectiveDslContext context = new PerspectiveDslContext(perspective, null);
        parser.parseValue(context, tokens("value", "Value"));

        assertEquals("Value", perspective.getValue());
    }

}