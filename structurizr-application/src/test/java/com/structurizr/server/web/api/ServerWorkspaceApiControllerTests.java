package com.structurizr.server.web.api;

import com.structurizr.Workspace;
import com.structurizr.api.HttpHeaders;
import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.configuration.Profile;
import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.component.workspace.WorkspaceVersion;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockHttpServletRequest;
import com.structurizr.server.web.MockHttpServletResponse;
import com.structurizr.server.web.MockWorkspaceComponent;
import com.structurizr.util.DateUtils;
import com.structurizr.util.WorkspaceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class ServerWorkspaceApiControllerTests extends ControllerTestsBase {

    private ServerWorkspaceApiController controller;
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @BeforeEach
    void setUp() {
        controller = new ServerWorkspaceApiController();
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {});

        enableAuthentication();
    }

    @Test
    void getWorkspace_ReturnsAnApiError_WhenNoAuthorizationHeaderIsSpecified() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                wmd.setApiKey("1234567890");

                return wmd;
            }
        });

        try {
            controller.getWorkspace(1, WorkspaceVersion.LATEST_VERSION, request, response);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Authorization header must be provided", e.getMessage());
        }
    }

    @Test
    void getWorkspace_ReturnsAnApiError_WhenTheAuthorizationHeaderIsIncorrect() {
        try {
            request.addHeader(HttpHeaders.AUTHORIZATION, "key");

            controller.setWorkspaceComponent(new MockWorkspaceComponent() {
                @Override
                public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                    WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                    wmd.setApiKey("1234567890");

                    return wmd;
                }
            });

            controller.getWorkspace(1, WorkspaceVersion.LATEST_VERSION, request, response);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Incorrect API key", e.getMessage());
        }
    }

    @Test
    void getWorkspace_ReturnsTheWorkspace_WhenTheAuthorizationHeaderIsCorrectAndTheWorkspaceApiKeyIsStoredAsPlaintext() {
        request.addHeader(HttpHeaders.AUTHORIZATION, "1234567890");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                wmd.setApiKey("1234567890");

                return wmd;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String json = controller.getWorkspace(1, WorkspaceVersion.LATEST_VERSION, request, response);
        assertEquals("json", json);
    }

    @Test
    void getWorkspace_ReturnsTheWorkspace_WhenTheAuthorizationHeaderIsCorrectAndTheWorkspaceApiKeyIsStoredAsBcrypt() {
        request.addHeader(HttpHeaders.AUTHORIZATION, "1234567890");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                wmd.setApiKey(new BCryptPasswordEncoder().encode("1234567890"));

                return wmd;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String json = controller.getWorkspace(1, WorkspaceVersion.LATEST_VERSION, request, response);
        assertEquals("json", json);
    }

    @Test
    void getWorkspace_ReturnsTheWorkspace_WhenTheAuthorizationHeaderIsCorrectAndTheAdminApiKeyIsUsed() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.API_KEY, new BCryptPasswordEncoder().encode("admin-1234567890"));
        enableAuthentication(properties);

        request.addHeader(HttpHeaders.AUTHORIZATION, "admin-1234567890");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                wmd.setApiKey("1234567890");

                return wmd;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String json = controller.getWorkspace(1, WorkspaceVersion.LATEST_VERSION, request, response);
        assertEquals("json", json);
    }

    @Test
    void putWorkspace_ReturnsAnApiError_WhenANegativeWorkspaceIdIsSpecified() {
        try {
            controller.putWorkspace(-1, "json", request, response);
            fail();
        } catch (ApiException e) {
            assertEquals("Workspace ID must be greater than 1", e.getMessage());
        }
    }

    @Test
    void putWorkspace_ReturnsAnApiError_WhenNoAuthorizationHeaderIsSpecified() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                wmd.setApiKey("key");

                return wmd;
            }
        });

        try {
            controller.putWorkspace(1, "json", request, response);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Authorization header must be provided", e.getMessage());
        }
    }

    @Test
    void putWorkspace_ReturnsAnApiError_WhenTheAuthorizationHeaderIsIncorrect() {
        try {
            request.addHeader(HttpHeaders.AUTHORIZATION, "key");

            controller.setWorkspaceComponent(new MockWorkspaceComponent() {
                @Override
                public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                    WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                    wmd.setApiKey("1234567890");

                    return wmd;
                }
            });

            controller.putWorkspace(1, "json", request, response);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Incorrect API key", e.getMessage());
        }
    }

    @Test
    void putWorkspace_PutsTheWorkspace_WhenTheAuthorizationHeaderIsCorrectAndTheApiKeyIsStoredAsPlaintext() throws Exception {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                wmd.setApiKey("1234567890");

                return wmd;
            }
        });

        Workspace workspace = new Workspace("Name", "Description");
        String json = WorkspaceUtils.toJson(workspace, false);

        request.addHeader(HttpHeaders.AUTHORIZATION, "1234567890");

        controller.putWorkspace(1, json, request, response);
    }

    @Test
    void putWorkspace_PutsTheWorkspace_WhenTheAuthorizationHeaderIsCorrectAndTheApiKeyIsStoredAsBcrypt() throws Exception {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                wmd.setApiKey(new BCryptPasswordEncoder().encode("1234567890"));

                return wmd;
            }
        });

        Workspace workspace = new Workspace("Name", "Description");
        String json = WorkspaceUtils.toJson(workspace, false);

        request.addHeader(HttpHeaders.AUTHORIZATION, "1234567890");

        controller.putWorkspace(1, json, request, response);
    }

    @Test
    void lockWorkspace_LocksTheWorkspace_WhenAnAuthorizationHeaderIsProvidedAndTheWorkspaceIsUnlocked() throws Exception {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setApiKey("1234567890");

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

        request.addHeader("Authorization", "1234567890");

        ApiResponse apiResponse = controller.lockWorkspace(1, "user@example.com", "structurizr-java/1.2.3", request, response);

        assertEquals("OK", apiResponse.getMessage());
        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user@example.com", workspaceMetaData.getLockedUser());
        assertEquals("structurizr-java/1.2.3", workspaceMetaData.getLockedAgent());
    }

    @Test
    void lockWorkspace_DoesNotLockTheWorkspace_WhenAnAuthorizationHeaderIsProvidedAndTheWorkspaceIsLocked() throws Exception {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setApiKey("1234567890");
        workspaceMetaData.setLockedUser("user1@example.com");
        workspaceMetaData.setLockedAgent("structurizr-web/123");

        Date date = new Date();
        workspaceMetaData.setLockedDate(date);

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

        request.addHeader("Authorization", "1234567890");

        ApiResponse apiResponse = controller.lockWorkspace(1, "user2@example.com", "structurizr-java/1.2.3", request, response);

        assertEquals(String.format("The workspace could not be locked; it was locked by user1@example.com using structurizr-web/123 at %s", new SimpleDateFormat(DateUtils.USER_FRIENDLY_DATE_FORMAT).format(workspaceMetaData.getLockedDate())), apiResponse.getMessage());
    }

    @Test
    void lockWorkspace_ReturnsAFailureResponse_WhenAnAuthorizationHeaderIsNotProvidedAndTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return null;
            }
        });

        setUser("user@example.com");

        try {
            controller.lockWorkspace(1, null, "agent", request, response);
            fail();
        } catch (ApiException e) {
            assertEquals("404", e.getMessage());
        }
    }

    @Test
    void lockWorkspace_LocksTheWorkspace_WhenAnAuthorizationHeaderIsNotProvidedAndTheWorkspaceIsNotLocked() {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public boolean lockWorkspace(long workspaceId, String username, String agent) {
                workspaceMetaData.addLock(username, agent);
                return true;
            }
        });

        setUser("user@example.com");
        ApiResponse apiResponse = controller.lockWorkspace(1, null, "agent", request, response);
        assertTrue(apiResponse.isSuccess());
        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user@example.com", workspaceMetaData.getLockedUser());
        assertEquals("agent", workspaceMetaData.getLockedAgent());
    }

    @Test
    void lockWorkspace_ReturnsAFailureResponse_WhenAnAuthorizationHeaderIsNotProvidedAndTheWorkspaceIsAlreadyLockedByAnotherAgent() {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.addLock("user1@example.com", "agent1");

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

        setUser("user1@example.com");
        ApiResponse apiResponse = controller.lockWorkspace(1, null, "agent2", request, response);
        assertFalse(apiResponse.isSuccess());
        assertEquals(String.format("The workspace could not be locked; it was locked by user1@example.com using agent1 at %s", new SimpleDateFormat(DateUtils.USER_FRIENDLY_DATE_FORMAT).format(workspaceMetaData.getLockedDate())), apiResponse.getMessage());
        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user1@example.com", workspaceMetaData.getLockedUser());
        assertEquals("agent1", workspaceMetaData.getLockedAgent());
    }

    @Test
    void lockWorkspace_ReturnsAFailureResponse_WhenAnAuthorizationHeaderIsNotProvidedAndTheWorkspaceIsAlreadyLockedBySomebodyElse() {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.addLock("user1@example.com", "agent");

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

        setUser("user2@example.com");
        ApiResponse apiResponse = controller.lockWorkspace(1, null, "agent", request, response);
        assertFalse(apiResponse.isSuccess());
        assertEquals(String.format("The workspace could not be locked; it was locked by user1@example.com using agent at %s", new SimpleDateFormat(DateUtils.USER_FRIENDLY_DATE_FORMAT).format(workspaceMetaData.getLockedDate())), apiResponse.getMessage());
        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user1@example.com", workspaceMetaData.getLockedUser());
        assertEquals("agent", workspaceMetaData.getLockedAgent());
    }

    @Test
    void unlockWorkspace_UnlocksTheWorkspace_WhenAnAuthorizationHeaderIsProvidedAndTheWorkspaceIsLocked() throws Exception {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setApiKey("1234567890");
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

        request.addHeader(HttpHeaders.X_AUTHORIZATION, "1234567890");

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

            controller.setWorkspaceComponent(new MockWorkspaceComponent() {
                @Override
                public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                    WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                    wmd.setApiKey("key");

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
    void getBranches_ReturnsAnApiError_WhenAnIncorrectApiKeyIsSpecifiedInTheAuthorizationHeader() {
        Configuration.init(Profile.Server, new Properties());
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        try {
            request.addHeader(HttpHeaders.AUTHORIZATION, "otherkey:NWNkODEzYjVkZDE2ZGIzYmFlZDcxNjM5MjY3YjFhNGZiNDc5YjY1MzZiMzkwMjUyYzk3MGVhM2IyNmU4ZWI5OQ==");

            controller.setWorkspaceComponent(new MockWorkspaceComponent() {
                @Override
                public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                    WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                    wmd.setApiKey("key");

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
                wmd.setApiKey("1234567890");

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

        request.addHeader("Authorization", "1234567890");

        String json = controller.getBranches(1, request, response);
        assertEquals("""
                ["branch1","branch2"]""", json);
    }

    @Test
    void deleteBranch_ReturnsAnApiError_WhenBranchesAreNotEnabled() {
        Configuration.init(Profile.Server, new Properties());
        Configuration.getInstance().setFeatureDisabled(Features.WORKSPACE_BRANCHES);

        try {
            controller.deleteBranch(1, "branch", request);
            fail();
        } catch (ApiException e) {
            assertEquals("Workspace branches are not enabled for this installation", e.getMessage());
        }
    }

    @Test
    void deleteBranch_ReturnsAnApiError_WhenNoAuthorizationHeaderIsSpecified() {
        Configuration.init(Profile.Server, new Properties());
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        try {
            controller.deleteBranch(1, "branch", request);
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

            controller.setWorkspaceComponent(new MockWorkspaceComponent() {
                @Override
                public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                    WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                    wmd.setApiKey("key");

                    return wmd;
                }
            });

            controller.deleteBranch(1, "branch", request);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Incorrect API key", e.getMessage());
        }
    }

    @Test
    void deleteBranch_ReturnsAnApiError_WhenAnIncorrectApiKeyIsSpecifiedInTheAuthorizationHeader() {
        Configuration.init(Profile.Server, new Properties());
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        try {
            request.addHeader(HttpHeaders.AUTHORIZATION, "otherkey:NWNkODEzYjVkZDE2ZGIzYmFlZDcxNjM5MjY3YjFhNGZiNDc5YjY1MzZiMzkwMjUyYzk3MGVhM2IyNmU4ZWI5OQ==");

            controller.setWorkspaceComponent(new MockWorkspaceComponent() {
                @Override
                public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                    WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                    wmd.setApiKey("key");

                    return wmd;
                }
            });

            controller.deleteBranch(1, "branch", request);
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Incorrect API key", e.getMessage());
        }
    }

    @Test
    void deleteBranch_ThrowsAnException_WhenTheAuthorizationHeaderIsCorrectlySpecifiedButTheMainBranchIsSpecified() throws Exception {
        Configuration.init(Profile.Server, new Properties());
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                wmd.setApiKey("1234567890");

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

        request.addHeader(HttpHeaders.X_AUTHORIZATION, "1234567890");

        // 1. "" as the branch
        try {
            controller.deleteBranch(1, "", request);
            fail();
        } catch (Exception e) {
            assertEquals("The main branch cannot be deleted", e.getMessage());
        }

        // 2. "main" as the branch
        try {
            controller.deleteBranch(1, "main", request);
            fail();
        } catch (Exception e) {
            assertEquals("The main branch cannot be deleted", e.getMessage());
        }
    }

    @Test
    void deleteBranch_ReturnsTrue_WhenTheAuthorizationHeaderIsCorrectlySpecifiedAndTheBranchDoesNotExist() throws Exception {
        Configuration.init(Profile.Server, new Properties());
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                wmd.setApiKey("1234567890");

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

        request.addHeader("Authorization", "1234567890");

        // 1. "" as the branch
        ApiResponse apiResponse = controller.deleteBranch(1, "branch3", request);
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
                wmd.setApiKey("1234567890");

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

        request.addHeader("Authorization", "1234567890");

        ApiResponse apiResponse = controller.deleteBranch(1, "branch1", request);
        assertTrue(apiResponse.isSuccess());
        assertEquals("deleteBranch(1, branch1)", buf.toString());
    }

}