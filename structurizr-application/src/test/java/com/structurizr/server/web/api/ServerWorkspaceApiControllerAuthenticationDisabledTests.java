package com.structurizr.server.web.api;

import com.structurizr.Workspace;
import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.component.workspace.WorkspaceVersion;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockHttpServletRequest;
import com.structurizr.server.web.MockWorkspaceComponent;
import com.structurizr.util.DateUtils;
import com.structurizr.util.WorkspaceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerWorkspaceApiControllerAuthenticationDisabledTests extends ControllerTestsBase {

    private ServerWorkspaceApiController controller;
    private final MockHttpServletRequest request = new MockHttpServletRequest();

    @BeforeEach
    void setUp() {
        controller = new ServerWorkspaceApiController();
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {});

        disableAuthentication();
    }

    @Test
    void getWorkspace_ReturnsAnError_WhenANegativeWorkspaceIdIsSpecified() {
        try {
            controller.getWorkspace(-1, "json", "");
            fail();
        } catch (ApiException e) {
            assertEquals("Workspace ID must be greater than 1", e.getMessage());
        }
    }

    @Test
    void getWorkspace_ReturnsAnError_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return null;
            }
        });

        try {
            controller.getWorkspace(1, "json", "");
            fail();
        } catch (ApiException e) {
            assertEquals("404", e.getMessage());
        }
    }

    @Test
    void getWorkspace_ReturnsTheWorkspace() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return new WorkspaceMetadata(1);
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
    void putWorkspace_ReturnsAnError_WhenANegativeWorkspaceIdIsSpecified() {
        try {
            controller.putWorkspace(-1, "json", "");
            fail();
        } catch (ApiException e) {
            assertEquals("Workspace ID must be greater than 1", e.getMessage());
        }
    }

    @Test
    void putWorkspace_ReturnsAnError_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return null;
            }
        });

        try {
            controller.putWorkspace(1, "json", "");
            fail();
        } catch (ApiException e) {
            assertEquals("404", e.getMessage());
        }
    }

    @Test
    void putWorkspace_PutsTheWorkspace() throws Exception {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return new WorkspaceMetadata(1);
            }
        });

        Workspace workspace = new Workspace("Name", "Description");
        String json = WorkspaceUtils.toJson(workspace, false);

        controller.putWorkspace(1, json, "");
    }

    @Test
    void lockWorkspace_ReturnsAnError_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return null;
            }
        });

        try {
            controller.lockWorkspace(1, null, "agent", "");
            fail();
        } catch (ApiException e) {
            assertEquals("404", e.getMessage());
        }
    }

    @Test
    void lockWorkspace_LocksTheWorkspace_WhenTheWorkspaceIsUnlocked() throws Exception {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);

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

        ApiResponse apiResponse = controller.lockWorkspace(1, "user@example.com", "structurizr-java/1.2.3", "");

        assertEquals("OK", apiResponse.getMessage());
        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user@example.com", workspaceMetaData.getLockedUser());
        assertEquals("structurizr-java/1.2.3", workspaceMetaData.getLockedAgent());

        // and again
        apiResponse = controller.lockWorkspace(1, "user@example.com", "structurizr-java/1.2.3", "");

        assertEquals("OK", apiResponse.getMessage());
        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user@example.com", workspaceMetaData.getLockedUser());
        assertEquals("structurizr-java/1.2.3", workspaceMetaData.getLockedAgent());
    }

    @Test
    void lockWorkspace_ReturnsAnError_WhenTheWorkspaceIsLockedBySomebodyElse() throws Exception {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
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

        ApiResponse apiResponse = controller.lockWorkspace(1, "user2@example.com", "structurizr-java/1.2.3", "");

        assertEquals(String.format("The workspace could not be locked; it was locked by user1@example.com using structurizr-web/123 at %s", new SimpleDateFormat(DateUtils.USER_FRIENDLY_DATE_FORMAT).format(workspaceMetaData.getLockedDate())), apiResponse.getMessage());
    }

    @Test
    void unlockWorkspace_UnlocksTheWorkspace_WhenTheWorkspaceIsLockedIsLockedByTheSameUserAndAgent() throws Exception {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setLockedUser("user@example.com");
        workspaceMetaData.setLockedAgent("agent");
        workspaceMetaData.setLockedDate(DateUtils.getXMinutesAgo(1));

        assertTrue(workspaceMetaData.isLocked());
        assertTrue(workspaceMetaData.isLockedBy("user@example.com", "agent"));

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

        ApiResponse apiResponse = controller.unlockWorkspace(1, "user@example.com", "agent", "");

        assertEquals("OK", apiResponse.getMessage());
        assertFalse(workspaceMetaData.isLocked());
        assertNull(workspaceMetaData.getLockedUser());
        assertNull(workspaceMetaData.getLockedAgent());
    }

    @Test
    void unlockWorkspace_ReturnsAnError_WhenTheWorkspaceIsLockedByADifferentUserAndAgent() throws Exception {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setLockedUser("user1@example.com");
        workspaceMetaData.setLockedAgent("agent");
        workspaceMetaData.setLockedDate(DateUtils.getXMinutesAgo(1));

        assertTrue(workspaceMetaData.isLocked());
        assertTrue(workspaceMetaData.isLockedBy("user1@example.com", "agent"));

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

        ApiResponse apiResponse = controller.unlockWorkspace(1, "user2@example.com", "agent", "");

        assertEquals("Could not unlock workspace", apiResponse.getMessage());
        assertTrue(workspaceMetaData.isLockedBy("user1@example.com", "agent"));
    }

    @Test
    void getBranches_ReturnsAnError_WhenBranchesAreNotEnabled() {
        Configuration.getInstance().setFeatureDisabled(Features.WORKSPACE_BRANCHES);

        try {
            controller.getBranches(1, "");
            fail();
        } catch (ApiException e) {
            assertEquals("Workspace branches are not enabled for this installation", e.getMessage());
        }
    }

    @Test
    void getBranches_ReturnsAnError_WhenBranchesAreEnabledButTheWorkspaceDoesNotExist() {
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return null;
            }
        });

        try {
            controller.getBranches(1, "");
            fail();
        } catch (ApiException e) {
            assertEquals("404", e.getMessage());
        }
    }

    @Test
    void getBranches_ReturnsTheBranches() throws Exception {
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return new WorkspaceMetadata(1);
            }

            @Override
            public List<WorkspaceBranch> getWorkspaceBranches(long workspaceId) {
                return List.of(
                        new WorkspaceBranch("branch2"),
                        new WorkspaceBranch("branch1")
                );
            }
        });

        String json = controller.getBranches(1, "");
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
    void deleteBranch_ReturnsAnError_WhenBranchesAreEnabledButTheWorkspaceDoesNotExist() {
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return null;
            }
        });

        try {
            controller.deleteBranch(1, "branch", "");
            fail();
        } catch (ApiException e) {
            assertEquals("404", e.getMessage());
        }
    }

    @Test
    void deleteBranch_ReturnsAnError_WhenTheMainBranchIsSpecified() throws Exception {
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return new WorkspaceMetadata(1);
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
            controller.deleteBranch(1, "", "");
            fail();
        } catch (Exception e) {
            assertEquals("The main branch cannot be deleted", e.getMessage());
        }

        // 2. "main" as the branch
        try {
            controller.deleteBranch(1, "main", "");
            fail();
        } catch (Exception e) {
            assertEquals("The main branch cannot be deleted", e.getMessage());
        }
    }

    @Test
    void deleteBranch_ReturnsAnError_WhenTheBranchDoesNotExist() throws Exception {
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return new WorkspaceMetadata(1);
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
        ApiResponse apiResponse = controller.deleteBranch(1, "branch3", "");
        assertFalse(apiResponse.isSuccess());
        assertEquals("Workspace branch \"branch3\" does not exist", apiResponse.getMessage());
    }

    @Test
    void deleteBranch_DeletesTheBranch() throws Exception {
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        StringBuilder buf = new StringBuilder();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return new WorkspaceMetadata(1);
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

        ApiResponse apiResponse = controller.deleteBranch(1, "branch1", "");
        assertTrue(apiResponse.isSuccess());
        assertEquals("deleteBranch(1, branch1)", buf.toString());
    }

}