package com.structurizr.server.web.api;

import com.structurizr.Workspace;
import com.structurizr.api.HttpHeaders;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.MockHttpServletRequest;
import com.structurizr.server.web.MockHttpServletResponse;
import com.structurizr.server.web.MockWorkspaceComponent;
import com.structurizr.util.Md5Digest;
import com.structurizr.util.WorkspaceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class LocalWorkspaceApiControllerTests {

    private LocalWorkspaceApiController controller;
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @BeforeEach
    void setUp() {
        controller = new LocalWorkspaceApiController();
    }

    @Test
    void getWorkspace_ReturnsAnApiError_WhenANegativeWorkspaceIdIsSpecified() {
        try {
            controller.getWorkspace(-1, request, response);
            fail();
        } catch (ApiException e) {
            assertEquals("Workspace ID must be greater than 1", e.getMessage());
        }
    }

    @Test
    void getWorkspace_ReturnsAnApiError_WhenNoAuthorizationHeaderIsSpecified() {
        try {
            controller.getWorkspace(1, request, response);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Authorization header must be provided", e.getMessage());
        }
    }

    @Test
    void getWorkspace_ReturnsAnApiError_WhenTheAuthorizationHeaderIsIncorrectlySpecified() {
        try {
            request.addHeader(HttpHeaders.AUTHORIZATION, "123");
            request.addHeader(HttpHeaders.NONCE, "1234567890");
            request.addHeader(HttpHeaders.CONTENT_MD5, "ZDQxZDhjZDk4ZjAwYjIwNGU5ODAwOTk4ZWNmODQyN2U=");

            controller.setWorkspaceComponent(new MockWorkspaceComponent() {
                @Override
                public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                    WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                    wmd.setApiKey("key");
                    wmd.setApiSecret("secret");

                    return wmd;
                }
            });

            controller.getWorkspace(1, request, response);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Invalid authorization header", e.getMessage());
        }
    }

    @Test
    void getWorkspace_ReturnsAnApiError_WhenAnIncorrectApiKeyIsSpecifiedInTheAuthorizationHeader() {
        try {
            request.addHeader(HttpHeaders.AUTHORIZATION, "otherkey:NWNkODEzYjVkZDE2ZGIzYmFlZDcxNjM5MjY3YjFhNGZiNDc5YjY1MzZiMzkwMjUyYzk3MGVhM2IyNmU4ZWI5OQ==");
            request.addHeader(HttpHeaders.NONCE, "1234567890");
            request.addHeader(HttpHeaders.CONTENT_MD5, "ZDQxZDhjZDk4ZjAwYjIwNGU5ODAwOTk4ZWNmODQyN2U=");

            controller.setWorkspaceComponent(new MockWorkspaceComponent() {
                @Override
                public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                    WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                    wmd.setApiKey("key");
                    wmd.setApiSecret("secret");

                    return wmd;
                }
            });

            controller.getWorkspace(1, request, response);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Incorrect API key", e.getMessage());
        }
    }
    
    @Test
    void getWorkspace_ReturnsTheWorkspace_WhenTheAuthorizationHeaderIsCorrectlySpecified() {
        request.addHeader(HttpHeaders.AUTHORIZATION, "key:YTM4ZGQ0OTk4Y2ZhMzRiYzdlMmQ0MzZlNzljZmZhZjEzMGJlN2U5NTU1NjFhODcxZDYxYmU4M2IwMDUyOGMzMg==");
        request.addHeader(HttpHeaders.NONCE, "1234567890");
        request.addHeader(HttpHeaders.CONTENT_MD5, "ZDQxZDhjZDk4ZjAwYjIwNGU5ODAwOTk4ZWNmODQyN2U=");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                wmd.setApiKey("key");
                wmd.setApiSecret("secret");

                return wmd;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String json = controller.getWorkspace(1, request, response);
        assertEquals("json", json);
    }

    @Test
    void putWorkspace_ReturnsAnApiError_WhenNoAuthorizationHeaderIsSpecified() {
        try {
            controller.putWorkspace(1, "json", request, response);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Authorization header must be provided", e.getMessage());
        }
    }

    @Test
    void putWorkspace_ReturnsAnApiError_WhenNoNonceHeaderIsSpecified() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                wmd.setApiKey("key");
                wmd.setApiSecret("secret");

                return wmd;
            }
        });

        try {
            request.setContent("json");
            request.addHeader(HttpHeaders.AUTHORIZATION, "key:NWNkODEzYjVkZDE2ZGIzYmFlZDcxNjM5MjY3YjFhNGZiNDc5YjY1MzZiMzkwMjUyYzk3MGVhM2IyNmU4ZWI5OQ==");
            controller.putWorkspace(1, "", request, response);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Request header missing: Nonce", e.getMessage());
        }
    }

    @Test
    void putWorkspace_ReturnsAnApiError_WhenNoContentMd5HeaderIsSpecified() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                wmd.setApiKey("key");
                wmd.setApiSecret("secret");

                return wmd;
            }
        });

        try {
            request.addHeader(HttpHeaders.AUTHORIZATION, "key:NWNkODEzYjVkZDE2ZGIzYmFlZDcxNjM5MjY3YjFhNGZiNDc5YjY1MzZiMzkwMjUyYzk3MGVhM2IyNmU4ZWI5OQ==");
            request.addHeader(HttpHeaders.NONCE, "1234567890");
            controller.putWorkspace(1, "json", request, response);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Request header missing: Content-MD5", e.getMessage());
        }
    }

    @Test
    void putWorkspace_ReturnsAnApiError_WhenTheContentMd5HeaderDoesNotMatchTheHashOfTheContent() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                wmd.setApiKey("key");
                wmd.setApiSecret("secret");

                return wmd;
            }
        });

        try {
            request.addHeader(HttpHeaders.AUTHORIZATION, "key:NWNkODEzYjVkZDE2ZGIzYmFlZDcxNjM5MjY3YjFhNGZiNDc5YjY1MzZiMzkwMjUyYzk3MGVhM2IyNmU4ZWI5OQ==");
            request.addHeader(HttpHeaders.NONCE, "1234567890");
            request.addHeader(HttpHeaders.CONTENT_MD5, "ZmM1ZTAzOGQzOGE1NzAzMjA4NTQ0MWU3ZmU3MDEwYjA=");
            controller.putWorkspace(1, "json", request, response);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("MD5 hash doesn't match content", e.getMessage());
        }
    }

    @Test
    void putWorkspace_PutsTheWorkspace_WhenTheAuthorizationHeaderIsCorrectlySpecified() throws Exception {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                wmd.setApiKey("key");
                wmd.setApiSecret("secret");

                return wmd;
            }
        });

        Workspace workspace = new Workspace("Name", "Description");
        String json = WorkspaceUtils.toJson(workspace, false);

        request.addHeader(HttpHeaders.AUTHORIZATION, "key:YjUxOTBkNjg5NjI5MjRiMzZjNWQwZmEwYjE3ZmI4OWFmNjY4NmY3MjEzZWRkNGE5ZjJmZTFjMDhjZmU0OGNlZg==");
        request.addHeader(HttpHeaders.NONCE, "1234567890");
        request.addHeader(HttpHeaders.CONTENT_MD5, Base64.getEncoder().encodeToString(new Md5Digest().generate(json).getBytes()));

        controller.putWorkspace(1, json, request, response);
    }

}