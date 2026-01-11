package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetadata;
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
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return null;
            }
        });

        setUser("user@example.com");
        String view = controller.showAuthenticatedView("view", 1, "main", "version", model, true, true);
        assertEquals("404", view);
    }

    @Test
    void showAuthenticatedView_ReturnsThe404Page_WhenAuthenticationIsEnabledAndTheUserIsAnonymous() {
        enableAuthentication();

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.addWriteUser("user1@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }
        });

        String view = controller.showAuthenticatedView("view", 1, "main", "version", model, true, true);
        assertEquals("404", view);
    }

    @Test
    void showAuthenticatedView_ReturnsThe404Page_WhenAuthenticationIsEnabledAndTheUserHasNoPermissions() {
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
        });

        setUser("user2@example.com");
        String view = controller.showAuthenticatedView("view", 1, "main", "version", model, true, true);
        assertEquals("404", view);
    }

    @Test
    void showAuthenticatedView_ReturnsTheView_WhenAuthenticationIsEnabledAndTheUserHasWritePermission() {
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
        });

        setUser("user1@example.com");
        String view = controller.showAuthenticatedView("view", 1, "", "version", model, true, true);
        assertEquals("view", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/images/", model.getAttribute("thumbnailUrl"));
    }

    @Test
    void showAuthenticatedView_ReturnsTheView_WhenAuthenticationIsEnabledAndTheUserHasReadPermission() {
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
        String view = controller.showAuthenticatedView("view", 1, "", "version", model, true, true);
        assertEquals("view", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertFalse(workspaceMetaData.isEditable());
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/images/", model.getAttribute("thumbnailUrl"));
    }

    @Test
    void showAuthenticatedView_ReturnsTheView_WhenAuthenticationIsDisabled() {
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
        });

        String view = controller.showAuthenticatedView("view", 1, "", "version", model, true, true);
        assertEquals("view", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/images/", model.getAttribute("thumbnailUrl"));
    }

}