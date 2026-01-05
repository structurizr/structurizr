package com.structurizr.server.web.home;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.domain.User;
import com.structurizr.server.domain.WorkspaceMetaData;
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

public class HomePageControllerTests extends ControllerTestsBase {

    private ServerHomeController controller;
    private ModelMap model;

    @BeforeEach
    void setUp() {
        controller = new ServerHomeController();

        model = new ModelMap();
    }

    @Test
    void showUnauthenticatedHomePage_WhenUnauthenticated() {
        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public List<WorkspaceMetaData> getWorkspaces(User user) {
                return List.of(workspace1);
            }
        });
        clearUser();

        String result = controller.showHomePage("", 1, 20, model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1));
        assertEquals("home", result);
    }

    @Test
    void showUnauthenticatedHomePage_WhenAuthenticated() {
        setUser("user@example.com");

        String result = controller.showHomePage("", 1, 20, model);

        assertEquals("redirect:/dashboard", result);
    }

    @Test
    void showAuthenticatedDashboard_WhenAuthenticatedAndNoAdminUsersHaveBeenDefined() {
        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public List<WorkspaceMetaData> getWorkspaces(User user) {
                return List.of(workspace1);
            }
        });
        setUser("user@example.com");

        String result = controller.showHomePage("", 1, 20, model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1));
        assertEquals("dashboard", result);
        assertEquals(true, model.getAttribute("userCanCreateWorkspace"));
    }

    @Test
    void showAuthenticatedDashboard_WhenAuthenticatedAndTheUserIsNotAnAdmin() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        enableAuthentication(properties);
        setUser("user@example.com");

        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public List<WorkspaceMetaData> getWorkspaces(User user) {
                return List.of(workspace1);
            }
        });

        String result = controller.showHomePage("", 1, 20, model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1));
        assertEquals("dashboard", result);
        assertEquals(false, model.getAttribute("userCanCreateWorkspace"));
    }

    @Test
    void showAuthenticatedDashboard_WhenAuthenticatedAndTheUserIsAnAdmin() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        enableAuthentication(properties);
        setUser("admin@example.com");

        WorkspaceMetaData workspace1 = new WorkspaceMetaData(1);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public List<WorkspaceMetaData> getWorkspaces(User user) {
                return List.of(workspace1);
            }
        });

        String result = controller.showHomePage("", 1, 20, model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));
        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1));
        assertEquals("dashboard", result);
        assertEquals(true, model.getAttribute("userCanCreateWorkspace"));
    }

}