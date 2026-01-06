package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class DiagramEditorControllerTests extends ControllerTestsBase {

    private DiagramEditorController controller;
    private ModelMap model;

    @BeforeEach
    public void setUp() {
        controller = new DiagramEditorController();
        model = new ModelMap();
    }

    @Test
    void showAuthenticatedDiagramEditor_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        disableAuthentication();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return null;
            }
        });

        setUser("user@example.com");
        String view = controller.showAuthenticatedDiagramEditor(1, "main", "version", model);
        assertEquals("404", view);
    }

    @Test
    void showAuthenticatedDiagramEditor_ReturnsThe404Page_WhenTheUserDoesNotHaveAccess() {
        enableAuthentication();

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.addWriteUser("user2@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }
        });

        setUser("user1@example.com");
        String view = controller.showAuthenticatedDiagramEditor(1, "main", "version", model);
        assertEquals("404", view);
    }

    @Test
    void showAuthenticatedDiagramEditor_ReturnsThe404Page_WhenRunningInLocalModeWithEditingDisabled() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.EDITABLE_PROPERTY, "false");
        configureAsLocal(properties);

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }
        });

        String view = controller.showAuthenticatedDiagramEditor(1, "main", "version", model);
        assertEquals("404", view);
    }

    @Test
    void showAuthenticatedDiagramEditor_ReturnsTheDiagramEditorPage_WhenAuthenticationIsDisabled()  {
        disableAuthentication();

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
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

        setUser("1234567890"); // random user ID
        String view = controller.showAuthenticatedDiagramEditor(1, "main", "version", model);
        assertEquals("diagrams", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/images/", model.getAttribute("thumbnailUrl"));
        assertTrue(workspaceMetaData.isLocked());
        assertEquals("1234567890", workspaceMetaData.getLockedUser());
        assertTrue(workspaceMetaData.getLockedAgent().startsWith("structurizr/diagrams/"));
    }

    @Test
    void showAuthenticatedDiagramEditor_ReturnsTheDiagramEditorPage_WhenTheWorkspaceHasNoUsersConfigured()  {
        enableAuthentication();

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
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
        assertEquals("/workspace/1/images/", model.getAttribute("thumbnailUrl"));
        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user@example.com", workspaceMetaData.getLockedUser());
        assertTrue(workspaceMetaData.getLockedAgent().startsWith("structurizr/diagrams/"));
    }

    @Test
    void showAuthenticatedDiagramEditor_ReturnsTheDiagramEditorPage_WhenTheUserHasWriteAccess()  {
        enableAuthentication();

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.addWriteUser("user1@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
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
        assertEquals("/workspace/1/images/", model.getAttribute("thumbnailUrl"));
        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user1@example.com", workspaceMetaData.getLockedUser());
        assertTrue(workspaceMetaData.getLockedAgent().startsWith("structurizr/diagrams/"));
    }

    @Test
    void showAuthenticatedDiagramEditor_ReturnsAnErrorPage_WhenTheUserHasReadAccess()  {
        enableAuthentication();

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.addReadUser("user1@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
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