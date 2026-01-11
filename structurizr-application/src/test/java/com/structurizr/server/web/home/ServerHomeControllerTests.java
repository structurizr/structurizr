package com.structurizr.server.web.home;

import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.domain.User;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerHomeControllerTests extends ControllerTestsBase {

    private ServerHomeController controller;
    private ModelMap model;

    @BeforeEach
    void setUp() {
        controller = new ServerHomeController();

        model = new ModelMap();
    }

    @Test
    void showHomePage_WhenAuthenticationIsDisabled() {
        disableAuthentication();

        WorkspaceMetadata workspace1 = new WorkspaceMetadata(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public List<WorkspaceMetadata> getWorkspaces(User user) {
                return List.of(workspace1);
            }
        });

        String result = controller.showHomePage("", 1, 20, model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1));
        assertEquals("home", result);
        assertEquals(true, model.getAttribute("userCanCreateWorkspace"));
    }


    @Test
    void showHomePage_WhenAuthenticationIsEnabledAndTheUserIsAuthenticatedAndHasAdminPermission() {
        enableAuthentication();
        setUser("user@example.com");

        WorkspaceMetadata workspace1 = new WorkspaceMetadata(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public List<WorkspaceMetadata> getWorkspaces(User user) {
                return List.of(workspace1);
            }
        });

        String result = controller.showHomePage("", 1, 20, model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1));
        assertEquals("home", result);
        assertEquals(true, model.getAttribute("userCanCreateWorkspace"));
    }

    @Test
    void showAuthenticatedDashboard_WhenAuthenticationIsEnabledAndTheUserIsAuthenticatedAndDoesNotHaveAdminPermission() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        enableAuthentication(properties);
        setUser("user@example.com");

        WorkspaceMetadata workspace1 = new WorkspaceMetadata(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public List<WorkspaceMetadata> getWorkspaces(User user) {
                return List.of(workspace1);
            }
        });

        String result = controller.showHomePage("", 1, 20, model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1));
        assertEquals("home", result);
        assertEquals(false, model.getAttribute("userCanCreateWorkspace"));
    }

    @Test
    void showAuthenticatedDashboard_WhenAuthenticationIsEnabledAndTheUserIsAuthenticatedAndTheUserIsAnAdmin() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        enableAuthentication(properties);
        setUser("admin@example.com");

        WorkspaceMetadata workspace1 = new WorkspaceMetadata(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public List<WorkspaceMetadata> getWorkspaces(User user) {
                return List.of(workspace1);
            }
        });

        String result = controller.showHomePage("", 1, 20, model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1));
        assertEquals("home", result);
        assertEquals(true, model.getAttribute("userCanCreateWorkspace"));
    }

    @Test
    void showAuthenticatedDashboard_WhenAuthenticationIsEnabledAndTheUserIsNotAuthenticated() {
        enableAuthentication();

        WorkspaceMetadata workspace1 = new WorkspaceMetadata(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public List<WorkspaceMetadata> getWorkspaces(User user) {
                return List.of(workspace1);
            }
        });

        String result = controller.showHomePage("", 1, 20, model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1));
        assertEquals("home", result);
        assertEquals(false, model.getAttribute("userCanCreateWorkspace"));
    }

}