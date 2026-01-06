package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.Workspace;
import com.structurizr.dsl.DslUtils;
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

public class DslControllerTests extends ControllerTestsBase {

    private DslController controller;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        controller = new DslController();
        response = new MockHttpServletResponse();
    }

    @Test
    void showAuthenticatedDsl_ReturnsA404_WhenTheWorkspaceDoesNotExist() throws Exception {
        disableAuthentication();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return null;
            }
        });

        String dsl = controller.showAuthenticatedDsl(1, "branch", "version", response);
        assertNull(dsl);
        assertEquals(404, response.getStatus());
    }

    @Test
    void showAuthenticatedDsl_ReturnsTheDsl_WhenAuthenticationIsDisabled() throws Exception {
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
                    DslUtils.setDsl(workspace, "workspace { ... }");

                    return WorkspaceUtils.toJson(workspace, false);
                } catch (Exception e) {
                    throw new WorkspaceComponentException(e);
                }
            }
        });

        String dsl = controller.showAuthenticatedDsl(1, "main", "version", response);
        assertEquals("workspace { ... }", dsl);
    }

    @Test
    void showAuthenticatedDsl_ReturnsTheDsl_WhenAuthenticationIsEnabledAndTheUserHasAccess() throws Exception {
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
                    DslUtils.setDsl(workspace, "workspace { ... }");

                    return WorkspaceUtils.toJson(workspace, false);
                } catch (Exception e) {
                    throw new WorkspaceComponentException(e);
                }
            }
        });

        String dsl = controller.showAuthenticatedDsl(1, "main", "version", response);
        assertEquals("workspace { ... }", dsl);
    }

    @Test
    void showAuthenticatedDsl_ReturnsA404_WhenAuthenticationIsEnabledAndTheUserDoesNotHaveAccess() throws Exception {
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

        String dsl = controller.showAuthenticatedDsl(1, "main", "version", response);
        assertNull(dsl);
        assertEquals(404, response.getStatus());
    }

}