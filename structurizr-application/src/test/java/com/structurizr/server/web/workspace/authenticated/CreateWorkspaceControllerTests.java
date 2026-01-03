package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.User;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateWorkspaceControllerTests extends ControllerTestsBase {

    private CreateWorkspaceController controller;
    private ModelMap model;

    @BeforeEach
    void setUp() {
        controller = new CreateWorkspaceController();
        model = new ModelMap();
    }

    @Test
    public void createWorkspace_CreatesAWorkspace_WhenAuthenticationIsDisabled() {
        disableAuthentication();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public long createWorkspace(User user) throws WorkspaceComponentException {
                return 1;
            }
        });
        String result = controller.createWorkspace(model);

        assertEquals("redirect:/workspace/1", result);
    }

    @Test
    void createWorkspace_CreatesAWorkspace_WhenAuthenticationIsEnabledAndNoAdminUsersAreDefined() {
        enableAuthentication();
        setUser("user@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public long createWorkspace(User user) throws WorkspaceComponentException {
                return 1;
            }
        });
        String result = controller.createWorkspace(model);

        assertEquals("redirect:/workspace/1", result);
    }

    @Test
    void createWorkspace_CreatesAWorkspace_WhenAuthenticationIsEnabledAndAnAdminUserIsDefined() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        enableAuthentication(properties);
        setUser("admin@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public long createWorkspace(User user) throws WorkspaceComponentException {
                return 1;
            }
        });
        String result = controller.createWorkspace(model);

        assertEquals("redirect:/workspace/1", result);
    }

    @Test
    void createWorkspace_ReturnsThe404Page_WhenAuthenticationIsEnabledAndTheUserIsNotAnAdmin() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        enableAuthentication(properties);
        setUser("user@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {});
        String result = controller.createWorkspace(model);

        assertEquals("404", result);
    }

}