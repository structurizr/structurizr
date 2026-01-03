package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.component.workspace.WorkspaceLockResponse;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockWorkspaceComponent;
import com.structurizr.util.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import java.text.SimpleDateFormat;

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
    void lockWorkspace_ReturnsAFailureResponse_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        setUser("user@example.com");
        WorkspaceLockResponse response = controller.lockWorkspace(1, "agent");
        assertFalse(response.isSuccess());
        assertFalse(response.isLocked());
    }

    @Test
    void lockWorkspace_LocksTheWorkspace_WhenTheWorkspaceIsNotLocked() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public boolean lockWorkspace(long workspaceId, String username, String agent) {
                workspaceMetaData.addLock(username, agent);
                return true;
            }
        });

        setUser("user@example.com");
        WorkspaceLockResponse response = controller.lockWorkspace(1, "agent");
        assertTrue(response.isSuccess());
        assertTrue(response.isLocked());
        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user@example.com", workspaceMetaData.getLockedUser());
        assertEquals("agent", workspaceMetaData.getLockedAgent());
    }

    @Test
    void lockWorkspace_ReturnsAFailureResponse_WhenTheWorkspaceIsAlreadyLockedByAnotherAgent() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addLock("user1@example.com", "agent1");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public boolean lockWorkspace(long workspaceId, String username, String agent) {
                return false;
            }
        });

        setUser("user1@example.com");
        WorkspaceLockResponse response = controller.lockWorkspace(1, "agent2");
        assertFalse(response.isSuccess());
        assertTrue(response.isLocked());
        assertEquals(String.format("The workspace could not be locked; it was locked by user1@example.com using agent1 at %s.", new SimpleDateFormat(DateUtils.USER_FRIENDLY_DATE_FORMAT).format(workspaceMetaData.getLockedDate())), response.getMessage());
        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user1@example.com", workspaceMetaData.getLockedUser());
        assertEquals("agent1", workspaceMetaData.getLockedAgent());
    }

    @Test
    void lockWorkspace_ReturnsAFailureResponse_WhenTheWorkspaceIsAlreadyLockedBySomebodyElse() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addLock("user1@example.com", "agent");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public boolean lockWorkspace(long workspaceId, String username, String agent) {
                return false;
            }
        });

        setUser("user2@example.com");
        WorkspaceLockResponse response = controller.lockWorkspace(1, "agent");
        assertFalse(response.isSuccess());
        assertTrue(response.isLocked());
        assertEquals(String.format("The workspace could not be locked; it was locked by user1@example.com using agent at %s.", new SimpleDateFormat(DateUtils.USER_FRIENDLY_DATE_FORMAT).format(workspaceMetaData.getLockedDate())), response.getMessage());
        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user1@example.com", workspaceMetaData.getLockedUser());
        assertEquals("agent", workspaceMetaData.getLockedAgent());
    }

    @Test
    void unlockWorkspace_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        setUser("user@example.com");
        String view = controller.unlockWorkspace(1, model);
        assertEquals("404", view);
    }

    @Test
    void unlockWorkspace_ReturnsThe404Page_WhenTheUserDoesNotHaveAccess() {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user2@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        setUser("user1@example.com");
        String view = controller.unlockWorkspace(1, model);
        assertEquals("404", view);
    }

    @Test
    void unlockWorkspace_UnlocksTheWorkspace_WhenTheWorkspaceHasNoUsersConfigured()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addLock("user@example.com", "agent");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public boolean unlockWorkspace(long workspaceId) {
                workspaceMetaData.clearLock();
                return true;
            }
        });

        setUser("user@example.com");
        String view = controller.unlockWorkspace(1, model);
        assertEquals("redirect:/workspace/1", view);
        assertFalse(workspaceMetaData.isLocked());
    }

    @Test
    void unlockWorkspace_UnlocksTheWorkspace_WhenTheUserHasWriteAccess()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user@example.com");
        workspaceMetaData.addLock("user@example.com", "agent");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public boolean unlockWorkspace(long workspaceId) {
                workspaceMetaData.clearLock();
                return true;
            }
        });

        setUser("user@example.com");
        String view = controller.unlockWorkspace(1, model);
        assertEquals("redirect:/workspace/1", view);
        assertFalse(workspaceMetaData.isLocked());
    }

    @Test
    void unlockWorkspace_ReturnsThe404Page_WhenTheUserHasReadAccess()  {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addReadUser("user@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        setUser("user@example.com");
        String view = controller.unlockWorkspace(1, model);
        assertEquals("404", view);
    }

}