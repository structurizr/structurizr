package com.structurizr.server.component.search;

import com.structurizr.Workspace;
import com.structurizr.documentation.Decision;
import com.structurizr.documentation.Format;
import com.structurizr.documentation.Section;
import com.structurizr.model.Component;
import com.structurizr.model.Container;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.server.web.AbstractTestsBase;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class AbstractSearchComponentTests extends AbstractTestsBase {

    protected abstract SearchComponent getSearchComponent();

    @Test
    public void index_AddsTheWorkspaceToTheSearchIndex() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.setId(12345);
        getSearchComponent().index(workspace);

        List<SearchResult> results = getSearchComponent().search("name", null, Collections.singleton(12345L));
        assertEquals(1, results.size());

        SearchResult result = results.get(0);
        assertEquals(12345, result.getWorkspaceId());
        assertEquals("workspace", result.getType());
        assertEquals("", result.getUrl());
        assertEquals("Name", result.getName());
        assertEquals("Description", result.getDescription());
    }

    @Test
    public void index_AddsTheWorkspaceToTheSearchIndex_WhenFieldsAreNull() throws Exception {
        Workspace workspace = new Workspace("Name", null); // null description
        workspace.setId(12345);
        getSearchComponent().index(workspace);

        List<SearchResult> results = getSearchComponent().search("name", null, Collections.singleton(12345L));
        assertEquals(1, results.size());

        SearchResult result = results.get(0);
        assertEquals(12345, result.getWorkspaceId());
        assertEquals("workspace", result.getType());
        assertEquals("", result.getUrl());
        assertEquals("Name", result.getName());
        assertEquals("", result.getDescription());
    }

    @Test
    public void index_ReplacesTheWorkspaceInTheSearchIndexWhenItAlreadyExists() throws Exception {
        Workspace workspace = new Workspace("Name", "Old");
        workspace.setId(12345);
        getSearchComponent().index(workspace);

        workspace.setDescription("New");
        getSearchComponent().index(workspace);

        List<SearchResult> results = getSearchComponent().search("new", null, Collections.singleton(12345L));
        assertEquals(1, results.size());

        SearchResult result = results.get(0);
        assertEquals(12345, result.getWorkspaceId());
        assertEquals("workspace", result.getType());
        assertEquals("", result.getUrl());
        assertEquals("Name", result.getName());
        assertEquals("New", result.getDescription());
    }

    @Test
    public void test_search_ThrowsAnException_WhenNoWorkspaceIdsAreProvided() throws Exception {
        try {
            getSearchComponent().search("query", "type", Collections.emptySet());
            fail();
        } catch (IllegalArgumentException iae) {
            assertEquals("One or more workspace IDs must be provided.", iae.getMessage());
        }
    }

    @Test
    public void search_FiltersResultsByWorkspaceId() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.setId(1);
        getSearchComponent().index(workspace);

        workspace = new Workspace("Name", "Description");
        workspace.setId(11);
        getSearchComponent().index(workspace);

        List<SearchResult> results = getSearchComponent().search("name", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getWorkspaceId());

        results = getSearchComponent().search("name", null, Collections.singleton(11L));
        assertEquals(1, results.size());
        assertEquals(11, results.get(0).getWorkspaceId());
    }

    @Test
    public void search_FiltersResultsByType() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.setId(1);
        getSearchComponent().index(workspace);

        List<SearchResult> results = getSearchComponent().search("name", null, Set.of(1L));
        assertEquals(1, results.size());

        results = getSearchComponent().search("name", DocumentType.WORKSPACE, Set.of(1L));
        assertEquals(1, results.size());

        results = getSearchComponent().search("name", DocumentType.DOCUMENTATION, Set.of(1L));
        assertEquals(0, results.size());
    }

    @Test
    public void search_WorkspaceDocumentation() throws Exception {
        String content =
                """
## Section 1

Foo

## Section 2

Bar
                """;

        Workspace workspace = new Workspace("W", "Description");
        workspace.setId(1);
        workspace.getDocumentation().addSection(new Section(Format.Markdown, content));

        getSearchComponent().index(workspace);

        List<SearchResult> results = getSearchComponent().search("foo", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("W - Section 1", results.get(0).getName());
        assertEquals("/documentation#1", results.get(0).getUrl());

        results = getSearchComponent().search("bar", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("W - Section 2", results.get(0).getName());
        assertEquals("/documentation#2", results.get(0).getUrl());
    }

    @Test
    public void search_SoftwareSystemDocumentation() throws Exception {
        String content =
                """
## Section 1

Foo

## Section 2

Bar
                """;

        Workspace workspace = new Workspace("W", "Description");
        workspace.setId(1);
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("A");
        softwareSystem.getDocumentation().addSection(new Section(Format.Markdown, content));

        getSearchComponent().index(workspace);

        List<SearchResult> results = getSearchComponent().search("foo", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("A - Section 1", results.get(0).getName());
        assertEquals("/documentation/A#1", results.get(0).getUrl());

        results = getSearchComponent().search("bar", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("A - Section 2", results.get(0).getName());
        assertEquals("/documentation/A#2", results.get(0).getUrl());
    }

    @Test
    public void search_ContainerDocumentation() throws Exception {
        String content =
                """
## Section 1

Foo

## Section 2

Bar
                """;

        Workspace workspace = new Workspace("W", "Description");
        workspace.setId(1);
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("A");
        Container container = softwareSystem.addContainer("B");
        container.getDocumentation().addSection(new Section(Format.Markdown, content));

        getSearchComponent().index(workspace);

        List<SearchResult> results = getSearchComponent().search("foo", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("B - Section 1", results.get(0).getName());
        assertEquals("/documentation/A/B#1", results.get(0).getUrl());

        results = getSearchComponent().search("bar", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("B - Section 2", results.get(0).getName());
        assertEquals("/documentation/A/B#2", results.get(0).getUrl());
    }

    @Test
    public void search_ComponentDocumentation() throws Exception {
        String content =
                """
## Section 1

Foo

## Section 2

Bar
                """;

        Workspace workspace = new Workspace("W", "Description");
        workspace.setId(1);
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("A");
        Container container = softwareSystem.addContainer("B");
        Component component = container.addComponent("C");
        component.getDocumentation().addSection(new Section(Format.Markdown, content));

        getSearchComponent().index(workspace);

        List<SearchResult> results = getSearchComponent().search("foo", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("C - Section 1", results.get(0).getName());
        assertEquals("/documentation/A/B/C#1", results.get(0).getUrl());

        results = getSearchComponent().search("bar", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("C - Section 2", results.get(0).getName());
        assertEquals("/documentation/A/B/C#2", results.get(0).getUrl());
    }

    @Test
    public void search_SoftwareSystemDecisions() throws Exception {
        String content =
                """
## Context

Foo
                """;

        Workspace workspace = new Workspace("W", "Description");
        workspace.setId(1);
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("A");
        Decision decision = new Decision("1");
        decision.setTitle("Title");
        decision.setStatus("Accepted");
        decision.setFormat(Format.Markdown);
        decision.setContent(content);
        softwareSystem.getDocumentation().addDecision(decision);

        getSearchComponent().index(workspace);

        List<SearchResult> results = getSearchComponent().search("foo", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("A - 1. Title", results.get(0).getName());
        assertEquals("/decisions/A#1", results.get(0).getUrl());
    }

    @Test
    public void search_ContainerDecisions() throws Exception {
        String content =
                """
## Context

Foo
                """;

        Workspace workspace = new Workspace("W", "Description");
        workspace.setId(1);
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("A");
        Container container = softwareSystem.addContainer("B");
        Decision decision = new Decision("1");
        decision.setTitle("Title");
        decision.setStatus("Accepted");
        decision.setFormat(Format.Markdown);
        decision.setContent(content);
        container.getDocumentation().addDecision(decision);

        getSearchComponent().index(workspace);

        List<SearchResult> results = getSearchComponent().search("foo", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("B - 1. Title", results.get(0).getName());
        assertEquals("/decisions/A/B#1", results.get(0).getUrl());
    }

    @Test
    public void search_ComponentDecisions() throws Exception {
        String content =
                """
## Context

Foo
                """;

        Workspace workspace = new Workspace("W", "Description");
        workspace.setId(1);
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("A");
        Container container = softwareSystem.addContainer("B");
        Component component = container.addComponent("C");
        Decision decision = new Decision("1");
        decision.setTitle("Title");
        decision.setStatus("Accepted");
        decision.setFormat(Format.Markdown);
        decision.setContent(content);
        component.getDocumentation().addDecision(decision);

        getSearchComponent().index(workspace);

        List<SearchResult> results = getSearchComponent().search("foo", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("C - 1. Title", results.get(0).getName());
        assertEquals("/decisions/A/B/C#1", results.get(0).getUrl());
    }

}