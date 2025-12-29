package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.domain.InputStreamAndContentLength;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockHttpServletResponse;
import com.structurizr.server.web.MockWorkspaceComponent;
import com.structurizr.server.web.workspace.authenticated.ImageController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticatedImageControllerTests extends ControllerTestsBase {

    private ImageController controller;
    private MockHttpServletResponse response;
    private static final InputStreamAndContentLength IMAGE = new InputStreamAndContentLength(new ByteArrayInputStream(new byte[1234]), 1234);

    @BeforeEach
    public void setUp() {
        controller = new ImageController();
        response = new MockHttpServletResponse();

        enableAuthentication();
    }

    @Test
    public void getAuthenticatedImage_ReturnsA404_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        Resource resource = controller.getAuthenticatedImage(1, "thumbnail.png", response);
        assertEquals(404, response.getStatus());
        assertNull(resource);
    }

    @Test
    void getAuthenticatedImage_ReturnsA404_WhenTheUserDoesNotHaveAccessToTheWorkspace() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user2@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        setUser("user1@example.com");
        Resource resource = controller.getAuthenticatedImage(1, "thumbnail.png", response);
        assertEquals(404, response.getStatus());
        assertNull(resource);
    }

    @Test
    void getAuthenticatedImage_ReturnsTheImage_WhenTheUserHasReadAccessToTheWorkspace() throws Exception {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addReadUser("user@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public InputStreamAndContentLength getImage(long workspaceId, String branch, String diagramKey) {
                return IMAGE;
            }
        });

        setUser("user@example.com");
        Resource resource = controller.getAuthenticatedImage(1, "thumbnail.png", response);
        assertEquals(200, response.getStatus());
        assertNotNull(resource);
        assertEquals(1234, resource.contentLength());
    }

    @Test
    void getAuthenticatedImage_ReturnsTheImage_WhenTheUserHasWriteAccessToTheWorkspace() throws Exception {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public InputStreamAndContentLength getImage(long workspaceId, String branch, String diagramKey) {
                return IMAGE;
            }
        });

        setUser("user@example.com");
        Resource resource = controller.getAuthenticatedImage(1, "thumbnail.png", response);
        assertEquals(200, response.getStatus());
        assertNotNull(resource);
        assertEquals(1234, resource.contentLength());
    }

    @Test
    void getAuthenticatedImage_ReturnsA404_WhenTheImageDoesNotExist() throws Exception {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public InputStreamAndContentLength getImage(long workspaceId, String branch, String diagramKey) {
                return null;
            }
        });

        Resource resource = controller.getAuthenticatedImage(1, "thumbnail.png", response);
        assertEquals(404, response.getStatus());
        assertNull(resource);
    }

}