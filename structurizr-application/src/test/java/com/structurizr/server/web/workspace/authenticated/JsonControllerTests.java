package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.AbstractTestsBase;
import com.structurizr.server.web.MockHttpServletResponse;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JsonControllerTests extends AbstractTestsBase {

    private JsonController controller;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        controller = new JsonController();
        response = new MockHttpServletResponse();
    }

    @Test
    void showAuthenticatedJson_ReturnsA404_WhenTheWorkspaceDoesNotExist() {
        configureAsServerWithAuthenticationDisabled();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return null;
            }
        });

        String json = controller.showAuthenticatedJson(1, "main", "version", response);
        assertNull(json);
        assertEquals(404, response.getStatus());
    }

    @Test
    void showAuthenticatedJson_ReturnsTheJson_WhenAuthenticationIsDisabled() {
        configureAsServerWithAuthenticationDisabled();

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

        String json = controller.showAuthenticatedJson(1, "main", "version", response);
        assertEquals("json", json);
    }

    @Test
    void showAuthenticatedJson_ReturnsTheJson_WhenAuthenticationIsEnabledAndTheUserHasAccess() {
        configureAsServerWithAuthenticationEnabled();
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
                return "json";
            }
        });

        String json = controller.showAuthenticatedJson(1, "main", "version", response);
        assertEquals("json", json);
    }

    @Test
    void showAuthenticatedJson_ReturnsA404_WhenAuthenticationIsEnabledAndTheUserDoesNotHaveAccess() {
        configureAsServerWithAuthenticationEnabled();
        setUser("user2@example.com");

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.addReadUser("user1@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }
        });

        String json = controller.showAuthenticatedJson(1, "main", "version", response);
        assertNull(json);
        assertEquals(404, response.getStatus());
    }

}