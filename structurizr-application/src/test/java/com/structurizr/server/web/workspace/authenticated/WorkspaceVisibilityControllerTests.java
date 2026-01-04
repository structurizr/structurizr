package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class WorkspaceVisibilityControllerTests extends ControllerTestsBase {

    private WorkspaceVisibilityController controller;
    private ModelMap model;

    @BeforeEach
    void setUp() {
        controller = new WorkspaceVisibilityController();
        model = new ModelMap();
    }

    @Test
    void changeVisibility_ReturnsTheFeatureNotAvailablePage_WhenAuthenticationIsDisabled() {
        disableAuthentication();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.changeVisibility(1, "public", model);
        assertEquals("feature-not-available", view);
    }

    @Test
    void changeVisibility_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        enableAuthentication();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.changeVisibility(1, "public", model);
        assertEquals("404", view);
    }

    @Test
    void changeVisibility_DoesNothingAndRedirectsToTheWorkspaceSettingsPage_WhenAuthenticationIsEnabledAndTheUserIsNotAnAdmin() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        enableAuthentication(properties);
        setUser("user@example.com");

        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user@example.com");
        assertFalse(workspaceMetaData.isPublicWorkspace());

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void makeWorkspacePublic(long workspaceId) throws WorkspaceComponentException {
                fail();
            }
        });

        String view = controller.changeVisibility(1, "public", model);
        assertEquals("redirect:/workspace/1/settings", view);
        assertFalse(workspaceMetaData.isPublicWorkspace());
    }

    @Test
    void changeVisibility_MakesTheWorkspacePublic_WhenAuthenticationIsEnabledAndTheUserIsAnAdmin() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        enableAuthentication(properties);
        setUser("admin@example.com");

        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        assertFalse(workspaceMetaData.isPublicWorkspace());

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void makeWorkspacePublic(long workspaceId) throws WorkspaceComponentException {
                workspaceMetaData.setPublicWorkspace(true);
            }
        });

        String view = controller.changeVisibility(1, "public", model);
        assertEquals("redirect:/workspace/1/settings", view);
        assertTrue(workspaceMetaData.isPublicWorkspace());
    }

    @Test
    void changeVisibility_MakesTheWorkspacePublic_WhenAuthenticationIsEnabledAndNoAdminUsersAreDefined() {
        enableAuthentication();
        setUser("user@example.com");

        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user@example.com");
        assertFalse(workspaceMetaData.isPublicWorkspace());

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void makeWorkspacePublic(long workspaceId) throws WorkspaceComponentException {
                workspaceMetaData.setPublicWorkspace(true);
            }
        });

        String view = controller.changeVisibility(1, "public", model);
        assertEquals("redirect:/workspace/1/settings", view);
        assertTrue(workspaceMetaData.isPublicWorkspace());
    }

    @Test
    void changeVisibility_MakesTheWorkspacePrivate_WhenAuthenticationIsEnabledAndNoAdminUsersAreDefined() {
        enableAuthentication();
        setUser("user@example.com");

        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user@example.com");
        workspaceMetaData.setPublicWorkspace(true);
        assertTrue(workspaceMetaData.isPublicWorkspace());

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void makeWorkspacePrivate(long workspaceId) {
                workspaceMetaData.setPublicWorkspace(false);
            }
        });

        String view = controller.changeVisibility(1, "private", model);
        assertEquals("redirect:/workspace/1/settings", view);
        assertFalse(workspaceMetaData.isPublicWorkspace());
    }

    @Test
    void changeVisibility_MakesTheWorkspaceShared_WhenAuthenticationIsEnabledAndNoAdminUsersAreDefined() {
        enableAuthentication();
        setUser("user@example.com");

        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user@example.com");
        assertFalse(workspaceMetaData.isShareable());

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void shareWorkspace(long workspaceId) {
                workspaceMetaData.setSharingToken("token");
            }
        });

        String view = controller.changeVisibility(1, "share", model);
        assertEquals("redirect:/workspace/1/settings", view);
        assertTrue(workspaceMetaData.isShareable());
        assertEquals("token", workspaceMetaData.getSharingToken());
    }

    @Test
    void changeVisibility_MakesTheWorkspaceUnshared_WhenAuthenticationIsEnabledAndNoAdminUsersAreDefined() {
        enableAuthentication();
        setUser("user@example.com");

        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user@example.com");
        workspaceMetaData.setSharingToken("token");
        assertTrue(workspaceMetaData.isShareable());

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void unshareWorkspace(long workspaceId) {
                workspaceMetaData.setSharingToken("");
            }
        });

        String view = controller.changeVisibility(1, "unshare", model);
        assertEquals("redirect:/workspace/1/settings", view);
        assertFalse(workspaceMetaData.isShareable());
        assertEquals("", workspaceMetaData.getSharingToken());
    }

    @Test
    void changeVisibility_ReturnsThe404Page_WhenAnInvalidActionIsSpecified() {
        enableAuthentication();
        setUser("user@example.com");

        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        String view = controller.changeVisibility(1, "action", model);
        assertEquals("404", view);
    }

}