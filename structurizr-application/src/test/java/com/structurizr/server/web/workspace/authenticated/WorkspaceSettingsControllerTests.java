package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class WorkspaceSettingsControllerTests extends ControllerTestsBase {

    private WorkspaceSettingsController controller;
    private ModelMap model;

    @BeforeEach
    void setUp() {
        controller = new WorkspaceSettingsController();
        model = new ModelMap();
    }

    @Test
    void showAuthenticatedWorkspaceSettings_ReturnsTheWorkspaceSettingsPage_WhenAuthenticationIsEnabledAndNoAdminUsersAreDefined()  {
        enableAuthentication();
        setUser("user@example.com");

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String view = controller.showAuthenticatedWorkspaceSettings(1, "version", model);
        assertEquals("workspace-settings", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals(true, model.getAttribute("showAdminFeatures"));
    }

    @Test
    void showAuthenticatedWorkspaceSettings_ReturnsTheWorkspaceSettingsPage_WhenAuthenticationIsEnabledAndTheUserIsAnAdmin()  {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        enableAuthentication(properties);
        setUser("admin@example.com");

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String view = controller.showAuthenticatedWorkspaceSettings(1, "version", model);
        assertEquals("workspace-settings", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals(true, model.getAttribute("showAdminFeatures"));
    }

    @Test
    void showAuthenticatedWorkspaceSettings_ReturnsTheWorkspaceSettingsPage_WhenAuthenticationIsEnabledAndTheUserIsNotAnAdmin()  {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        enableAuthentication(properties);
        setUser("user@example.com");

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String view = controller.showAuthenticatedWorkspaceSettings(1, "version", model);
        assertEquals("workspace-settings", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals(false, model.getAttribute("showAdminFeatures"));
    }

    @Test
    void showAuthenticatedWorkspaceSettings_ReturnsTheWorkspaceSettingsPage_WhenAuthenticationIsDisabled()  {
        disableAuthentication();

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String view = controller.showAuthenticatedWorkspaceSettings(1, "version", model);
        assertEquals("workspace-settings", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals(true, model.getAttribute("showAdminFeatures"));
    }

    @Test
    void showAuthenticatedWorkspaceSettings_ReturnsThe404Page_WhenAuthenticationIsEnabledAndTheUserHasReadAccess()  {
        enableAuthentication();
        setUser("read@example.com");

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.addWriteUser("write@example.com");
        workspaceMetaData.addReadUser("read@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }
        });

        String view = controller.showAuthenticatedWorkspaceSettings(1, "version", model);
        assertEquals("404", view);
    }

}