package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import static org.junit.jupiter.api.Assertions.*;

public class DslEditorControllerTests extends ControllerTestsBase {

    private DslEditorController controller;
    private ModelMap model;

    @BeforeEach
    public void setUp() {
        controller = new DslEditorController();
        model = new ModelMap();
    }

    @Test
    void showAuthenticatedDslEditor_ReturnsAnErrorPage_WhenTheDslEditorHasBeenDisabled() {
        enableAuthentication();
        Configuration.getInstance().setFeatureEnabled(Features.UI_DSL_EDITOR);
        setUser("user@example.com");

        Configuration.getInstance().setFeatureDisabled(Features.UI_DSL_EDITOR);
        String view = controller.showAuthenticatedDslEditor(1, "", "version", model);
        assertEquals("dsl-editor-disabled", view);
    }

    @Test
    void showAuthenticatedDslEditor_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        enableAuthentication();
        Configuration.getInstance().setFeatureEnabled(Features.UI_DSL_EDITOR);
        setUser("user@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.showAuthenticatedDslEditor(1, "", "version", model);
        assertEquals("404", view);
    }

    @Test
    void showAuthenticatedDslEditor_ReturnsAnErrorPage_WhenTheWorkspaceIsClientSideEncrypted() {
        enableAuthentication();
        Configuration.getInstance().setFeatureEnabled(Features.UI_DSL_EDITOR);
        setUser("user@example.com");

        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setClientSideEncrypted(true);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        String view = controller.showAuthenticatedDslEditor(1, "", "version", model);
        assertEquals("workspace-is-client-side-encrypted", view);
    }

    @Test
    void showAuthenticatedDslEditor_ReturnsThe404Page_WhenTheUserDoesNotHaveAccess() {
        enableAuthentication();
        Configuration.getInstance().setFeatureEnabled(Features.UI_DSL_EDITOR);
        setUser("user@example.com");

        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user2@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        setUser("user1@example.com");
        String view = controller.showAuthenticatedDslEditor(1, "", "version", model);
        assertEquals("404", view);
    }

    @Test
    void showAuthenticatedDslEditor_ReturnsTheDslEditorPage_WhenAuthenticationIsDisabled()  {
        disableAuthentication();
        Configuration.getInstance().setFeatureEnabled(Features.UI_DSL_EDITOR);

        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }

            @Override
            public boolean lockWorkspace(long workspaceId, String username, String agent) {
                workspaceMetaData.addLock(username, agent);
                return true;
            }
        });

        String view = controller.showAuthenticatedDslEditor(1, "", "version", model);
        assertEquals("dsl-editor", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/images/", model.getAttribute("thumbnailUrl"));
        assertTrue(workspaceMetaData.isLocked());
        assertTrue(workspaceMetaData.getLockedUser().matches("[0-9]*"));
        assertTrue(workspaceMetaData.getLockedAgent().startsWith("structurizr/dsl-editor/"));
    }

    @Test
    void showAuthenticatedDslEditor_ReturnsTheDslEditorPage_WhenTheWorkspaceHasNoUsersConfigured()  {
        enableAuthentication();
        Configuration.getInstance().setFeatureEnabled(Features.UI_DSL_EDITOR);
        setUser("user@example.com");

        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }

            @Override
            public boolean lockWorkspace(long workspaceId, String username, String agent) {
                workspaceMetaData.addLock(username, agent);
                return true;
            }
        });

        String view = controller.showAuthenticatedDslEditor(1, "", "version", model);
        assertEquals("dsl-editor", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/images/", model.getAttribute("thumbnailUrl"));
        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user@example.com", workspaceMetaData.getLockedUser());
        assertTrue(workspaceMetaData.getLockedAgent().startsWith("structurizr/dsl-editor/"));
    }

    @Test
    public void showAuthenticatedDslEditor_ReturnsTheDslEditorPage_WhenTheUserHasWriteAccess()  {
        enableAuthentication();
        Configuration.getInstance().setFeatureEnabled(Features.UI_DSL_EDITOR);
        setUser("user@example.com");

        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user1@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }

            @Override
            public boolean lockWorkspace(long workspaceId, String username, String agent) {
                workspaceMetaData.addLock(username, agent);
                return true;
            }
        });

        setUser("user1@example.com");
        String view = controller.showAuthenticatedDslEditor(1, "", "version", model);
        assertEquals("dsl-editor", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertTrue(workspaceMetaData.isEditable());
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("/workspace/1/images/", model.getAttribute("thumbnailUrl"));
        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user1@example.com", workspaceMetaData.getLockedUser());
        assertTrue(workspaceMetaData.getLockedAgent().startsWith("structurizr/dsl-editor/"));
    }

    @Test
    public void showAuthenticatedDslEditor_ReturnsAnErrorPage_WhenTheUserHasReadAccess()  {
        enableAuthentication();
        Configuration.getInstance().setFeatureEnabled(Features.UI_DSL_EDITOR);
        setUser("user@example.com");

        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addReadUser("user1@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        setUser("user1@example.com");
        String view = controller.showAuthenticatedDslEditor(1, "", "version", model);
        assertEquals("workspace-is-readonly", view);
    }

}