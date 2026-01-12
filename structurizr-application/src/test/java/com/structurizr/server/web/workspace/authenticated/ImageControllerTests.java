package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.InputStreamAndContentLength;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockHttpServletResponse;
import com.structurizr.server.web.MockWorkspaceComponent;
import com.structurizr.server.web.api.ApiException;
import com.structurizr.server.web.api.ApiResponse;
import com.structurizr.server.web.api.NotFoundApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

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

        resource = controller.getAuthenticatedPngImage(1, "branch", "thumbnail", response);
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

        resource = controller.getAuthenticatedPngImage(1, "branch", "thumbnail", response);
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

        resource = controller.getAuthenticatedPngImage(1, "branch", "thumbnail", response);
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

        resource = controller.getAuthenticatedPngImage(1, "branch", "thumbnail", response);
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
        setUser("user1@example.com");

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.addWriteUser("user2@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }
        });

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

    @Test
    void putImage_ReturnsA404_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return null;
            }
        });

        try {
            controller.putImage(1, "image.png", "data:image/png;base64,");
            fail();
        } catch (NotFoundApiException e) {
            assertEquals("404", e.getMessage());
        }
    }

    @Test
    void putImage_ReturnsA404_WhenTheUserDoeNotHaveAccessToTheWorkspace() {
        enableAuthentication();
        setUser("user@example.com");

        final WorkspaceMetadata workspaceMetadata = new WorkspaceMetadata(1);
        workspaceMetadata.addWriteUser("write@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }
        });

        try {
            controller.putImage(1, "image.png", "data:image/png;base64,");
            fail();
        } catch (NotFoundApiException e) {
            assertEquals("404", e.getMessage());
        }
    }

    @Test
    void putImage_ReturnsA404_WhenPuttingAnImageThatIsNotAPngOrSvg() {
        enableAuthentication();
        setUser("user@example.com");

        final WorkspaceMetadata workspaceMetadata = new WorkspaceMetadata(1);
        workspaceMetadata.addWriteUser("user@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }

            @Override
            public boolean putImage(long workspaceId, String branch, String filename, File file) {
                return true;
            }
        });

        try {
            controller.putImage(1, "image.gif", "data:image/gif;base64,");
            fail();
        } catch (NotFoundApiException e) {
            assertEquals("404", e.getMessage());
        }
    }

    @Test
    void putImage_SavesThePngImage_WhenTheUserHasAccessToTheWorkspace() {
        enableAuthentication();
        setUser("user@example.com");

        WorkspaceMetadata workspaceMetadata = new WorkspaceMetadata(1);
        workspaceMetadata.addWriteUser("user@example.com");

        StringBuilder buf = new StringBuilder();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }

            @Override
            public boolean putImage(long workspaceId, String branch, String filename, File file) {
                try {
                    buf.append(workspaceId + "|" + branch + "|" + filename + "|" + Files.readString(file.toPath()));
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    return false;
                }
                return true;
            }
        });

        ApiResponse apiResponse = controller.putImage(1, "image.png", "data:image/png;base64,MTIzNDU2Nzg5MA");
        assertEquals("OK", apiResponse.getMessage());
        assertEquals("1||image.png|1234567890", buf.toString());
    }

    @Test
    void putImage_SavesThePngImageOnABranch_WhenTheUserHasAccessToTheWorkspace() {
        enableAuthentication();
        setUser("user@example.com");

        WorkspaceMetadata workspaceMetadata = new WorkspaceMetadata(1);
        workspaceMetadata.addWriteUser("user@example.com");

        StringBuilder buf = new StringBuilder();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }

            @Override
            public boolean putImage(long workspaceId, String branch, String filename, File file) {
                try {
                    buf.append(workspaceId + "|" + branch + "|" + filename + "|" + Files.readString(file.toPath()));
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    return false;
                }
                return true;
            }
        });

        ApiResponse apiResponse = controller.putImage(1, "branch", "image.png", "data:image/png;base64,MTIzNDU2Nzg5MA");
        assertEquals("OK", apiResponse.getMessage());
        assertEquals("1|branch|image.png|1234567890", buf.toString());
    }

    @Test
    void putImage_SavesTheSvgImage_WhenTheUserHasAccessToTheWorkspace() {
        enableAuthentication();
        setUser("user@example.com");

        WorkspaceMetadata workspaceMetadata = new WorkspaceMetadata(1);
        workspaceMetadata.addWriteUser("user@example.com");

        StringBuilder buf = new StringBuilder();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }

            @Override
            public boolean putImage(long workspaceId, String branch, String filename, File file) {
                try {
                    buf.append(workspaceId + "|" + branch + "|" + filename + "|" + Files.readString(file.toPath()));
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    return false;
                }
                return true;
            }
        });

        ApiResponse apiResponse = controller.putImage(1, "image.svg", "data:image/svg+xml;base64,PHN2Zz48L3N2Zz4=");
        assertEquals("OK", apiResponse.getMessage());
        assertEquals("1||image.svg|<svg></svg>", buf.toString());
    }

    @Test
    void putImage_SavesTheSvgImageOnABranch_WhenTheUserHasAccessToTheWorkspace() {
        enableAuthentication();
        setUser("user@example.com");

        WorkspaceMetadata workspaceMetadata = new WorkspaceMetadata(1);
        workspaceMetadata.addWriteUser("user@example.com");

        StringBuilder buf = new StringBuilder();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }

            @Override
            public boolean putImage(long workspaceId, String branch, String filename, File file) {
                try {
                    buf.append(workspaceId + "|" + branch + "|" + filename + "|" + Files.readString(file.toPath()));
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    return false;
                }
                return true;
            }
        });

        ApiResponse apiResponse = controller.putImage(1, "branch", "image.svg", "data:image/svg+xml;base64,PHN2Zz48L3N2Zz4=");
        assertEquals("OK", apiResponse.getMessage());
        assertEquals("1|branch|image.svg|<svg></svg>", buf.toString());
    }

}