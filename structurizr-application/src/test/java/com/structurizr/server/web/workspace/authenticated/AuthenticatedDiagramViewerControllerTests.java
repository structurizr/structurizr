package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AuthenticatedDiagramViewerControllerTests extends ControllerTestsBase {

    private DiagramViewerController controller;
    private ModelMap model;

    @BeforeEach
    public void setUp() {
        controller = new DiagramViewerController();
        model = new ModelMap();

        enableAuthentication();
    }

    @Test
    void showAuthenticatedDiagramViewer_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.showAuthenticatedDiagramViewer(1, "main", "version", "perspective", model);
        assertEquals("404", view);
    }

    @Test
    void showAuthenticatedDiagramViewer_ReturnsTheDiagramViewerPage_WhenAuthenticationIsDisabled()  {
        disableAuthentication();

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
        });

        setUser("user@example.com");
        String view = controller.showAuthenticatedDiagramViewer(1, "main", "version", "perspective", model);
        assertEquals("diagrams", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/branch/main/images/", model.getAttribute("thumbnailUrl"));
        assertEquals(true, model.getAttribute("includeEditButton"));
    }

    @Test
    void showAuthenticatedDiagramViewer_ReturnsTheDiagramViewerPage_WhenAuthenticationIsEnabledAndTheWorkspaceHasNoUsersConfigured()  {
        enableAuthentication();

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
        });

        setUser("user@example.com");
        String view = controller.showAuthenticatedDiagramViewer(1, "main", "version", "perspective", model);
        assertEquals("diagrams", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/branch/main/images/", model.getAttribute("thumbnailUrl"));
        assertEquals(true, model.getAttribute("includeEditButton"));
    }

    @Test
    void showAuthenticatedDiagramViewer_ReturnsTheDiagramViewerPage_WhenAuthenticationIsEnabledTheUserHasWriteAccess()  {
        enableAuthentication();

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
        });

        setUser("user1@example.com");
        String view = controller.showAuthenticatedDiagramViewer(1, "main", "version", "perspective", model);
        assertEquals("diagrams", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/branch/main/images/", model.getAttribute("thumbnailUrl"));
        assertEquals(true, model.getAttribute("includeEditButton"));
    }

    @Test
    public void showAuthenticatedDiagramViewer_ReturnsTheDiagramViewerPage_WhenAuthenticationIsEnabledAndTheUserHasReadAccess()  {
        enableAuthentication();

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
        String view = controller.showAuthenticatedDiagramViewer(1, "main", "version", "perspective", model);
        assertEquals("diagrams", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/branch/main/images/", model.getAttribute("thumbnailUrl"));
        assertEquals(false, model.getAttribute("includeEditButton"));
    }

    @Test
    void showAuthenticatedDiagramViewer_ReturnsTheDiagramViewerPage_WhenRunningInLocalMode() throws Exception {
        configureAsLocal();

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
            public long getLastModifiedDate() {
                return 1234567890;
            }
        });

        setUser("user@example.com");
        String view = controller.showAuthenticatedDiagramViewer(1, "main", "version", "perspective", model);
        assertEquals("diagrams", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/branch/main/images/", model.getAttribute("thumbnailUrl"));
        assertEquals(true, model.getAttribute("includeEditButton"));

        assertEquals(12345, model.getAttribute("autoRefreshInterval"));
        assertEquals(1234567890L, model.getAttribute("autoRefreshLastModifiedDate"));
    }

}