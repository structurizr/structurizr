package com.structurizr.view;

import com.structurizr.Workspace;
import com.structurizr.http.HttpClient;
import com.structurizr.model.Relationship;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.model.Tags;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class ThemeUtilsTests {

    @Test
    void loadThemes_DoesNothingWhenNoThemesAreDefined() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        ThemeUtils.loadThemes(workspace);

        // there should still be zero styles in the workspace
        assertEquals(0, workspace.getViews().getConfiguration().getStyles().getElements().size());
    }

    @Test
    @Tag("IntegrationTest")
    void loadThemes_LoadsThemesWhenThemesAreDefined_AndContentTypeIsApplicationJson() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Name");
        softwareSystem.addTags("Amazon Web Services - Alexa For Business");
        workspace.getViews().getConfiguration().setThemes("https://static.structurizr.com/themes/amazon-web-services-2020.04.30/theme.json");

        HttpClient httpClient = new HttpClient();
        httpClient.allow(".*");
        ThemeUtils.loadThemes(workspace, httpClient);

        // there should still be zero styles in the workspace
        assertEquals(0, workspace.getViews().getConfiguration().getStyles().getElements().size());

        // but we should be able to find a style included in the theme
        ElementStyle style = workspace.getViews().getConfiguration().getStyles().findElementStyle(softwareSystem);
        assertNotNull(style);
        assertEquals("#d6242d", style.getStroke());
        assertEquals("#d6242d", style.getColor());
        assertEquals("https://static.structurizr.com/themes/amazon-web-services-2020.04.30/alexa-for-business.png", style.getIcon());
    }

    @Test
    @Tag("IntegrationTest")
    void loadThemes_LoadsThemesWhenThemesAreDefined_AndContentTypeIsPlainText() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Name");
        softwareSystem.addTags("Amazon Web Services - Alexa For Business");
        workspace.getViews().getConfiguration().setThemes("https://raw.githubusercontent.com/structurizr/themes/refs/heads/master/amazon-web-services-2020.04.30/theme.json");

        HttpClient httpClient = new HttpClient();
        httpClient.allow(".*");
        ThemeUtils.loadThemes(workspace, httpClient);

        // there should still be zero styles in the workspace
        assertEquals(0, workspace.getViews().getConfiguration().getStyles().getElements().size());

        // but we should be able to find a style included in the theme
        ElementStyle style = workspace.getViews().getConfiguration().getStyles().findElementStyle(softwareSystem);
        assertNotNull(style);
        assertEquals("#d6242d", style.getStroke());
        assertEquals("#d6242d", style.getColor());
        assertEquals("https://raw.githubusercontent.com/structurizr/themes/refs/heads/master/amazon-web-services-2020.04.30/alexa-for-business.png", style.getIcon());
    }

    @Test
    void toJson() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        assertEquals("{\n" +
                "  \"name\" : \"Name\",\n" +
                "  \"description\" : \"Description\"\n" +
                "}", ThemeUtils.toJson(workspace));

        workspace.getViews().getConfiguration().getStyles().addElementStyle(Tags.ELEMENT).background("#ff0000");
        workspace.getViews().getConfiguration().getStyles().addRelationshipStyle(Tags.RELATIONSHIP).color("#ff0000");
        assertEquals("""
                {
                  "name" : "Name",
                  "description" : "Description",
                  "elements" : [ {
                    "tag" : "Element",
                    "background" : "#ff0000"
                  } ],
                  "relationships" : [ {
                    "tag" : "Relationship",
                    "color" : "#ff0000"
                  } ]
                }""", ThemeUtils.toJson(workspace));
    }

    @Test
    void findElementStyle_WithThemes() {
        Workspace workspace = new Workspace("Name", "Description");
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Name");
        workspace.getViews().getConfiguration().getStyles().addElementStyle("Element").shape(Shape.RoundedBox);

        // theme 1
        Collection<ElementStyle> elementStyles = new ArrayList<>();
        Collection<RelationshipStyle> relationshipStyles = new ArrayList<>();
        elementStyles.add(new ElementStyle("Element").shape(Shape.Box).background("#000000").color("#ffffff"));
        workspace.getViews().getConfiguration().getStyles().addStylesFromTheme(new Theme(elementStyles, relationshipStyles));

        // theme 2
        elementStyles = new ArrayList<>();
        relationshipStyles = new ArrayList<>();
        elementStyles.add(new ElementStyle("Element").background("#ff0000"));
        workspace.getViews().getConfiguration().getStyles().addStylesFromTheme(new Theme(elementStyles, relationshipStyles));

        ElementStyle style = workspace.getViews().getConfiguration().getStyles().findElementStyle(softwareSystem);
        assertNull(style.getWidth());
        assertNull(style.getHeight());
        assertEquals("#ff0000", style.getBackground()); // from theme 2
        assertEquals("#ffffff", style.getColor()); // from theme 1
        assertEquals(Integer.valueOf(24), style.getFontSize());
        assertEquals(Shape.RoundedBox, style.getShape()); // from workspace
        assertNull(style.getIcon());
        assertEquals(Border.Solid, style.getBorder());
        assertEquals("#b20000", style.getStroke());
        assertEquals(Integer.valueOf(100), style.getOpacity());
        assertEquals(true, style.getMetadata());
        assertEquals(true, style.getDescription());
    }

    @Test
    void findRelationshipStyle_WithThemes() {
        Workspace workspace = new Workspace("Name", "Description");
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Name");
        Relationship relationship = softwareSystem.uses(softwareSystem, "Uses");
        workspace.getViews().getConfiguration().getStyles().addRelationshipStyle("Relationship").dashed(false);

        // theme 1
        Collection<ElementStyle> elementStyles = new ArrayList<>();
        Collection<RelationshipStyle> relationshipStyles = new ArrayList<>();
        relationshipStyles.add(new RelationshipStyle("Relationship").color("#ff0000").thickness(4));
        workspace.getViews().getConfiguration().getStyles().addStylesFromTheme(new Theme(elementStyles, relationshipStyles));

        // theme 2
        elementStyles = new ArrayList<>();
        relationshipStyles = new ArrayList<>();
        relationshipStyles.add(new RelationshipStyle("Relationship").color("#0000ff"));
        workspace.getViews().getConfiguration().getStyles().addStylesFromTheme(new Theme(elementStyles, relationshipStyles));

        RelationshipStyle style = workspace.getViews().getConfiguration().getStyles().findRelationshipStyle(relationship);
        assertEquals(Integer.valueOf(4), style.getThickness()); // from theme 1
        assertEquals("#0000ff", style.getColor()); // from theme 2
        assertFalse(style.getDashed()); // from workspace
        assertEquals(Routing.Direct, style.getRouting());
        assertEquals(Integer.valueOf(24), style.getFontSize());
        assertEquals(Integer.valueOf(200), style.getWidth());
        assertEquals(Integer.valueOf(50), style.getPosition());
        assertEquals(Integer.valueOf(100), style.getOpacity());
    }

    @Test
    @Tag("IntegrationTest")
    void loadThemes_ReplacesRelativeIconReferences() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Name");
        softwareSystem.addTags("Amazon Web Services - Alexa For Business");
        workspace.getViews().getConfiguration().setThemes("https://static.structurizr.com/themes/amazon-web-services-2020.04.30/theme.json");

        HttpClient httpClient = new HttpClient();
        httpClient.allow(".*");
        ThemeUtils.loadThemes(workspace, httpClient);

        // there should still be zero styles in the workspace
        assertEquals(0, workspace.getViews().getConfiguration().getStyles().getElements().size());

        // but we should be able to find a style included in the theme
        ElementStyle style = workspace.getViews().getConfiguration().getStyles().findElementStyle(softwareSystem);
        assertNotNull(style);
        assertEquals("#d6242d", style.getStroke());
        assertEquals("#d6242d", style.getColor());
        assertEquals("https://static.structurizr.com/themes/amazon-web-services-2020.04.30/alexa-for-business.png", style.getIcon());
    }

    @Test
    void inlineTheme() throws Exception {
        File themeFile = new File("src/test/resources/theme.json");

        try {
            Workspace theme = new Workspace("Theme", "");
            theme.getViews().getConfiguration().getStyles().addElementStyle("Tag").background("#ff0000").icon("logo.png");
            theme.getViews().getConfiguration().getStyles().addRelationshipStyle("Tag").color("#00ff00");
            ThemeUtils.toJson(theme, themeFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Workspace workspace = new Workspace("Name", "Description");
        ThemeUtils.inlineTheme(workspace, themeFile);

        assertEquals(0, workspace.getViews().getConfiguration().getThemes().length);
        assertEquals("#ff0000", workspace.getViews().getConfiguration().getStyles().getElementStyle("Tag").getBackground());
        assertEquals("data:image/png;base64,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", workspace.getViews().getConfiguration().getStyles().getElementStyle("Tag").getIcon());
        assertEquals("#00ff00", workspace.getViews().getConfiguration().getStyles().getRelationshipStyle("Tag").getColor());
    }

    @Test
    void inlineThemes_WhenTheElementStyleDoesNotExist() {
        ThemeUtils.registerThemes(new File("../structurizr-themes"));

        Workspace workspace = new Workspace("Name", "Description");
        workspace.getModel().addSoftwareSystem("Name").addTags("Amazon Web Services - Fargate");
        workspace.getViews().getConfiguration().addTheme("amazon-web-services-2025.07");

        ThemeUtils.inlineThemes(workspace);

        ElementStyle elementStyle = workspace.getViews().getConfiguration().getStyles().getElementStyle("Amazon Web Services - Fargate");
        assertNotNull(elementStyle);
        assertEquals("data:image/png;base64,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", elementStyle.getIcon());
    }

    @Test
    void inlineThemes_WhenTheElementStyleDoeExist() {
        ThemeUtils.registerThemes(new File("../structurizr-themes"));

        Workspace workspace = new Workspace("Name", "Description");
        workspace.getModel().addSoftwareSystem("Name").addTags("Amazon Web Services - Fargate");
        workspace.getViews().getConfiguration().getStyles().addElementStyle("Amazon Web Services - Fargate").shape(Shape.RoundedBox);
        workspace.getViews().getConfiguration().addTheme("amazon-web-services-2025.07");

        ThemeUtils.inlineThemes(workspace);

        ElementStyle elementStyle = workspace.getViews().getConfiguration().getStyles().getElementStyle("Amazon Web Services - Fargate");
        assertNotNull(elementStyle);
        assertEquals("data:image/png;base64,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", elementStyle.getIcon());
        assertEquals(Shape.RoundedBox, elementStyle.getShape());
    }

}