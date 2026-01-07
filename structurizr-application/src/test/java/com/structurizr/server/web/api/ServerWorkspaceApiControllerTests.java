package com.structurizr.server.web.api;

import com.structurizr.Workspace;
import com.structurizr.api.HashBasedMessageAuthenticationCode;
import com.structurizr.api.HmacContent;
import com.structurizr.api.HttpHeaders;
import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.configuration.Profile;
import com.structurizr.server.component.workspace.WorkspaceBranch;
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
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class ServerWorkspaceApiControllerTests {

    private ServerWorkspaceApiController controller;
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @BeforeEach
    void setUp() {
        controller = new ServerWorkspaceApiController();
    }

    @Test
    void getWorkspace_ReturnsAnApiError_WhenANegativeWorkspaceIdIsSpecified() {
        try {
            controller.getWorkspace(-1, null, request, response);
            fail();
        } catch (ApiException e) {
            assertEquals("Workspace ID must be greater than 1", e.getMessage());
        }
    }

    @Test
    void getWorkspace_ReturnsAnApiError_WhenNoAuthorizationHeaderIsSpecified() {
        try {
            controller.getWorkspace(1, null, request, response);
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

            controller.getWorkspace(1, null, request, response);
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

            controller.getWorkspace(1, null, request, response);
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

        String json = controller.getWorkspace(1, null, request, response);
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

    @Test
    void lockWorkspace_LocksTheWorkspace_WhenTheWorkspaceIsUnlocked() throws Exception {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setApiKey("key");
        workspaceMetaData.setApiSecret("secret");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public boolean lockWorkspace(long workspaceId, String username, String agent) {
                workspaceMetaData.setLockedUser(username);
                workspaceMetaData.setLockedAgent(agent);
                workspaceMetaData.setLockedDate(new Date());

                return true;
            }
        });

        HashBasedMessageAuthenticationCode code = new HashBasedMessageAuthenticationCode("secret");
        HmacContent hmacContent = new HmacContent("PUT", "/api/workspace/1/lock?user=user@example.com&agent=structurizr-java/1.2.3", new Md5Digest().generate(""), "", "1234567890");
        String generatedHmac = code.generate(hmacContent.toString());

        request.addHeader("Authorization", "key:" + Base64.getEncoder().encodeToString(generatedHmac.getBytes()));
        request.addHeader("Nonce", "1234567890");

        ApiResponse apiResponse = controller.lockWorkspace(1, "user@example.com", "structurizr-java/1.2.3", request, response);

        assertEquals("OK", apiResponse.getMessage());
        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user@example.com", workspaceMetaData.getLockedUser());
        assertEquals("structurizr-java/1.2.3", workspaceMetaData.getLockedAgent());
    }

    @Test
    void lockWorkspace_DoesNotLockTheWorkspace_WhenTheWorkspaceIsLocked() throws Exception {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setApiKey("key");
        workspaceMetaData.setApiSecret("secret");
        workspaceMetaData.setLockedUser("user1@example.com");
        workspaceMetaData.setLockedAgent("structurizr-web/123");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public boolean lockWorkspace(long workspaceId, String username, String agent) {
                return false;
            }
        });

        HashBasedMessageAuthenticationCode code = new HashBasedMessageAuthenticationCode("secret");
        HmacContent hmacContent = new HmacContent("PUT", "/api/workspace/1/lock?user=user2@example.com&agent=structurizr-java/1.2.3", new Md5Digest().generate(""), "", "1234567890");
        String generatedHmac = code.generate(hmacContent.toString());

        request.addHeader("Authorization", "key:" + Base64.getEncoder().encodeToString(generatedHmac.getBytes()));
        request.addHeader("Nonce", "1234567890");

        ApiResponse apiResponse = controller.lockWorkspace(1, "user2@example.com", "structurizr-java/1.2.3", request, response);

        assertEquals("The workspace is already locked by user1@example.com using structurizr-web/123.", apiResponse.getMessage());
    }

    @Test
    void unlockWorkspace_UnlocksTheWorkspace_WhenTheWorkspaceIsLocked() throws Exception {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setApiKey("key");
        workspaceMetaData.setApiSecret("secret");
        workspaceMetaData.setLockedUser("user1@example.com");
        workspaceMetaData.setLockedAgent("structurizr-web/123");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public boolean unlockWorkspace(long workspaceId) {
                workspaceMetaData.clearLock();
                return true;
            }
        });

        HashBasedMessageAuthenticationCode code = new HashBasedMessageAuthenticationCode("secret");
        HmacContent hmacContent = new HmacContent("DELETE", "/api/workspace/1/lock?user=user@example.com&agent=structurizr-java/1.2.3", new Md5Digest().generate(""), "", "1234567890");
        String generatedHmac = code.generate(hmacContent.toString());

        request.addHeader("Authorization", "key:" + Base64.getEncoder().encodeToString(generatedHmac.getBytes()));
        request.addHeader("Nonce", "1234567890");

        ApiResponse apiResponse = controller.unlockWorkspace(1, "user@example.com", "structurizr-java/1.2.3", request, response);

        assertEquals("OK", apiResponse.getMessage());
        assertFalse(workspaceMetaData.isLocked());
        assertNull(workspaceMetaData.getLockedUser());
        assertNull(workspaceMetaData.getLockedAgent());
    }

    @Test
    void getBranches_ReturnsAnApiError_WhenBranchesAreNotEnabled() {
        Configuration.init(Profile.Server, new Properties());
        Configuration.getInstance().setFeatureDisabled(Features.WORKSPACE_BRANCHES);

        try {
            controller.getBranches(1, request, response);
            fail();
        } catch (ApiException e) {
            assertEquals("Workspace branches are not enabled for this installation", e.getMessage());
        }
    }

    @Test
    void getBranches_ReturnsAnApiError_WhenANegativeWorkspaceIdIsSpecified() {
        Configuration.init(Profile.Server, new Properties());
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        try {
            controller.getBranches(-1, request, response);
            fail();
        } catch (ApiException e) {
            assertEquals("Workspace ID must be greater than 1", e.getMessage());
        }
    }

    @Test
    void getBranches_ReturnsAnApiError_WhenNoAuthorizationHeaderIsSpecified() {
        Configuration.init(Profile.Server, new Properties());
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        try {
            controller.getBranches(1, request, response);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Authorization header must be provided", e.getMessage());
        }
    }

    @Test
    void getBranches_ReturnsAnApiError_WhenTheAuthorizationHeaderIsIncorrectlySpecified() {
        Configuration.init(Profile.Server, new Properties());
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

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

            controller.getBranches(1, request, response);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Invalid authorization header", e.getMessage());
        }
    }

    @Test
    void getBranches_ReturnsAnApiError_WhenAnIncorrectApiKeyIsSpecifiedInTheAuthorizationHeader() {
        Configuration.init(Profile.Server, new Properties());
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

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

            controller.getBranches(1, request, response);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Incorrect API key", e.getMessage());
        }
    }

    @Test
    void getBranches_ReturnsTheBranches_WhenTheAuthorizationHeaderIsCorrectlySpecified() throws Exception {
        Configuration.init(Profile.Server, new Properties());
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                wmd.setApiKey("key");
                wmd.setApiSecret("secret");

                return wmd;
            }

            @Override
            public List<WorkspaceBranch> getWorkspaceBranches(long workspaceId) {
                return List.of(
                        new WorkspaceBranch("branch2"),
                        new WorkspaceBranch("branch1")
                );
            }
        });

        HashBasedMessageAuthenticationCode code = new HashBasedMessageAuthenticationCode("secret");
        HmacContent hmacContent = new HmacContent("GET", "/api/workspace/1/branch", new Md5Digest().generate(""), "", "1234567890");
        String generatedHmac = code.generate(hmacContent.toString());

        request.addHeader("Authorization", "key:" + Base64.getEncoder().encodeToString(generatedHmac.getBytes()));
        request.addHeader("Nonce", "1234567890");

        String json = controller.getBranches(1, request, response);
        assertEquals("""
                ["branch1","branch2"]""", json);
    }

    @Test
    void deleteBranch_ReturnsAnApiError_WhenBranchesAreNotEnabled() {
        Configuration.init(Profile.Server, new Properties());
        Configuration.getInstance().setFeatureDisabled(Features.WORKSPACE_BRANCHES);

        try {
            controller.deleteBranch(1, "branch", request, response);
            fail();
        } catch (ApiException e) {
            assertEquals("Workspace branches are not enabled for this installation", e.getMessage());
        }
    }

    @Test
    void deleteBranch_ReturnsAnApiError_WhenANegativeWorkspaceIdIsSpecified() {
        Configuration.init(Profile.Server, new Properties());
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        try {
            controller.deleteBranch(-1, "branch", request, response);
            fail();
        } catch (ApiException e) {
            assertEquals("Workspace ID must be greater than 1", e.getMessage());
        }
    }

    @Test
    void deleteBranch_ReturnsAnApiError_WhenNoAuthorizationHeaderIsSpecified() {
        Configuration.init(Profile.Server, new Properties());
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        try {
            controller.deleteBranch(1, "branch", request, response);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Authorization header must be provided", e.getMessage());
        }
    }

    @Test
    void deleteBranch_ReturnsAnApiError_WhenTheAuthorizationHeaderIsIncorrectlySpecified() {
        Configuration.init(Profile.Server, new Properties());
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

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

            controller.deleteBranch(1, "branch", request, response);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Invalid authorization header", e.getMessage());
        }
    }

    @Test
    void deleteBranch_ReturnsAnApiError_WhenAnIncorrectApiKeyIsSpecifiedInTheAuthorizationHeader() {
        Configuration.init(Profile.Server, new Properties());
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

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

            controller.deleteBranch(1, "branch", request, response);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Incorrect API key", e.getMessage());
        }
    }

    @Test
    void deleteBranch_ReturnsTrue_WhenTheAuthorizationHeaderIsCorrectlySpecifiedButTheMainBranchIsSpecified() throws Exception {
        Configuration.init(Profile.Server, new Properties());
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                wmd.setApiKey("key");
                wmd.setApiSecret("secret");

                return wmd;
            }

            @Override
            public List<WorkspaceBranch> getWorkspaceBranches(long workspaceId) {
                return List.of(
                        new WorkspaceBranch("branch2"),
                        new WorkspaceBranch("branch1")
                );
            }
        });

        // 1. "" as the branch
        HashBasedMessageAuthenticationCode code = new HashBasedMessageAuthenticationCode("secret");
        HmacContent hmacContent = new HmacContent("DELETE", "/api/workspace/1/branch/", new Md5Digest().generate(""), "", "1234567890");
        String generatedHmac = code.generate(hmacContent.toString());

        request.addHeader("Authorization", "key:" + Base64.getEncoder().encodeToString(generatedHmac.getBytes()));
        request.addHeader("Nonce", "1234567890");

        ApiResponse apiResponse = controller.deleteBranch(1, "", request, response);
        assertFalse(apiResponse.isSuccess());
        assertEquals("The main branch cannot be deleted", apiResponse.getMessage());

        // 2. "main" as the branch
        hmacContent = new HmacContent("DELETE", "/api/workspace/1/branch/main", new Md5Digest().generate(""), "", "1234567890");
        generatedHmac = code.generate(hmacContent.toString());
        request.addHeader("Authorization", "key:" + Base64.getEncoder().encodeToString(generatedHmac.getBytes()));

        apiResponse = controller.deleteBranch(1, "main", request, response);
        assertFalse(apiResponse.isSuccess());
        assertEquals("The main branch cannot be deleted", apiResponse.getMessage());
    }

    @Test
    void deleteBranch_ReturnsTrue_WhenTheAuthorizationHeaderIsCorrectlySpecifiedAndTheBranchDoesNotExist() throws Exception {
        Configuration.init(Profile.Server, new Properties());
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                wmd.setApiKey("key");
                wmd.setApiSecret("secret");

                return wmd;
            }

            @Override
            public List<WorkspaceBranch> getWorkspaceBranches(long workspaceId) {
                return List.of(
                        new WorkspaceBranch("branch2"),
                        new WorkspaceBranch("branch1")
                );
            }
        });

        // 1. "" as the branch
        HashBasedMessageAuthenticationCode code = new HashBasedMessageAuthenticationCode("secret");
        HmacContent hmacContent = new HmacContent("DELETE", "/api/workspace/1/branch/branch3", new Md5Digest().generate(""), "", "1234567890");
        String generatedHmac = code.generate(hmacContent.toString());

        request.addHeader("Authorization", "key:" + Base64.getEncoder().encodeToString(generatedHmac.getBytes()));
        request.addHeader("Nonce", "1234567890");

        ApiResponse apiResponse = controller.deleteBranch(1, "branch3", request, response);
        assertFalse(apiResponse.isSuccess());
        assertEquals("Workspace branch \"branch3\" does not exist", apiResponse.getMessage());
    }

    @Test
    void deleteBranch_ReturnsTrue_WhenTheAuthorizationHeaderIsCorrectlySpecified() throws Exception {
        Configuration.init(Profile.Server, new Properties());
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        StringBuilder buf = new StringBuilder();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                wmd.setApiKey("key");
                wmd.setApiSecret("secret");

                return wmd;
            }

            @Override
            public List<WorkspaceBranch> getWorkspaceBranches(long workspaceId) {
                return List.of(
                        new WorkspaceBranch("branch2"),
                        new WorkspaceBranch("branch1")
                );
            }

            @Override
            public boolean deleteBranch(long workspaceId, String branch) {
                buf.append("deleteBranch(" + workspaceId + ", " + branch + ")");
                return true;
            }
        });

        HashBasedMessageAuthenticationCode code = new HashBasedMessageAuthenticationCode("secret");
        HmacContent hmacContent = new HmacContent("DELETE", "/api/workspace/1/branch/branch1", new Md5Digest().generate(""), "", "1234567890");
        String generatedHmac = code.generate(hmacContent.toString());

        request.addHeader("Authorization", "key:" + Base64.getEncoder().encodeToString(generatedHmac.getBytes()));
        request.addHeader("Nonce", "1234567890");

        ApiResponse apiResponse = controller.deleteBranch(1, "branch1", request, response);
        assertTrue(apiResponse.isSuccess());
        assertEquals("deleteBranch(1, branch1)", buf.toString());
    }

}