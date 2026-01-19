package com.structurizr.dsl;

import com.structurizr.view.AutomaticLayout;
import com.structurizr.view.SystemLandscapeView;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AutoLayoutParserTests extends AbstractTests {

    private AutoLayoutParser parser = new AutoLayoutParser();

    @Test
    void test_parse_ThrowsAnException_WhenThereAreTooManyTokens() {
        try {
            parser.parse(null, tokens("autoLayout", "rankDirection", "rankSeparation", "nodeSeparation", "edgeSeparation", "vertices", "extra"));
            fail();
        } catch (Exception e) {
            assertEquals("Too many tokens, expected: autoLayout [rankDirection] [rankSeparation] [nodeSeparation] [edgeSeparation] [vertices]", e.getMessage());
        }
    }

    @Test
    void test_parse_EnablesAutoLayoutWithSomeDefaults() {
        SystemLandscapeView view = workspace.getViews().createSystemLandscapeView("key", "description");
        SystemLandscapeViewDslContext context = new SystemLandscapeViewDslContext(view);
        context.setWorkspace(workspace);

        assertNull(view.getAutomaticLayout());
        parser.parse(context, tokens("autoLayout"));
        assertEquals(AutomaticLayout.RankDirection.LeftRight, view.getAutomaticLayout().getRankDirection());
        assertEquals(100, view.getAutomaticLayout().getRankSeparation());
        assertEquals(50, view.getAutomaticLayout().getNodeSeparation());
        assertEquals(50, view.getAutomaticLayout().getEdgeSeparation());
        assertTrue(view.getAutomaticLayout().isVertices());
    }

    @Test
    void test_parse_EnablesAutoLayoutWithAllSettings() {
        SystemLandscapeView view = workspace.getViews().createSystemLandscapeView("key", "description");
        SystemLandscapeViewDslContext context = new SystemLandscapeViewDslContext(view);
        context.setWorkspace(workspace);

        assertNull(view.getAutomaticLayout());
        parser.parse(context, tokens("autoLayout", "lr", "111", "222", "333", "false"));
        assertEquals(AutomaticLayout.RankDirection.LeftRight, view.getAutomaticLayout().getRankDirection());
        assertEquals(111, view.getAutomaticLayout().getRankSeparation());
        assertEquals(222, view.getAutomaticLayout().getNodeSeparation());
        assertEquals(333, view.getAutomaticLayout().getEdgeSeparation());
        assertFalse(view.getAutomaticLayout().isVertices());
    }

    @Test
    void test_parse_EnablesAutoLayoutWithAValidRankDirection() {
        SystemLandscapeView view = workspace.getViews().createSystemLandscapeView("key", "description");
        SystemLandscapeViewDslContext context = new SystemLandscapeViewDslContext(view);
        context.setWorkspace(workspace);

        assertNull(view.getAutomaticLayout());
        parser.parse(context, tokens("autoLayout", "lr"));
        assertEquals(AutomaticLayout.RankDirection.LeftRight, view.getAutomaticLayout().getRankDirection());
        assertEquals(100, view.getAutomaticLayout().getRankSeparation());
        assertEquals(50, view.getAutomaticLayout().getNodeSeparation());
        assertEquals(50, view.getAutomaticLayout().getEdgeSeparation());
        assertTrue(view.getAutomaticLayout().isVertices());
    }

    @Test
    void test_parse_ThrowsAnException_WhenAnInvalidRankDirectionIsSpecified() {
        SystemLandscapeView view = workspace.getViews().createSystemLandscapeView("key", "description");
        SystemLandscapeViewDslContext context = new SystemLandscapeViewDslContext(view);
        context.setWorkspace(workspace);

        try {
            parser.parse(context, tokens("autoLayout", "hello"));
            fail();
        } catch (Exception e) {
            assertEquals("Valid rank directions are: tb|bt|lr|rl", e.getMessage());
        }
    }

    @Test
    void test_parse_EnablesAutoLayoutWithAValidRankSeparation() {
        SystemLandscapeView view = workspace.getViews().createSystemLandscapeView("key", "description");
        SystemLandscapeViewDslContext context = new SystemLandscapeViewDslContext(view);
        context.setWorkspace(workspace);

        assertNull(view.getAutomaticLayout());
        parser.parse(context, tokens("autoLayout", "tb", "123"));
        assertEquals(AutomaticLayout.RankDirection.TopBottom, view.getAutomaticLayout().getRankDirection());
        assertEquals(123, view.getAutomaticLayout().getRankSeparation());
        assertEquals(50, view.getAutomaticLayout().getNodeSeparation());
        assertEquals(50, view.getAutomaticLayout().getEdgeSeparation());
        assertTrue(view.getAutomaticLayout().isVertices());
    }

    @Test
    void test_parse_ThrowsAnException_WhenAnInvalidRankSeparationIsSpecified() {
        SystemLandscapeView view = workspace.getViews().createSystemLandscapeView("key", "description");
        SystemLandscapeViewDslContext context = new SystemLandscapeViewDslContext(view);
        context.setWorkspace(workspace);

        try {
            parser.parse(context, tokens("autoLayout", "tb", "hello"));
            fail();
        } catch (Exception e) {
            assertEquals("Rank separation must be positive integer in pixels", e.getMessage());
        }
    }

    @Test
    void test_parse_EnablesAutoLayoutWithAValidNodeSeparation() {
        SystemLandscapeView view = workspace.getViews().createSystemLandscapeView("key", "description");
        SystemLandscapeViewDslContext context = new SystemLandscapeViewDslContext(view);
        context.setWorkspace(workspace);

        assertNull(view.getAutomaticLayout());
        parser.parse(context, tokens("autoLayout", "tb", "123", "456"));
        assertEquals(AutomaticLayout.RankDirection.TopBottom, view.getAutomaticLayout().getRankDirection());
        assertEquals(123, view.getAutomaticLayout().getRankSeparation());
        assertEquals(456, view.getAutomaticLayout().getNodeSeparation());
    }

    @Test
    void test_parse_ThrowsAnException_WhenAnInvalidNodeSeparationIsSpecified() {
        SystemLandscapeView view = workspace.getViews().createSystemLandscapeView("key", "description");
        SystemLandscapeViewDslContext context = new SystemLandscapeViewDslContext(view);
        context.setWorkspace(workspace);

        try {
            parser.parse(context, tokens("autoLayout", "tb", "300", "hello"));
            fail();
        } catch (Exception e) {
            assertEquals("Node separation must be positive integer in pixels", e.getMessage());
        }
    }

}