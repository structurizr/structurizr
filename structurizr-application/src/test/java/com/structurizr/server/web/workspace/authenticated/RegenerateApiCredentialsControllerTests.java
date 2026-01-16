package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class RegenerateApiCredentialsControllerTests extends ControllerTestsBase {

    private RegenerateApiCredentialsController controller;
    private ModelMap model;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        controller = new RegenerateApiCredentialsController();
        model = new ModelMap();
        redirectAttributes = new RedirectAttributesModelMap();
    }

    @Test
    void regenerateApiCredentials_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        enableAuthentication();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return null;
            }
        });

        String view = controller.regenerateApiCredentials(1, model, redirectAttributes);
        assertEquals("404", view);
    }

    @Test
    void regenerateApiCredentials_DoesNothingAndRedirectsToTheWorkspaceSettingsPage_WhenAuthenticationIsEnabledAndTheUserDoesNotHaveAdminPermission() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        enableAuthentication(properties);
        setUser("user@example.com");

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.addWriteUser("user@example.com");
        workspaceMetaData.setApiKey("key");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void putWorkspaceMetadata(WorkspaceMetadata workspaceMetaData) {
                fail();
            }
        });

        String view = controller.regenerateApiCredentials(1, model, redirectAttributes);
        assertEquals("redirect:/workspace/1/settings", view);
        assertEquals("key", workspaceMetaData.getApiKey());
    }

    @Test
    void regenerateApiCredentials_RegeneratesApiCredentials_WhenAuthenticationIsEnabledAndTheUserHasAdminPermission() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        enableAuthentication(properties);
        setUser("admin@example.com");

        final WorkspaceMetadata workspaceMetadata = new WorkspaceMetadata(1);
        workspaceMetadata.setApiKey("key");

        StringBuilder buf = new StringBuilder();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }

            @Override
            public void putWorkspaceMetadata(WorkspaceMetadata wmd) {
                buf.append(wmd.getId() + "|" + wmd.getApiKey());
            }
        });

        String view = controller.regenerateApiCredentials(1, model, redirectAttributes);
        assertEquals("redirect:/workspace/1/settings", view);
        assertNotEquals("key", workspaceMetadata.getApiKey());
        assertEquals("1|" + workspaceMetadata.getApiKey(), buf.toString());
    }

}