package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import static org.junit.jupiter.api.Assertions.*;

public class DiagramEditorControllerTests extends ControllerTestsBase {

    private DiagramEditorController controller;
    private ModelMap model;

    @BeforeEach
    public void setUp() {
        controller = new DiagramEditorController();
        model = new ModelMap();

        enableAuthentication();
    }

    @Test
    public void showAuthenticatedDiagramEditor_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        setUser("user@example.com");
        String view = controller.showAuthenticatedDiagramEditor(1, "main", "version", model);
        assertEquals("404", view);
    }

    @Test
    public void showAuthenticatedDiagramEditor_ReturnsThe404Page_WhenTheUserDoesNotHaveAccess() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user2@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        setUser("user1@example.com");
        String view = controller.showAuthenticatedDiagramEditor(1, "main", "version", model);
        assertEquals("404", view);
    }

    @Test
    public void showAuthenticatedDiagramEditor_ReturnsTheDiagramEditorPage_WhenTheWorkspaceHasNoUsersConfigured()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }

            @Override
            public boolean lockWorkspace(long workspaceId, String username, String agent) {
                workspaceMetaData.addLock(username, agent);
                return true;
            }
        });

        setUser("user@example.com");
        String view = controller.showAuthenticatedDiagramEditor(1, "main", "version", model);
        assertEquals("diagrams", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/branch/main/images/", model.getAttribute("thumbnailUrl"));
        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user@example.com", workspaceMetaData.getLockedUser());
        assertTrue(workspaceMetaData.getLockedAgent().startsWith("structurizr/diagrams/"));
    }

    @Test
    public void showAuthenticatedDiagramEditor_ReturnsTheDiagramEditorPage_WhenTheUserHasWriteAccess()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user1@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }

            @Override
            public boolean lockWorkspace(long workspaceId, String username, String agent) {
                workspaceMetaData.addLock(username, agent);
                return true;
            }
        });

        setUser("user1@example.com");
        String view = controller.showAuthenticatedDiagramEditor(1, "main", "version", model);
        assertEquals("diagrams", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/branch/main/images/", model.getAttribute("thumbnailUrl"));
        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user1@example.com", workspaceMetaData.getLockedUser());
        assertTrue(workspaceMetaData.getLockedAgent().startsWith("structurizr/diagrams/"));
    }

    @Test
    public void showAuthenticatedDiagramEditor_ReturnsAnErrorPage_WhenTheUserHasReadAccess()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addReadUser("user1@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        setUser("user1@example.com");
        String view = controller.showAuthenticatedDiagramEditor(1, "main", "version", model);
        assertEquals("workspace-is-readonly", view);
    }

}