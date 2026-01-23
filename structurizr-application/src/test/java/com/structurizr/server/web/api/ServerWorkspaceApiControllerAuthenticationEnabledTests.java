package com.structurizr.server.web.api;

import com.structurizr.Workspace;
import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.component.workspace.WorkspaceVersion;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.ControllerTestsBase;
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

public class ServerWorkspaceApiControllerAuthenticationEnabledTests extends ControllerTestsBase {

    private ServerWorkspaceApiController controller;
    private WorkspaceMetadata workspaceMetadata;

    @BeforeEach
    void setUp() {
        controller = new ServerWorkspaceApiController();
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {});

        workspaceMetadata = new WorkspaceMetadata(1);
        workspaceMetadata.addReadUser("read@example.com");
        workspaceMetadata.addWriteUser("write@example.com");
        workspaceMetadata.setApiKey(new BCryptPasswordEncoder().encode("1234567890"));

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }
        });

        enableAuthentication();
        clearUser();
    }

    @Test
    void getWorkspace_ReturnsAnError_WhenNoApiKeyIsSpecified() {
        try {
            controller.getWorkspace(1, WorkspaceVersion.LATEST_VERSION, "");
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("API key must be provided", e.getMessage());
        }
    }

    @Test
    void getWorkspace_ReturnsAnError_WhenTheApiKeyIsIncorrect() {
        try {
            controller.getWorkspace(1, WorkspaceVersion.LATEST_VERSION, "0987654321");
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Incorrect API key", e.getMessage());
        }
    }

    @Test
    void getWorkspace_ReturnsTheWorkspace_WhenTheApiKeyIsNotSpecifiedAndTheAuthenticatedCanReadTheWorkspace() {
        setUser("read@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String json = controller.getWorkspace(1, WorkspaceVersion.LATEST_VERSION, "");
        assertEquals("json", json);
    }

    @Test
    void getWorkspace_ReturnsAnError_WhenTheApiKeyIsNotSpecifiedAndTheAuthenticatedUserCannotReadTheWorkspace() {
        setUser("user2@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        try {
            controller.getWorkspace(1, WorkspaceVersion.LATEST_VERSION, "");
            fail();
        } catch (Exception e) {
            assertEquals("API key must be provided", e.getMessage());
        }
    }

    @Test
    void getWorkspace_ReturnsTheWorkspace_WhenTheApiKeyIsCorrectAndTheWorkspaceApiKeyIsStoredAsPlaintext() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String json = controller.getWorkspace(1, WorkspaceVersion.LATEST_VERSION, "1234567890");
        assertEquals("json", json);
    }

    @Test
    void getWorkspace_ReturnsTheWorkspace_WhenTheApiKeyIsCorrectAndTheWorkspaceApiKeyIsStoredAsBcrypt() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String json = controller.getWorkspace(1, WorkspaceVersion.LATEST_VERSION, "1234567890");
        assertEquals("json", json);
    }

    @Test
    void getWorkspace_ReturnsTheWorkspace_WhenTheApiKeyIsCorrectAndTheAdminApiKeyIsUsed() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.API_KEY, new BCryptPasswordEncoder().encode("admin-1234567890"));
        enableAuthentication(properties);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String json = controller.getWorkspace(1, WorkspaceVersion.LATEST_VERSION, "admin-1234567890");
        assertEquals("json", json);
    }

    @Test
    void putWorkspace_ReturnsAnError_WhenNoApiKeyIsSpecified() {
        try {
            controller.putWorkspace(1, "json", "");
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("API key must be provided", e.getMessage());
        }
    }

    @Test
    void putWorkspace_ReturnsAnError_WhenTheApiKeyIsIncorrect() {
        try {
            controller.putWorkspace(1, "json", "0987654321");
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Incorrect API key", e.getMessage());
        }
    }

    @Test
    void putWorkspace_PutsTheWorkspace_WhenTheApiKeyIsNotSpecifiedAndTheAuthenticatedCanWriteTheWorkspace() throws Exception {
        setUser("write@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }
        });

        Workspace workspace = new Workspace("Name", "Description");
        String json = WorkspaceUtils.toJson(workspace, false);

        controller.putWorkspace(1, json, "");
    }

    @Test
    void putWorkspace_ReturnsAnError_WhenTheApiKeyIsNotSpecifiedAndTheAuthenticatedUserCannotWriteTheWorkspace() throws Exception {
        setUser("read@example.com");

        Workspace workspace = new Workspace("Name", "Description");
        String json = WorkspaceUtils.toJson(workspace, false);

        try {
            controller.putWorkspace(1, json, "");
            fail();
        } catch (Exception e) {
            assertEquals("Missing permission Write", e.getMessage());
        }
    }

    @Test
    void putWorkspace_PutsTheWorkspace_WhenTheApiKeyIsCorrectAndTheApiKeyIsStoredAsPlaintext() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        String json = WorkspaceUtils.toJson(workspace, false);

        controller.putWorkspace(1, json, "1234567890");
    }

    @Test
    void putWorkspace_PutsTheWorkspace_WhenTheApiKeyIsCorrectAndTheApiKeyIsStoredAsBcrypt() throws Exception {
        workspaceMetadata.setApiKey(new BCryptPasswordEncoder().encode("1234567890"));

        Workspace workspace = new Workspace("Name", "Description");
        String json = WorkspaceUtils.toJson(workspace, false);

        controller.putWorkspace(1, json, "1234567890");
    }

    @Test
    void putWorkspace_PutsTheWorkspace_WhenTheApiKeyIsCorrectAndTheAdminApiKeyIsUsed() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.API_KEY, new BCryptPasswordEncoder().encode("admin-1234567890"));
        enableAuthentication(properties);

        Workspace workspace = new Workspace("Name", "Description");
        String json = WorkspaceUtils.toJson(workspace, false);

        controller.putWorkspace(1, json, "admin-1234567890");
    }

    @Test
    void lockWorkspace_LocksTheWorkspace_WhenAnApiKeyIsProvidedAndTheWorkspaceIsUnlocked() throws Exception {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }

            @Override
            public boolean lockWorkspace(long workspaceId, String username, String agent) {
                workspaceMetadata.setLockedUser(username);
                workspaceMetadata.setLockedAgent(agent);
                workspaceMetadata.setLockedDate(new Date());

                return true;
            }
        });

        ApiResponse apiResponse = controller.lockWorkspace(1, "user@example.com", "agent", "1234567890");

        assertEquals("OK", apiResponse.getMessage());
        assertTrue(workspaceMetadata.isLocked());
        assertEquals("user@example.com", workspaceMetadata.getLockedUser());
        assertEquals("agent", workspaceMetadata.getLockedAgent());
    }

    @Test
    void lockWorkspace_ReturnsAnError_WhenAnApiKeyIsProvidedAndTheWorkspaceIsLocked() throws Exception {
        workspaceMetadata.setApiKey("1234567890");
        workspaceMetadata.setLockedUser("user1@example.com");
        workspaceMetadata.setLockedAgent("agent");

        Date date = new Date();
        workspaceMetadata.setLockedDate(date);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }

            @Override
            public boolean lockWorkspace(long workspaceId, String username, String agent) {
                return false;
            }
        });

        ApiResponse apiResponse = controller.lockWorkspace(1, "user2@example.com", "agent", "1234567890");

        assertEquals(String.format("The workspace could not be locked; it was locked by user1@example.com using agent at %s", new SimpleDateFormat(DateUtils.USER_FRIENDLY_DATE_FORMAT).format(workspaceMetadata.getLockedDate())), apiResponse.getMessage());
    }

    @Test
    void lockWorkspace_LocksTheWorkspace_WhenAnApiKeyIsNotProvidedAndTheAuthenticationUserHasWriteAccessAndTheWorkspaceIsNotLocked() {
        setUser("write@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }

            @Override
            public boolean lockWorkspace(long workspaceId, String username, String agent) {
                workspaceMetadata.addLock(username, agent);
                return true;
            }
        });

        ApiResponse apiResponse = controller.lockWorkspace(1, null, "agent", "");
        assertTrue(apiResponse.isSuccess());
        assertTrue(workspaceMetadata.isLocked());
        assertEquals("write@example.com", workspaceMetadata.getLockedUser());
        assertEquals("agent", workspaceMetadata.getLockedAgent());
    }

    @Test
    void lockWorkspace_ReturnsAnError_WhenAnApiKeyIsNotProvidedAndTheWorkspaceIsAlreadyLockedByADifferentUserAndAgent() {
        setUser("write@example.com");
        workspaceMetadata.addLock("admin@example.com", "agent");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }

            @Override
            public boolean lockWorkspace(long workspaceId, String username, String agent) {
                return false;
            }
        });

        ApiResponse apiResponse = controller.lockWorkspace(1, null, "agent", "");
        assertFalse(apiResponse.isSuccess());
        assertEquals(String.format("The workspace could not be locked; it was locked by admin@example.com using agent at %s", new SimpleDateFormat(DateUtils.USER_FRIENDLY_DATE_FORMAT).format(workspaceMetadata.getLockedDate())), apiResponse.getMessage());
        assertTrue(workspaceMetadata.isLocked());
        assertEquals("admin@example.com", workspaceMetadata.getLockedUser());
        assertEquals("agent", workspaceMetadata.getLockedAgent());
    }

    @Test
    void unlockWorkspace_UnlocksTheWorkspace_WhenAnApiKeyIsProvidedAndTheWorkspaceIsLocked() throws Exception {
        final WorkspaceMetadata workspaceMetadata = new WorkspaceMetadata(1);
        workspaceMetadata.setApiKey("1234567890");
        workspaceMetadata.setLockedUser("user@example.com");
        workspaceMetadata.setLockedAgent("agent");
        workspaceMetadata.setLockedDate(new Date());

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }

            @Override
            public boolean unlockWorkspace(long workspaceId) {
                workspaceMetadata.clearLock();
                return true;
            }
        });

        ApiResponse apiResponse = controller.unlockWorkspace(1, "user@example.com", "agent", "1234567890");

        assertEquals("OK", apiResponse.getMessage());
        assertFalse(workspaceMetadata.isLocked());
        assertNull(workspaceMetadata.getLockedUser());
        assertNull(workspaceMetadata.getLockedAgent());
    }

    @Test
    void getBranches_ReturnsAnError_WhenNoApiKeyIsSpecified() {
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        try {
            controller.getBranches(1, "");
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("API key must be provided", e.getMessage());
        }
    }

    @Test
    void getBranches_ReturnsAnError_WhenTheApiKeyIsIncorrectlySpecified() {
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        try {
            controller.getBranches(1, "0987654321");
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Incorrect API key", e.getMessage());
        }
    }

    @Test
    void getBranches_ReturnsAnError_WhenAnIncorrectApiKeyIsSpecified() {
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        try {
            controller.getBranches(1, "0987654321");
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Incorrect API key", e.getMessage());
        }
    }

    @Test
    void getBranches_ReturnsTheBranches_WhenTheApiKeyIsCorrect() throws Exception {
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }

            @Override
            public List<WorkspaceBranch> getWorkspaceBranches(long workspaceId) {
                return List.of(
                        new WorkspaceBranch("branch2"),
                        new WorkspaceBranch("branch1")
                );
            }
        });

        String json = controller.getBranches(1, "1234567890");
        assertEquals("""
                ["branch1","branch2"]""", json);
    }

    @Test
    void deleteBranch_ReturnsAnError_WhenBranchesAreNotEnabled() {
        Configuration.getInstance().setFeatureDisabled(Features.WORKSPACE_BRANCHES);

        try {
            controller.deleteBranch(1, "branch", "");
            fail();
        } catch (ApiException e) {
            assertEquals("Workspace branches are not enabled for this installation", e.getMessage());
        }
    }

    @Test
    void deleteBranch_ReturnsAnError_WhenNoApiKeyIsSpecified() {
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        try {
            controller.deleteBranch(1, "branch", "");
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("API key must be provided", e.getMessage());
        }
    }

    @Test
    void deleteBranch_ReturnsAnError_WhenTheApiKeyIsIncorrectlySpecified() {
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        try {
            controller.deleteBranch(1, "branch", "0987654321");
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Incorrect API key", e.getMessage());
        }
    }

    @Test
    void deleteBranch_ReturnsAnError_WhenAnIncorrectApiKeyIsSpecified() {
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        try {
            controller.deleteBranch(1, "branch", "0987654321");
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Incorrect API key", e.getMessage());
        }
    }

    @Test
    void deleteBranch_ReturnsAnError_WhenTheApiKeyIsCorrectlySpecifiedButTheMainBranchIsSpecified() throws Exception {
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
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
        try {
            controller.deleteBranch(1, "", "1234567890");
            fail();
        } catch (Exception e) {
            assertEquals("The main branch cannot be deleted", e.getMessage());
        }

        // 2. "main" as the branch
        try {
            controller.deleteBranch(1, "main", "1234567890");
            fail();
        } catch (Exception e) {
            assertEquals("The main branch cannot be deleted", e.getMessage());
        }
    }

    @Test
    void deleteBranch_ReturnsAnError_WhenTheApiKeyIsCorrectlySpecifiedAndTheBranchDoesNotExist() throws Exception {
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
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
        ApiResponse apiResponse = controller.deleteBranch(1, "branch3", "1234567890");
        assertFalse(apiResponse.isSuccess());
        assertEquals("Workspace branch \"branch3\" does not exist", apiResponse.getMessage());
    }

    @Test
    void deleteBranch_DeletesTheBranch_WhenTheApiKeyIsCorrectlySpecified() throws Exception {
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        StringBuilder buf = new StringBuilder();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
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

        ApiResponse apiResponse = controller.deleteBranch(1, "branch1", "1234567890");
        assertTrue(apiResponse.isSuccess());
        assertEquals("deleteBranch(1, branch1)", buf.toString());
    }

}