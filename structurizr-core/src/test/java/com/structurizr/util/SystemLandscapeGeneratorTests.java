package com.structurizr.util;

import com.structurizr.Workspace;
import com.structurizr.model.Person;
import com.structurizr.model.SoftwareSystem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SystemLandscapeGeneratorTests {

    @Test
    void generate_ThrowsAnException_WhenNoWorkspacesAreProvided() {
        SystemLandscapeGenerator generator = new SystemLandscapeGenerator();

        try {
            generator.generate(null);
        } catch (Exception e) {
            assertEquals("Two or more workspaces must be provided", e.getMessage());
        }

        try {
            generator.generate(new ArrayList<>());
        } catch (Exception e) {
            assertEquals("Two or more workspaces must be provided", e.getMessage());
        }
    }

    @Test
    void generate_ThrowsAnException_WhenOneWorkspaceIsProvided() {
        SystemLandscapeGenerator generator = new SystemLandscapeGenerator();
        Workspace workspace = new Workspace("A");

        try {
            generator.generate(List.of(workspace));
        } catch (Exception e) {
            assertEquals("Two or more workspaces must be provided", e.getMessage());
        }
    }

    @Test
    void generate_BasicUsage() {
        SystemLandscapeGenerator generator = new SystemLandscapeGenerator();

        Workspace workspace1 = new Workspace("1");
        Person user = workspace1.getModel().addPerson("User");
        SoftwareSystem a = workspace1.getModel().addSoftwareSystem("A");
        SoftwareSystem b = workspace1.getModel().addSoftwareSystem("B");
        user.uses(a, "User-A");
        a.uses(b, "A-B");

        Workspace workspace2 = new Workspace("2");
        b = workspace2.getModel().addSoftwareSystem("B");
        SoftwareSystem c = workspace2.getModel().addSoftwareSystem("C");
        b.uses(c, "B-C");

        Workspace workspace = generator.generate(List.of(workspace1, workspace2));

        assertEquals(4, workspace.getModel().getElements().size());
        assertEquals(1, workspace.getModel().getPeople().size());
        assertEquals(3, workspace.getModel().getSoftwareSystems().size());

        user = workspace.getModel().getPersonWithName("User");
        a = workspace.getModel().getSoftwareSystemWithName("A");
        b = workspace.getModel().getSoftwareSystemWithName("B");
        c = workspace.getModel().getSoftwareSystemWithName("C");

        assertEquals(3, workspace.getModel().getRelationships().size());
        assertTrue(user.hasEfferentRelationshipWith(a, "User-A"));
        assertTrue(a.hasEfferentRelationshipWith(b, "A-B"));
        assertTrue(b.hasEfferentRelationshipWith(c, "B-C"));
    }

    @Test
    void generate_WhenRelationshipStrategyIsFirst() {
        SystemLandscapeGenerator generator = new SystemLandscapeGenerator();
        generator.setRelationshipStrategy(SystemLandscapeGenerator.RelationshipsStrategy.First);

        Workspace workspace1 = new Workspace("1");
        SoftwareSystem a = workspace1.getModel().addSoftwareSystem("A");
        SoftwareSystem b = workspace1.getModel().addSoftwareSystem("B");
        a.uses(b, "Description 1");

        Workspace workspace2 = new Workspace("2");
        a = workspace2.getModel().addSoftwareSystem("A");
        b = workspace2.getModel().addSoftwareSystem("B");
        a.uses(b, "Description 2");

        Workspace workspace = generator.generate(List.of(workspace1, workspace2));

        a = workspace.getModel().getSoftwareSystemWithName("A");
        b = workspace.getModel().getSoftwareSystemWithName("B");

        assertEquals(1, workspace.getModel().getRelationships().size());
        assertTrue(a.hasEfferentRelationshipWith(b, "Description 1"));
    }

    @Test
    void generate_WhenRelationshipStrategyIsAll() {
        SystemLandscapeGenerator generator = new SystemLandscapeGenerator();
        generator.setRelationshipStrategy(SystemLandscapeGenerator.RelationshipsStrategy.All);

        Workspace workspace1 = new Workspace("1");
        SoftwareSystem a = workspace1.getModel().addSoftwareSystem("A");
        SoftwareSystem b = workspace1.getModel().addSoftwareSystem("B");
        a.uses(b, "Description 1");

        Workspace workspace2 = new Workspace("2");
        a = workspace2.getModel().addSoftwareSystem("A");
        b = workspace2.getModel().addSoftwareSystem("B");
        a.uses(b, "Description 2");

        Workspace workspace = generator.generate(List.of(workspace1, workspace2));

        a = workspace.getModel().getSoftwareSystemWithName("A");
        b = workspace.getModel().getSoftwareSystemWithName("B");

        assertEquals(2, workspace.getModel().getRelationships().size());
        assertTrue(a.hasEfferentRelationshipWith(b, "Description 1"));
        assertTrue(a.hasEfferentRelationshipWith(b, "Description 2"));
    }

    @Test
    void generate_WorkspaceHyperlinks() {
        SystemLandscapeGenerator generator = new SystemLandscapeGenerator();

        Workspace workspace1 = new Workspace("1");
        workspace1.setId(1);
        SoftwareSystem a = workspace1.getModel().addSoftwareSystem("A");
        a.addContainer("Web Application");
        SoftwareSystem b = workspace1.getModel().addSoftwareSystem("B");
        a.uses(b, "Description");

        Workspace workspace2 = new Workspace("2");
        workspace2.setId(2);
        b = workspace2.getModel().addSoftwareSystem("B");
        b.addContainer("API");

        Workspace workspace = generator.generate(List.of(workspace1, workspace2));

        a = workspace.getModel().getSoftwareSystemWithName("A");
        b = workspace.getModel().getSoftwareSystemWithName("B");

        assertEquals("{workspace:1}/diagrams", a.getUrl());
        assertEquals("{workspace:2}/diagrams", b.getUrl());
    }


}