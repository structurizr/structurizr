package com.structurizr.view;

import com.structurizr.Workspace;
import com.structurizr.http.HttpClient;
import com.structurizr.model.Relationship;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.model.Tags;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

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
        assertEquals("""
                {
                  "name" : "Name",
                  "description" : "Description"
                }""", ThemeUtils.toJson(workspace));

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
    void inlineAllStylesFromTheme() throws Exception {
        File themeFile = new File("src/test/resources/theme.json");

        try {
            Workspace theme = new Workspace("Theme", "");
            theme.getViews().getConfiguration().getStyles().addElementStyle("Tag").background("#ff0000").icon("structurizr-logo.png");
            theme.getViews().getConfiguration().getStyles().addRelationshipStyle("Tag").color("#00ff00");
            ThemeUtils.toJson(theme, themeFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Workspace workspace = new Workspace("Name", "Description");
        ThemeUtils.inlineAllStylesFromTheme(workspace, themeFile);

        assertEquals(0, workspace.getViews().getConfiguration().getThemes().length);
        assertEquals("#ff0000", workspace.getViews().getConfiguration().getStyles().getElementStyle("Tag").getBackground());
        assertEquals("data:image/png;base64,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", workspace.getViews().getConfiguration().getStyles().getElementStyle("Tag").getIcon());
        assertEquals("#00ff00", workspace.getViews().getConfiguration().getStyles().getRelationshipStyle("Tag").getColor());
    }

    @Test
    void inlineUsedStylesFromRegisteredThemes_WhenTheElementStyleDoesNotExist() {
        ThemeUtils.installThemes(new File("../structurizr-themes"));

        Workspace workspace = new Workspace("Name", "Description");
        workspace.getModel().addSoftwareSystem("Name").addTags("Amazon Web Services - Fargate");
        workspace.getViews().getConfiguration().addTheme("amazon-web-services-2025.07");

        ThemeUtils.inlineStylesUsedFromInstalledThemes(workspace);

        assertEquals(1, workspace.getViews().getConfiguration().getStyles().getElements().size()); // only the one style has been inlined
        assertEquals(0, workspace.getViews().getConfiguration().getStyles().getRelationships().size()); // only the one style has been inlined

        ElementStyle elementStyle = workspace.getViews().getConfiguration().getStyles().getElementStyle("Amazon Web Services - Fargate");
        assertNotNull(elementStyle);
        assertEquals("data:image/png;base64,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", elementStyle.getIcon());
    }

    @Test
    void inlineStylesUsedFromInstalledThemes_WhenTheElementStyleDoesExist() {
        ThemeUtils.installThemes(new File("../structurizr-themes"));

        Workspace workspace = new Workspace("Name", "Description");
        workspace.getModel().addSoftwareSystem("Name").addTags("Amazon Web Services - Fargate");
        workspace.getViews().getConfiguration().getStyles().addElementStyle("Amazon Web Services - Fargate").shape(Shape.RoundedBox);
        workspace.getViews().getConfiguration().addTheme("amazon-web-services-2025.07");

        ThemeUtils.inlineStylesUsedFromInstalledThemes(workspace);

        assertEquals(1, workspace.getViews().getConfiguration().getStyles().getElements().size()); // only the one style has been inlined
        assertEquals(0, workspace.getViews().getConfiguration().getStyles().getRelationships().size()); // only the one style has been inlined

        ElementStyle elementStyle = workspace.getViews().getConfiguration().getStyles().getElementStyle("Amazon Web Services - Fargate");
        assertNotNull(elementStyle);
        assertEquals("data:image/png;base64,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", elementStyle.getIcon());
        assertEquals(Shape.RoundedBox, elementStyle.getShape());
    }

    @Test
    void inlineStylesUsedFromInstalledThemes() throws Exception {
        Workspace themeWorkspace = new Workspace("Theme");
        themeWorkspace.getViews().getConfiguration().getStyles().addElementStyle("Red").color("#ff0000");
        themeWorkspace.getViews().getConfiguration().getStyles().addElementStyle("Blue").color("#0000ff");
        themeWorkspace.getViews().getConfiguration().getStyles().addRelationshipStyle("Red").color("#ff0000");
        themeWorkspace.getViews().getConfiguration().getStyles().addRelationshipStyle("Blue").color("#0000ff");

        File themesDirectory = Files.createTempDirectory(this.getClass().getSimpleName()).toFile();
        themesDirectory.mkdirs();
        themesDirectory.deleteOnExit();

        String themeName = UUID.randomUUID().toString();

        File themeDirectory = new File(themesDirectory, themeName);
        themeDirectory.mkdir();
        ThemeUtils.toJson(themeWorkspace, new File(themeDirectory, "theme.json"));

        Workspace workspace = new Workspace("Name");
        SoftwareSystem a = workspace.getModel().addSoftwareSystem("A");
        a.addTags("Red");
        SoftwareSystem b = workspace.getModel().addSoftwareSystem("B");
        Relationship r = a.uses(b, "Uses");
        r.addTags("Red");

        ThemeUtils.installThemes(themesDirectory);
        workspace.getViews().getConfiguration().addTheme(themeName);

        ThemeUtils.inlineStylesUsedFromInstalledThemes(workspace);
        assertEquals(1, workspace.getViews().getConfiguration().getStyles().getElements().size());
        assertEquals("#ff0000", workspace.getViews().getConfiguration().getStyles().getElementStyle("Red").getColor());
        assertEquals(1, workspace.getViews().getConfiguration().getStyles().getRelationships().size());
        assertEquals("#ff0000", workspace.getViews().getConfiguration().getStyles().getRelationshipStyle("Red").getColor());
    }

}