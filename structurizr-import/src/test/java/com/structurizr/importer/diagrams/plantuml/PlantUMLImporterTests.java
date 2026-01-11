package com.structurizr.importer.diagrams.plantuml;

import com.structurizr.Workspace;
import com.structurizr.http.HttpClient;
import com.structurizr.view.ImageView;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class PlantUMLImporterTests {

    @Test
    public void importDiagram_WhenATitleIsDefined() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.getViews().getConfiguration().addProperty(PlantUMLImporter.PLANTUML_URL_PROPERTY, "https://plantuml.com/plantuml");
        ImageView view = workspace.getViews().createImageView("key");

        new PlantUMLImporter().importDiagram(view, new File("./src/test/resources/diagrams/plantuml/with-title.puml"));
        assertEquals("key", view.getKey());
        assertNull(view.getElement());
        assertNull(view.getElementId());
        assertEquals("Sequence diagram example", view.getTitle());
        assertEquals("https://plantuml.com/plantuml/svg/SoWkIImgAStDuIh9BCb9LGXEBInDpKjELKZ9J4mlIinLIAr8p2t8IULooazIqBLJSCp914fQAMIavkJaSpcavgK0zG80", view.getContent());
        assertEquals("image/svg+xml", view.getContentType());
    }

    @Test
    public void importDiagram_WhenATitleIsNotDefined() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.getViews().getConfiguration().addProperty(PlantUMLImporter.PLANTUML_URL_PROPERTY, "https://plantuml.com/plantuml");
        ImageView view = workspace.getViews().createImageView("key");

        new PlantUMLImporter().importDiagram(view, new File("./src/test/resources/diagrams/plantuml/without-title.puml"));
        assertEquals("key", view.getKey());
        assertNull(view.getElement());
        assertNull(view.getElementId());
        assertEquals("without-title.puml", view.getTitle());
        assertEquals("https://plantuml.com/plantuml/svg/SoWkIImgAStDuNBAJrBGjLDmpCbCJbMmKiX8pSd9vt98pKi1IW80", view.getContent());
        assertEquals("image/svg+xml", view.getContentType());
    }

    @Test
    public void importDiagram_AsPNG() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.getViews().getConfiguration().addProperty(PlantUMLImporter.PLANTUML_URL_PROPERTY, "https://plantuml.com/plantuml");
        workspace.getViews().getConfiguration().addProperty(PlantUMLImporter.PLANTUML_FORMAT_PROPERTY, "png");
        ImageView view = workspace.getViews().createImageView("key");

        new PlantUMLImporter().importDiagram(view, new File("./src/test/resources/diagrams/plantuml/with-title.puml"));
        assertEquals("https://plantuml.com/plantuml/png/SoWkIImgAStDuIh9BCb9LGXEBInDpKjELKZ9J4mlIinLIAr8p2t8IULooazIqBLJSCp914fQAMIavkJaSpcavgK0zG80", view.getContent());
        assertEquals("image/png", view.getContentType());
    }

    @Test
    @Tag("IntegrationTest")
    public void importDiagram_AsInlinePNG() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.getViews().getConfiguration().addProperty(PlantUMLImporter.PLANTUML_URL_PROPERTY, "https://plantuml.com/plantuml");
        workspace.getViews().getConfiguration().addProperty(PlantUMLImporter.PLANTUML_FORMAT_PROPERTY, "png");
        workspace.getViews().getConfiguration().addProperty(PlantUMLImporter.PLANTUML_INLINE_PROPERTY, "true");
        ImageView view = workspace.getViews().createImageView("key");

        HttpClient httpClient = new HttpClient();
        httpClient.allow(".*");

        new PlantUMLImporter(httpClient).importDiagram(view, new File("./src/test/resources/diagrams/plantuml/with-title.puml"));
        assertEquals("Sequence diagram example", view.getTitle());
        assertTrue(view.getContent().startsWith("data:image/png;base64,"));
        assertEquals("image/png", view.getContentType());
    }

    @Test
    public void importDiagram_AsSVG() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.getViews().getConfiguration().addProperty(PlantUMLImporter.PLANTUML_URL_PROPERTY, "https://plantuml.com/plantuml");
        workspace.getViews().getConfiguration().addProperty(PlantUMLImporter.PLANTUML_FORMAT_PROPERTY, "svg");
        ImageView view = workspace.getViews().createImageView("key");

        new PlantUMLImporter().importDiagram(view, new File("./src/test/resources/diagrams/plantuml/with-title.puml"));
        assertEquals("https://plantuml.com/plantuml/svg/SoWkIImgAStDuIh9BCb9LGXEBInDpKjELKZ9J4mlIinLIAr8p2t8IULooazIqBLJSCp914fQAMIavkJaSpcavgK0zG80", view.getContent());
        assertEquals("image/svg+xml", view.getContentType());
    }

    @Test
    @Tag("IntegrationTest")
    public void importDiagram_AsInlineSVG() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.getViews().getConfiguration().addProperty(PlantUMLImporter.PLANTUML_URL_PROPERTY, "https://plantuml.com/plantuml");
        workspace.getViews().getConfiguration().addProperty(PlantUMLImporter.PLANTUML_FORMAT_PROPERTY, "svg");
        workspace.getViews().getConfiguration().addProperty(PlantUMLImporter.PLANTUML_INLINE_PROPERTY, "true");
        ImageView view = workspace.getViews().createImageView("key");

        HttpClient httpClient = new HttpClient();
        httpClient.allow(".*");

        new PlantUMLImporter(httpClient).importDiagram(view, new File("./src/test/resources/diagrams/plantuml/with-title.puml"));
        assertTrue(view.getContent().startsWith("data:image/svg+xml;base64,"));
        assertEquals("image/svg+xml", view.getContentType());
    }

    @Test
    public void importDiagram_WhenThePlantUMLURLIsNotSpecified() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        ImageView view = workspace.getViews().createImageView("key");

        try {
        new PlantUMLImporter().importDiagram(view, new File("./src/test/resources/diagrams/plantuml/with-title.puml"));
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Please define a view/viewset property named plantuml.url to specify your PlantUML server", e.getMessage());
        }
    }

    @Test
    public void importDiagram_WhenAnInvalidFormatIsSpecified() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.getViews().getConfiguration().addProperty(PlantUMLImporter.PLANTUML_URL_PROPERTY, "https://plantuml.com/plantuml");
        workspace.getViews().getConfiguration().addProperty(PlantUMLImporter.PLANTUML_FORMAT_PROPERTY, "jpg");
        ImageView view = workspace.getViews().createImageView("key");

        try {
            new PlantUMLImporter().importDiagram(view, "...");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Expected a format of png or svg", e.getMessage());
        }
    }

}
