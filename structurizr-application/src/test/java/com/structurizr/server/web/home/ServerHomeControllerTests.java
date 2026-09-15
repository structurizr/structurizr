package com.structurizr.server.web.home;

import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.domain.User;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.AbstractTestsBase;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class ServerHomeControllerTests extends AbstractTestsBase {

    private ServerHomeController controller;
    private ModelMap model;

    @BeforeEach
    void setUp() {
        controller = new ServerHomeController();

        model = new ModelMap();
    }

    @Test
    void showHomePage_WhenAuthenticationIsDisabled() {
        configureAsServerWithAuthenticationDisabled();

        WorkspaceMetadata workspace1 = new WorkspaceMetadata(1);
        WorkspaceMetadata workspace2 = new WorkspaceMetadata(2);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public List<WorkspaceMetadata> getWorkspaces(User user) {
                return List.of(workspace1, workspace2);
            }
        });

        String result = controller.showHomePage("", 1, 1, model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));

        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1));
        assertFalse(((Collection)model.getAttribute("workspaces")).contains(workspace2));

        assertTrue(((Collection)model.getAttribute("quickNavigationItems")).contains(workspace1));
        assertTrue(((Collection)model.getAttribute("quickNavigationItems")).contains(workspace2));

        assertEquals("home", result);
        assertEquals(true, model.getAttribute("userCanCreateWorkspace"));
    }


    @Test
    void showHomePage_WhenAuthenticationIsEnabledAndTheUserIsAuthenticatedAndHasAdminPermission() {
        configureAsServerWithAuthenticationEnabled();
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
        configureAsServerWithAuthenticationEnabled(properties);
        setUser("user@example.com");

        WorkspaceMetadata workspace1 = new WorkspaceMetadata(1);
        WorkspaceMetadata workspace2 = new WorkspaceMetadata(2);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public List<WorkspaceMetadata> getWorkspaces(User user) {
                return List.of(workspace1, workspace2);
            }
        });

        String result = controller.showHomePage("", 1, 1, model);

        assertEquals(1, model.getAttribute("numberOfWorkspaces"));

        assertTrue(((Collection)model.getAttribute("workspaces")).contains(workspace1));
        assertFalse(((Collection)model.getAttribute("workspaces")).contains(workspace2));

        assertTrue(((Collection)model.getAttribute("quickNavigationItems")).contains(workspace1));
        assertTrue(((Collection)model.getAttribute("quickNavigationItems")).contains(workspace2));

        assertEquals("home", result);
        assertEquals(false, model.getAttribute("userCanCreateWorkspace"));
    }

    @Test
    void showAuthenticatedDashboard_WhenAuthenticationIsEnabledAndTheUserIsAuthenticatedAndTheUserIsAnAdmin() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        configureAsServerWithAuthenticationEnabled(properties);
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
        configureAsServerWithAuthenticationEnabled();

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