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
import com.structurizr.util.ImageUtils;
import com.structurizr.util.WorkspaceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;
import java.io.IOException;
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
    void putWorkspace_PutsTheWorkspace_WhenTheApiKeyIsNotSpecifiedAndTheAuthenticatedUserCanWriteTheWorkspace() throws Exception {
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
                {"id":1,"branches":["branch1","branch2"]}""", json);
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

    @Test
    void putImage_ReturnsAnError_WhenNoApiKeyIsSpecified() {
        try {
            controller.putImage(1, "image.png", "datauri", "");
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("API key must be provided", e.getMessage());
        }
    }

    @Test
    void putImage_ReturnsAnError_WhenTheApiKeyIsIncorrect() {
        try {
            controller.putImage(1, "image.png", "datauri", "0987654321");
            fail();
        } catch (HttpUnauthorizedException e) {
            assertEquals("Incorrect API key", e.getMessage());
        }
    }

    @Test
    void putImage_PutsTheImage_WhenTheApiKeyIsCorrect() throws Exception {
        StringBuilder buf = new StringBuilder();

        workspaceMetadata.setApiKey(new BCryptPasswordEncoder().encode("1234567890"));

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }

            @Override
            public boolean putImage(long workspaceId, String branch, String filename, File file) {
                try {
                    buf.append(filename + " -> " + ImageUtils.getImageAsDataUri(file));
                } catch (IOException e) {
                    return false;
                }
                return true;
            }
        });

        String image = ImageUtils.getImageAsDataUri(new File("src/test/images/structurizr-logo.png"));

        ApiResponse response = controller.putImage(1, "filename.png", image, "1234567890");

        assertTrue(response.isSuccess());
        assertEquals("OK", response.getMessage());
        assertEquals("filename.png -> data:image/png;base64,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", buf.toString());
    }

}