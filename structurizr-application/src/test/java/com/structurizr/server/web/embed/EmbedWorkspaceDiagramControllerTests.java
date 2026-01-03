package com.structurizr.server.web.embed;

import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmbedWorkspaceDiagramControllerTests extends ControllerTestsBase {

    private EmbedWorkspaceDiagramController controller;
    private ModelMap model;

    @BeforeEach
    public void setUp() {
        controller = new EmbedWorkspaceDiagramController();
        model = new ModelMap();
    }

    @Test
    void embedDiagrams_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        disableAuthentication();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.embedDiagrams(1, "viewKey", false, "iframe", false, "perspective", model);
        assertEquals("404", view);
    }

    @Test
    void embedDiagrams_ReturnsThe404Page_WhenTheWorkspaceIsNotPublic() {
        enableAuthentication();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
                workspaceMetaData.setPublicWorkspace(false);

                return workspaceMetaData;
            }
        });

        String view = controller.embedDiagrams(1, "viewKey", false, "iframe", false, "perspective", model);
        assertEquals("404", view);
    }

    @Test
    void embedDiagrams_ReturnsTheDiagramsPage_WhenAuthenticationIsEnabledAndTheWorkspaceIsPublic() {
        enableAuthentication();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
                workspaceMetaData.setPublicWorkspace(true);

                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String view = controller.embedDiagrams(1, "viewKey", false, "iframe", false, "perspective", model);

        assertEquals("diagrams", view);
        assertEquals("/share/1", model.get("urlPrefix"));
    }

    @Test
    void embedDiagrams_ReturnsTheDiagramsPage_WhenAuthenticationIsDisabled() {
        disableAuthentication();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return new WorkspaceMetaData(1);
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String view = controller.embedDiagrams(1, "viewKey", false, "iframe", false, "perspective", model);

        assertEquals("diagrams", view);
        assertEquals("/workspace/1", model.get("urlPrefix"));
    }

    @Test
    void embedDiagramsViaSharingToken_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        enableAuthentication();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.embedDiagramsViaSharingToken(1, "token", "viewKey", false, "iframe", false, "perspective", model);
        assertEquals("404", view);
    }

    @Test
    void embedDiagramsViaSharingToken_ReturnsThe404Page_WhenTheWorkspaceIsNotShareable() {
        enableAuthentication();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
                workspaceMetaData.setPublicWorkspace(false);

                return workspaceMetaData;
            }
        });

        String view = controller.embedDiagramsViaSharingToken(1, "token", "viewKey", false, "iframe", false, "perspective", model);
        assertEquals("404", view);
    }

    @Test
    void embedDiagramsViaSharingToken_ReturnsThe404Page_WhenAuthenticationIsEnabledAndTheSharingTokenIsIncorrect() {
        enableAuthentication();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
                workspaceMetaData.setSharingToken("1234567890");

                return workspaceMetaData;
            }
        });

        String view = controller.embedDiagramsViaSharingToken(1, "0987654321", "viewKey", false, "iframe", false, "perspective", model);
        assertEquals("404", view);
    }

    @Test
    void embedDiagramsViaSharingToken_ReturnsTheDiagramsPage_WhenAuthenticationIsEnabledAndTheSharingTokenIsCorrect() {
        enableAuthentication();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
                workspaceMetaData.setSharingToken("1234567890");

                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String view = controller.embedDiagramsViaSharingToken(1, "1234567890", "viewKey", false, "iframe", false, "perspective", model);

        assertEquals("diagrams", view);
        assertEquals("/share/1/1234567890", model.get("urlPrefix"));
    }

}