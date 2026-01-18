package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import static org.junit.jupiter.api.Assertions.*;

public class LockControllerTests extends ControllerTestsBase {

    private LockController controller;
    private ModelMap model;

    @BeforeEach
    public void setUp() {
        controller = new LockController();
        model = new ModelMap();
        enableAuthentication();
    }

    @Test
    void forceUnlockWorkspace_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return null;
            }
        });

        setUser("user@example.com");
        String view = controller.forceUnlockWorkspace(1, model);
        assertEquals("404", view);
    }

    @Test
    void forceUnlockWorkspace_ReturnsThe404Page_WhenTheUserDoesNotHaveAccess() {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.addWriteUser("user2@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }
        });

        setUser("user1@example.com");
        String view = controller.forceUnlockWorkspace(1, model);
        assertEquals("404", view);
    }

    @Test
    void forceUnlockWorkspace_UnlocksTheWorkspace_WhenTheWorkspaceHasNoUsersConfigured()  {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.addLock("user@example.com", "agent");

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

        setUser("user@example.com");
        String view = controller.forceUnlockWorkspace(1, model);
        assertEquals("redirect:/workspace/1", view);
        assertFalse(workspaceMetaData.isLocked());
    }

    @Test
    void forceUnlockWorkspace_UnlocksTheWorkspace_WhenTheUserHasAdminAccess()  {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.addWriteUser("user@example.com");
        workspaceMetaData.addLock("user@example.com", "agent");

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

        setUser("user@example.com");
        String view = controller.forceUnlockWorkspace(1, model);
        assertEquals("redirect:/workspace/1", view);
        assertFalse(workspaceMetaData.isLocked());
    }

    @Test
    void forceUnlockWorkspace_ReturnsThe404Page_WhenTheUserHasReadAccess()  {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.addReadUser("user@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }
        });

        setUser("user@example.com");
        String view = controller.forceUnlockWorkspace(1, model);
        assertEquals("404", view);
    }

    @Test
    void unlockWorkspaceViaBeacon_UnlocksTheWorkspace_WhenTheWorkspaceIsLockedByTheSameAgent()  {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.addLock("user1@example.com", "agent");

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

        setUser("user1@example.com");
        assertTrue(workspaceMetaData.isLocked());
        controller.unlockWorkspaceViaBeacon(1, "agent");
        assertFalse(workspaceMetaData.isLocked());
    }

    @Test
    void unlockWorkspaceViaBeacon_DoesNotUnlockTheWorkspace_WhenTheWorkspaceIsLockedByADifferentAgent()  {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.addLock("user1@example.com", "agent");

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

        setUser("user2@example.com");
        assertTrue(workspaceMetaData.isLocked());
        controller.unlockWorkspaceViaBeacon(1, "agent");
        assertTrue(workspaceMetaData.isLocked());
    }

}