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

abstract class AbstractSearchAdapterTests extends AbstractTestsBase {

    protected abstract SearchAdapter getSearchAdapter();

    @Test
    public void index_AddsTheWorkspaceToTheSearchIndex() {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.setId(12345);
        getSearchAdapter().index(workspace);

        List<SearchResult> results = getSearchAdapter().search("name", null, Collections.singleton(12345L));
        assertEquals(1, results.size());

        SearchResult result = results.get(0);
        assertEquals(12345, result.getWorkspaceId());
        assertEquals("workspace", result.getType());
        assertEquals("", result.getUrl());
        assertEquals("Name", result.getName());
        assertEquals("Description", result.getDescription());
    }

    @Test
    public void index_AddsTheWorkspaceToTheSearchIndex_WhenFieldsAreNull() {
        Workspace workspace = new Workspace("Name", null); // null description
        workspace.setId(12345);
        getSearchAdapter().index(workspace);

        List<SearchResult> results = getSearchAdapter().search("name", null, Collections.singleton(12345L));
        assertEquals(1, results.size());

        SearchResult result = results.get(0);
        assertEquals(12345, result.getWorkspaceId());
        assertEquals("workspace", result.getType());
        assertEquals("", result.getUrl());
        assertEquals("Name", result.getName());
        assertEquals("", result.getDescription());
    }

    @Test
    public void index_ReplacesTheWorkspaceInTheSearchIndexWhenItAlreadyExists() {
        Workspace workspace = new Workspace("Name", "Old");
        workspace.setId(12345);
        getSearchAdapter().index(workspace);

        workspace.setDescription("New");
        getSearchAdapter().index(workspace);

        List<SearchResult> results = getSearchAdapter().search("new", null, Collections.singleton(12345L));
        assertEquals(1, results.size());

        SearchResult result = results.get(0);
        assertEquals(12345, result.getWorkspaceId());
        assertEquals("workspace", result.getType());
        assertEquals("", result.getUrl());
        assertEquals("Name", result.getName());
        assertEquals("New", result.getDescription());
    }

    @Test
    public void test_search_ThrowsAnException_WhenNoWorkspaceIdsAreProvided() {
        try {
            getSearchAdapter().search("query", "type", Collections.emptySet());
            fail();
        } catch (IllegalArgumentException iae) {
            assertEquals("One or more workspace IDs must be provided.", iae.getMessage());
        }
    }

    @Test
    public void search_FiltersResultsByWorkspaceId() {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.setId(1);
        getSearchAdapter().index(workspace);

        workspace = new Workspace("Name", "Description");
        workspace.setId(11);
        getSearchAdapter().index(workspace);

        List<SearchResult> results = getSearchAdapter().search("name", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getWorkspaceId());

        results = getSearchAdapter().search("name", null, Collections.singleton(11L));
        assertEquals(1, results.size());
        assertEquals(11, results.get(0).getWorkspaceId());
    }

    @Test
    public void search_FiltersResultsByType() {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.setId(1);
        getSearchAdapter().index(workspace);

        List<SearchResult> results = getSearchAdapter().search("name", null, Set.of(1L));
        assertEquals(1, results.size());

        results = getSearchAdapter().search("name", DocumentType.WORKSPACE, Set.of(1L));
        assertEquals(1, results.size());

        results = getSearchAdapter().search("name", DocumentType.DOCUMENTATION, Set.of(1L));
        assertEquals(0, results.size());
    }

    @Test
    public void search_WorkspaceDocumentation() {
        Workspace workspace = new Workspace("W", "Description");
        workspace.setId(1);
        workspace.getDocumentation().addSection(new Section(Format.Markdown, """
## Section 1

Foo"""));
        workspace.getDocumentation().addSection(new Section(Format.Markdown, """
## Section 2

Bar

### Section 2.1

Baz"""));

        getSearchAdapter().index(workspace);

        List<SearchResult> results = getSearchAdapter().search("foo", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("W - Section 1", results.get(0).getName());
        assertEquals("/documentation#1", results.get(0).getUrl());

        results = getSearchAdapter().search("bar", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("W - Section 2", results.get(0).getName());
        assertEquals("/documentation#2", results.get(0).getUrl());

        results = getSearchAdapter().search("baz", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("W - Section 2.1", results.get(0).getName());
        assertEquals("/documentation#2.1", results.get(0).getUrl());
    }

    @Test
    public void search_SoftwareSystemDocumentation() {
        String content =
                """
== Section 1

Foo

== Section 2

Bar

=== Section 2.1

Baz
                """;

        Workspace workspace = new Workspace("W", "Description");
        workspace.setId(1);
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("A");
        softwareSystem.getDocumentation().addSection(new Section(Format.AsciiDoc, content));

        getSearchAdapter().index(workspace);

        List<SearchResult> results = getSearchAdapter().search("foo", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("A - Section 1", results.get(0).getName());
        assertEquals("/documentation/A#1", results.get(0).getUrl());

        results = getSearchAdapter().search("bar", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("A - Section 2", results.get(0).getName());
        assertEquals("/documentation/A#2", results.get(0).getUrl());

        results = getSearchAdapter().search("baz", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("A - Section 2.1", results.get(0).getName());
        assertEquals("/documentation/A#2.1", results.get(0).getUrl());
    }

    @Test
    public void search_ContainerDocumentation() {
        String content =
                """
## Section 1

Foo

## Section 2

Bar

### Section 2.1

Baz
                """;

        Workspace workspace = new Workspace("W", "Description");
        workspace.setId(1);
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("A");
        Container container = softwareSystem.addContainer("B");
        container.getDocumentation().addSection(new Section(Format.Markdown, content));

        getSearchAdapter().index(workspace);

        List<SearchResult> results = getSearchAdapter().search("foo", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("B - Section 1", results.get(0).getName());
        assertEquals("/documentation/A/B#1", results.get(0).getUrl());

        results = getSearchAdapter().search("bar", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("B - Section 2", results.get(0).getName());
        assertEquals("/documentation/A/B#2", results.get(0).getUrl());

        results = getSearchAdapter().search("baz", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("B - Section 2.1", results.get(0).getName());
        assertEquals("/documentation/A/B#2.1", results.get(0).getUrl());
    }

    @Test
    public void search_ComponentDocumentation() {
        String content =
                """
## Section 1

Foo

## Section 2

Bar

### Section 2.1

Baz
                """;

        Workspace workspace = new Workspace("W", "Description");
        workspace.setId(1);
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("A");
        Container container = softwareSystem.addContainer("B");
        Component component = container.addComponent("C");
        component.getDocumentation().addSection(new Section(Format.Markdown, content));

        getSearchAdapter().index(workspace);

        List<SearchResult> results = getSearchAdapter().search("foo", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("C - Section 1", results.get(0).getName());
        assertEquals("/documentation/A/B/C#1", results.get(0).getUrl());

        results = getSearchAdapter().search("bar", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("C - Section 2", results.get(0).getName());
        assertEquals("/documentation/A/B/C#2", results.get(0).getUrl());

        results = getSearchAdapter().search("baz", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("C - Section 2.1", results.get(0).getName());
        assertEquals("/documentation/A/B/C#2.1", results.get(0).getUrl());
    }

    @Test
    public void search_SoftwareSystemDecisions() {
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

        getSearchAdapter().index(workspace);

        List<SearchResult> results = getSearchAdapter().search("foo", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("A - 1. Title", results.get(0).getName());
        assertEquals("/decisions/A#1", results.get(0).getUrl());
    }

    @Test
    public void search_ContainerDecisions() {
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

        getSearchAdapter().index(workspace);

        List<SearchResult> results = getSearchAdapter().search("foo", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("B - 1. Title", results.get(0).getName());
        assertEquals("/decisions/A/B#1", results.get(0).getUrl());
    }

    @Test
    public void search_ComponentDecisions() {
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

        getSearchAdapter().index(workspace);

        List<SearchResult> results = getSearchAdapter().search("foo", null, Set.of(1L));
        assertEquals(1, results.size());
        assertEquals("C - 1. Title", results.get(0).getName());
        assertEquals("/decisions/A/B/C#1", results.get(0).getUrl());
    }

}