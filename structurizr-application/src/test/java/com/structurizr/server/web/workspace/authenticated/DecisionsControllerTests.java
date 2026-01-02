package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class DecisionsControllerTests extends ControllerTestsBase {

    private DecisionsController controller;
    private ModelMap model;

    @BeforeEach
    void setUp() {
        controller = new DecisionsController();
        model = new ModelMap();
    }

    @Test
    void showAuthenticatedDecisions_ReturnsTheDecisionsPageForSoftwareSystem()  {
        disableAuthentication();

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
        });

        String view = controller.showAuthenticatedDecisions(1, "main", "version", "SoftwareSystem", model);
        assertEquals("decisions", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("U29mdHdhcmVTeXN0ZW0=", model.getAttribute("scope"));
        assertEquals("SoftwareSystem", new String(Base64.getDecoder().decode((String)model.getAttribute("scope"))));
    }

    @Test
    void showAuthenticatedDecisions_ReturnsTheDecisionsPageForContainer()  {
        disableAuthentication();

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
        });

        String view = controller.showAuthenticatedDecisions(1, "main", "version", "SoftwareSystem", "Container", model);
        assertEquals("decisions", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("U29mdHdhcmVTeXN0ZW0vQ29udGFpbmVy", model.getAttribute("scope"));
        assertEquals("SoftwareSystem/Container", new String(Base64.getDecoder().decode((String)model.getAttribute("scope"))));
    }

    @Test
    void showAuthenticatedDecisions_ReturnsTheDecisionsPageForComponent()  {
        disableAuthentication();

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
        });

        String view = controller.showAuthenticatedDecisions(1, "main", "version", "SoftwareSystem", "Container", "Component", model);
        assertEquals("decisions", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("U29mdHdhcmVTeXN0ZW0vQ29udGFpbmVyL0NvbXBvbmVudA==", model.getAttribute("scope"));
        assertEquals("SoftwareSystem/Container/Component", new String(Base64.getDecoder().decode((String)model.getAttribute("scope"))));
    }

    @Test
    void showAuthenticatedDecisions_ReturnsTheDecisionsPageForComponent_WhenRunningInLocalMode() throws Exception  {
        configureAsLocal();

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
            public long getLastModifiedDate() {
                return 1234567890;
            }
        });

        String view = controller.showAuthenticatedDecisions(1, "main", "version", "SoftwareSystem", "Container", "Component", model);
        assertEquals("decisions", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("U29mdHdhcmVTeXN0ZW0vQ29udGFpbmVyL0NvbXBvbmVudA==", model.getAttribute("scope"));
        assertEquals("SoftwareSystem/Container/Component", new String(Base64.getDecoder().decode((String)model.getAttribute("scope"))));

        assertEquals(12345, model.getAttribute("autoRefreshInterval"));
        assertEquals(1234567890L, model.getAttribute("autoRefreshLastModifiedDate"));
    }

}