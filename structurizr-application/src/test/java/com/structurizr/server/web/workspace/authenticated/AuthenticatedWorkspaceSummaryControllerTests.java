package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.AbstractControllerTests;
import com.structurizr.server.web.MockWorkspaceComponent;
import com.structurizr.server.web.workspace.authenticated.WorkspaceSummaryController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticatedWorkspaceSummaryControllerTests extends AbstractControllerTests {

    private WorkspaceSummaryController controller;
    private ModelMap model;

    @BeforeEach
    public void setUp() {
        controller = new WorkspaceSummaryController();
        model = new ModelMap();
    }

    @Test
    public void showAuthenticatedWorkspaceSummary_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        setUser("user@example.com");
        String view = controller.showAuthenticatedWorkspaceSummary(1, "main", "version", model);
        assertEquals("404", view);
    }

    @Test
    public void showAuthenticatedWorkspaceSummary_ReturnsThe404Page_WhenTheUserDoesNotHaveAccess() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user2@example.com");
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
        String view = controller.showAuthenticatedWorkspaceSummary(1, "main", "version", model);
        assertEquals("404", view);
    }

    @Test
    public void showAuthenticatedWorkspaceSummary_ReturnsTheWorkspaceSummaryPage_WhenTheWorkspaceIsPublic()  {
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
        String view = controller.showAuthenticatedWorkspaceSummary(1, "main", "version", model);
        assertEquals("workspace-summary", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/images/", model.getAttribute("thumbnailUrl"));
    }

    @Test
    public void showAuthenticatedWorkspaceSummary_ReturnsTheWorkspaceSummaryPage_WhenTheUserHasWriteAccess()  {
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
        String view = controller.showAuthenticatedWorkspaceSummary(1, "main", "version", model);
        assertEquals("workspace-summary", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/images/", model.getAttribute("thumbnailUrl"));
    }

    @Test
    public void showAuthenticatedWorkspaceSummary_ReturnsTheWorkspaceSummaryPage_WhenTheUserHasReadAccess()  {
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
        String view = controller.showAuthenticatedWorkspaceSummary(1, "main", "version", model);
        assertEquals("workspace-summary", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/images/", model.getAttribute("thumbnailUrl"));
    }

}