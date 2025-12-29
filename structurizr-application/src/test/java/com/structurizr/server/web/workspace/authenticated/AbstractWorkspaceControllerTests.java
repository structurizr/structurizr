package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.AbstractControllerTests;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractWorkspaceControllerTests extends AbstractControllerTests {

    private AbstractWorkspaceController controller;
    private ModelMap model;

    @BeforeEach
    public void setUp() {
        enableAuthentication();
        controller = new AbstractWorkspaceController() {};
        model = new ModelMap();
    }

    @Test
    void showAuthenticatedView_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        final WorkspaceMetaData workspaceMetaData = null;

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        setUser("user@example.com");
        String view = controller.showAuthenticatedView("view", workspaceMetaData, "main", "version", model, true, true);
        assertEquals("404", view);
    }

    @Test
    void showAuthenticatedView_ReturnsThe404Page_WhenAuthenticationIsEnabledAndTheUserDoesNotHaveAccess() {
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

        setUser("user2@example.com");
        String view = controller.showAuthenticatedView("view", workspaceMetaData, "main", "version", model, true, true);
        assertEquals("404", view);
    }

    @Test
    void showAuthenticatedView_ReturnsTheView_WhenAuthenticationIsEnabledAndTheUserHasWriteAccess() {
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
        String view = controller.showAuthenticatedView("view", workspaceMetaData, "main", "version", model, true, true);
        assertEquals("view", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/branch/main/images/", model.getAttribute("thumbnailUrl"));
    }

    @Test
    void showAuthenticatedView_ReturnsTheView_WhenAuthenticationIsEnabledAndTheUserHasReadAccess() {
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
        String view = controller.showAuthenticatedView("view", workspaceMetaData, "main", "version", model, true, true);
        assertEquals("view", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertFalse(workspaceMetaData.isEditable());
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/branch/main/images/", model.getAttribute("thumbnailUrl"));
    }

    @Test
    void showAuthenticatedView_ReturnsTheView_WhenAuthenticationIsDisabled() {
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

        String view = controller.showAuthenticatedView("view", workspaceMetaData, "main", "version", model, true, true);
        assertEquals("view", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/branch/main/images/", model.getAttribute("thumbnailUrl"));
    }

}