package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class DocumentationControllerTests extends ControllerTestsBase {

    private DocumentationController controller;
    private ModelMap model;

    @BeforeEach
    void setUp() {
        controller = new DocumentationController();
        model = new ModelMap();
    }

    @Test
    void showAuthenticatedDocumentation_ReturnsTheDocumentationPageForSoftwareSystem()  {
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

        String view = controller.showAuthenticatedDocumentation(1, "main", "version", "SoftwareSystem", model);
        assertEquals("documentation", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("U29mdHdhcmVTeXN0ZW0=", model.getAttribute("scope"));
        assertEquals("SoftwareSystem", new String(Base64.getDecoder().decode((String)model.getAttribute("scope"))));
    }

    @Test
    void showAuthenticatedDocumentation_ReturnsTheDocumentationPageForContainer()  {
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

        String view = controller.showAuthenticatedDocumentation(1, "main", "version", "SoftwareSystem", "Container", model);
        assertEquals("documentation", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("U29mdHdhcmVTeXN0ZW0vQ29udGFpbmVy", model.getAttribute("scope"));
        assertEquals("SoftwareSystem/Container", new String(Base64.getDecoder().decode((String)model.getAttribute("scope"))));
    }

    @Test
    void showAuthenticatedDocumentation_ReturnsTheDocumentationPageForComponent()  {
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

        String view = controller.showAuthenticatedDocumentation(1, "main", "version", "SoftwareSystem", "Container", "Component", model);
        assertEquals("documentation", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("U29mdHdhcmVTeXN0ZW0vQ29udGFpbmVyL0NvbXBvbmVudA==", model.getAttribute("scope"));
        assertEquals("SoftwareSystem/Container/Component", new String(Base64.getDecoder().decode((String)model.getAttribute("scope"))));
    }

    @Test
    void showAuthenticatedDocumentation_ReturnsTheDocumentationPageForComponent_WhenRunningInLocalMode() throws Exception  {
        configureAsLocal();

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

            @Override
            public long getLastModifiedDate() {
                return 1234567890;
            }
        });

        String view = controller.showAuthenticatedDocumentation(1, "main", "version", "SoftwareSystem", "Container", "Component", model);
        assertEquals("documentation", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertEquals("U29mdHdhcmVTeXN0ZW0vQ29udGFpbmVyL0NvbXBvbmVudA==", model.getAttribute("scope"));
        assertEquals("SoftwareSystem/Container/Component", new String(Base64.getDecoder().decode((String)model.getAttribute("scope"))));

        assertEquals(12345, model.getAttribute("autoRefreshInterval"));
        assertEquals(1234567890L, model.getAttribute("autoRefreshLastModifiedDate"));
    }

}