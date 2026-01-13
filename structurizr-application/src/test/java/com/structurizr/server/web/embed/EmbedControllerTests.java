package com.structurizr.server.web.embed;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmbedControllerTests extends ControllerTestsBase {

    private EmbedController controller;
    private ModelMap model;

    @BeforeEach
    public void setUp() {
        controller = new EmbedController();
        model = new ModelMap();
    }

    @Test
    void embedDiagrams_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        disableAuthentication();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return null;
            }
        });

        String view = controller.embedDiagrams(1, WorkspaceBranch.MAIN_BRANCH, "viewKey", false, "iframe", false, "perspective", model);
        assertEquals("404", view);
    }

    @Test
    void embedDiagrams_ReturnsThe404Page_WhenTheWorkspaceIsNotPublic() {
        enableAuthentication();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
                workspaceMetaData.setPublicWorkspace(false);

                return workspaceMetaData;
            }
        });

        String view = controller.embedDiagrams(1, WorkspaceBranch.MAIN_BRANCH, "viewKey", false, "iframe", false, "perspective", model);
        assertEquals("404", view);
    }

    @Test
    void embedDiagrams_ReturnsTheBranchesNotEnabledPage_WhenTheBranchesAreNotEnabled() {
        enableAuthentication();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
                workspaceMetaData.setPublicWorkspace(true);

                return workspaceMetaData;
            }
        });

        String view = controller.embedDiagrams(1, "branch", "viewKey", false, "iframe", false, "perspective", model);
        assertEquals("workspace-branches-not-enabled", view);
    }

    @Test
    void embedDiagrams_ReturnsThe404Page_WhenTheBranchDoesNotExist() {
        enableAuthentication();
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
                workspaceMetaData.setPublicWorkspace(true);

                return workspaceMetaData;
            }
        });

        String view = controller.embedDiagrams(1, "branch", "viewKey", false, "iframe", false, "perspective", model);
        assertEquals("404", view);
    }

    @Test
    void embedDiagrams_ReturnsTheDiagramsPage_WhenAuthenticationIsEnabledAndTheWorkspaceIsPublic() {
        enableAuthentication();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
                workspaceMetaData.setPublicWorkspace(true);

                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String view = controller.embedDiagrams(1, WorkspaceBranch.MAIN_BRANCH, "viewKey", false, "iframe", false, "perspective", model);

        assertEquals("diagrams", view);
        assertEquals("/share/1", model.get("urlPrefix"));
        assertEquals("", model.get("urlSuffix"));
    }

    @Test
    void embedDiagrams_ReturnsTheDiagramsPageForABranch_WhenAuthenticationIsEnabledAndTheWorkspaceIsPublic() {
        enableAuthentication();
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
                workspaceMetaData.setPublicWorkspace(true);

                return workspaceMetaData;
            }

            @Override
            public List<WorkspaceBranch> getWorkspaceBranches(long workspaceId) {
                return List.of(new WorkspaceBranch("branch1"), new WorkspaceBranch("branch2"));
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String view = controller.embedDiagrams(1, "branch1", "viewKey", false, "iframe", false, "perspective", model);

        assertEquals("diagrams", view);
        assertEquals("/share/1", model.get("urlPrefix"));
        assertEquals("?branch=branch1", model.get("urlSuffix"));
    }

    @Test
    void embedDiagrams_ReturnsTheDiagramsPage_WhenAuthenticationIsDisabled() {
        disableAuthentication();

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return new WorkspaceMetadata(1);
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String view = controller.embedDiagrams(1, WorkspaceBranch.MAIN_BRANCH, "viewKey", false, "iframe", false, "perspective", model);

        assertEquals("diagrams", view);
        assertEquals("/workspace/1", model.get("urlPrefix"));
        assertEquals("", model.get("urlSuffix"));
    }

}