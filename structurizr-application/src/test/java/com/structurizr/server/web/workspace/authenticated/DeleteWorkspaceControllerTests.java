package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockSearchComponent;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class DeleteWorkspaceControllerTests extends ControllerTestsBase {

    private DeleteWorkspaceController controller;
    private ModelMap model;

    @BeforeEach
    void setUp() {
        controller = new DeleteWorkspaceController();
        model = new ModelMap();
    }

    @Test
    void deleteWorkspace_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        disableAuthentication();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.deleteWorkspace(1, model);
        assertEquals("404", view);
    }

    @Test
    void deleteWorkspace_RedirectsToTheDashboard_WhenAuthenticationIsEnabledAndTheUserIsNotAnAdmin() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        enableAuthentication(properties);
        setUser("user@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return new WorkspaceMetaData(1);
            }

            @Override
            public boolean deleteWorkspace(long workspaceId) throws WorkspaceComponentException {
                fail();
                return true;
            }
        });

        String view = controller.deleteWorkspace(1, model);
        assertEquals("redirect:/dashboard", view);
    }

    @Test
    void deleteWorkspace_DeletesTheWorkspace_WhenAuthenticationIsDisabled() {
        disableAuthentication();
        setUser("user@example.com");

        final StringBuilder buf = new StringBuilder();
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return new WorkspaceMetaData(1);
            }

            @Override
            public boolean deleteWorkspace(long workspaceId) throws WorkspaceComponentException {
                buf.append("1 ");
                return true;
            }
        });

        controller.setSearchComponent(new MockSearchComponent() {
            @Override
            public void delete(long workspaceId) {
                buf.append("2");
            }
        });

        String view = controller.deleteWorkspace(1, model);
        assertEquals("redirect:/dashboard", view);
        assertEquals("1 2", buf.toString());
    }

    @Test
    void deleteWorkspace_DeletesTheWorkspace_WhenAuthenticationIsEnabledAndNoAdminUsersAreDefined() {
        enableAuthentication();
        setUser("user@example.com");

        final StringBuilder buf = new StringBuilder();
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return new WorkspaceMetaData(1);
            }

            @Override
            public boolean deleteWorkspace(long workspaceId) throws WorkspaceComponentException {
                buf.append("1 ");
                return true;
            }
        });

        controller.setSearchComponent(new MockSearchComponent() {
            @Override
            public void delete(long workspaceId) {
                buf.append("2");
            }
        });

        String view = controller.deleteWorkspace(1, model);
        assertEquals("redirect:/dashboard", view);
        assertEquals("1 2", buf.toString());
    }

    @Test
    void deleteWorkspace_DeletesTheWorkspace_WhenAuthenticationIsEnabledAndTheUserIsAnAdmin() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        enableAuthentication(properties);
        setUser("admin@example.com");

        final StringBuilder buf = new StringBuilder();
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return new WorkspaceMetaData(1);
            }

            @Override
            public boolean deleteWorkspace(long workspaceId) throws WorkspaceComponentException {
                buf.append("1 ");
                return true;
            }
        });

        controller.setSearchComponent(new MockSearchComponent() {
            @Override
            public void delete(long workspaceId) {
                buf.append("2");
            }
        });

        String view = controller.deleteWorkspace(1, model);
        assertEquals("redirect:/dashboard", view);
        assertEquals("1 2", buf.toString());
    }

}