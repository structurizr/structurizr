package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.Workspace;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockHttpServletResponse;
import com.structurizr.server.web.MockWorkspaceComponent;
import com.structurizr.util.WorkspaceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ThemeControllerTests extends ControllerTestsBase {

    private ThemeController controller;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        controller = new ThemeController();
        response = new MockHttpServletResponse();
    }

    @Test
    void showAuthenticatedTheme_ReturnsA404_WhenTheWorkspaceDoesNotExist() throws Exception {
        disableAuthentication();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return null;
            }
        });

        String json = controller.showAuthenticatedTheme(1, "branch", "version", response);
        assertNull(json);
        assertEquals(404, response.getStatus());
    }

    @Test
    void showAuthenticatedTheme_ReturnsTheTheme_WhenAuthenticationIsDisabled() throws Exception {
        disableAuthentication();

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                try {
                    Workspace workspace = new Workspace("Name", "Description");
                    workspace.getViews().getConfiguration().getStyles().addElementStyle("Element").background("#ff0000");

                    return WorkspaceUtils.toJson(workspace, false);
                } catch (Exception e) {
                    throw new WorkspaceComponentException(e);
                }
            }
        });

        String json = controller.showAuthenticatedTheme(1, "main", "version", response);
        assertEquals("""
                {
                  "name" : "Name",
                  "description" : "Description",
                  "elements" : [ {
                    "tag" : "Element",
                    "background" : "#ff0000"
                  } ]
                }""", json);
    }

    @Test
    void showAuthenticatedTheme_ReturnsTheTheme_WhenAuthenticationIsEnabledAndTheUserHasAccess() throws Exception {
        enableAuthentication();
        setUser("user1@example.com");

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.addReadUser("user1@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                try {
                    Workspace workspace = new Workspace("Name", "Description");
                    workspace.getViews().getConfiguration().getStyles().addElementStyle("Element").background("#ff0000");

                    return WorkspaceUtils.toJson(workspace, false);
                } catch (Exception e) {
                    throw new WorkspaceComponentException(e);
                }
            }
        });

        String json = controller.showAuthenticatedTheme(1, "main", "version", response);
        assertEquals("""
                {
                  "name" : "Name",
                  "description" : "Description",
                  "elements" : [ {
                    "tag" : "Element",
                    "background" : "#ff0000"
                  } ]
                }""", json);
    }

    @Test
    void showAuthenticatedTheme_ReturnsA404_WhenAuthenticationIsEnabledAndTheUserDoesNotHaveAccess() throws Exception {
        enableAuthentication();
        setUser("user2@example.com");

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.addReadUser("user1@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }
        });

        String json = controller.showAuthenticatedTheme(1, "main", "version", response);
        assertNull(json);
        assertEquals(404, response.getStatus());
    }

}