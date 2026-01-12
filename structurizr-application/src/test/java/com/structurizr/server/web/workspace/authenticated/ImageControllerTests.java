package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.InputStreamAndContentLength;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockHttpServletResponse;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class ImageControllerTests extends ControllerTestsBase {

    private ImageController controller;
    private MockHttpServletResponse response;
    private static final InputStreamAndContentLength IMAGE = new InputStreamAndContentLength(new ByteArrayInputStream(new byte[1234]), 1234);

    @BeforeEach
    public void setUp() {
        controller = new ImageController();
        response = new MockHttpServletResponse();
        clearUser();
    }

    @Test
    public void getAuthenticatedPngImage_ReturnsA404_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return null;
            }
        });

        Resource resource = controller.getAuthenticatedPngImage(1, "thumbnail", response);
        assertEquals(404, response.getStatus());
        assertNull(resource);
    }

    @Test
    void getAuthenticatedPngImage_ReturnsA404_WhenTheImageDoesNotExist() throws Exception {
        disableAuthentication();

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public InputStreamAndContentLength getImage(long workspaceId, String branch, String filename) {
                return null;
            }
        });

        Resource resource = controller.getAuthenticatedPngImage(1, "thumbnail", response);
        assertEquals(404, response.getStatus());
        assertNull(resource);
    }

    @Test
    void getAuthenticatedPngImage_ReturnsA404_WhenTheUserDoesNotHaveAccessToTheWorkspace() {
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
        Resource resource = controller.getAuthenticatedPngImage(1, "thumbnail", response);
        assertEquals(404, response.getStatus());
        assertNull(resource);
    }

    @Test
    void getAuthenticatedPngImage_ReturnsTheImage_WhenTheUserHasAccessToTheWorkspace() throws Exception {
        enableAuthentication();
        setUser("user@example.com");

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.addWriteUser("user@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public InputStreamAndContentLength getImage(long workspaceId, String branch, String diagramKey) throws WorkspaceComponentException {
                return IMAGE;
            }
        });

        Resource resource = controller.getAuthenticatedPngImage(1, "thumbnail", response);
        assertEquals(200, response.getStatus());
        assertNotNull(resource);
        assertEquals(1234, resource.contentLength());
    }

    @Test
    public void getAuthenticatedSvgImage_ReturnsA404_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return null;
            }
        });

        Resource resource = controller.getAuthenticatedSvgImage(1, "thumbnail", response);
        assertEquals(404, response.getStatus());
        assertNull(resource);
    }

    @Test
    void getAuthenticatedSvgImage_ReturnsA404_WhenTheImageDoesNotExist() throws Exception {
        disableAuthentication();

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public InputStreamAndContentLength getImage(long workspaceId, String branch, String filename) {
                return null;
            }
        });

        Resource resource = controller.getAuthenticatedSvgImage(1, "thumbnail", response);
        assertEquals(404, response.getStatus());
        assertNull(resource);
    }

    @Test
    void getAuthenticatedSvgImage_ReturnsA404_WhenTheUserDoesNotHaveAccessToTheWorkspace() {
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
        Resource resource = controller.getAuthenticatedSvgImage(1, "thumbnail", response);
        assertEquals(404, response.getStatus());
        assertNull(resource);
    }

    @Test
    void getAuthenticatedSvgImage_ReturnsTheImage_WhenTheUserHasAccessToTheWorkspace() throws Exception {
        enableAuthentication();
        setUser("user@example.com");

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.addWriteUser("user@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public InputStreamAndContentLength getImage(long workspaceId, String branch, String diagramKey) throws WorkspaceComponentException {
                return IMAGE;
            }
        });

        Resource resource = controller.getAuthenticatedSvgImage(1, "thumbnail", response);
        assertEquals(200, response.getStatus());
        assertNotNull(resource);
        assertEquals(1234, resource.contentLength());
    }

}