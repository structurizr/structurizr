package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeleteBranchControllerTests extends ControllerTestsBase {

    private DeleteBranchController controller;
    private ModelMap model;

    @BeforeEach
    void setUp() {
        controller = new DeleteBranchController();
        model = new ModelMap();
    }

    @Test
    void deleteBranch_ReturnsThe404Page_WhenTheWorkspaceBranchesAreNotEnabled() {
        disableAuthentication();
        Configuration.getInstance().setFeatureDisabled(Features.WORKSPACE_BRANCHES);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {});

        String view = controller.deleteBranch(1, "branch", model);
        assertEquals("404", view);
    }

    @Test
    void deleteBranch_ReturnsThe404Page_WhenTryingToDeleteTheMainBranch() {
        disableAuthentication();
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {});

        String view = controller.deleteBranch(1, "main", model);
        assertEquals("404", view);

        view = controller.deleteBranch(1, "", model);
        assertEquals("404", view);
    }

    @Test
    void deleteBranch_ReturnsThe404Page_WhenTheWorkspaceDoesNotExist() {
        disableAuthentication();
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return null;
            }
        });

        String view = controller.deleteBranch(1, "branch", model);
        assertEquals("404", view);
    }

    @Test
    void deleteBranch_DeletesTheBranch_WhenAuthenticationIsDisabled() {
        disableAuthentication();
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

        final StringBuilder buf = new StringBuilder();
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return new WorkspaceMetaData(1);
            }

            @Override
            public boolean deleteBranch(long workspaceId, String branch) {
                buf.append(workspaceId + ":" + branch);
                return true;
            }
        });

        String view = controller.deleteBranch(1, "branch", model);
        assertEquals("redirect:/workspace/1", view);
        assertEquals("1:branch", buf.toString());
    }

    @Test
    void deleteBranch_DeletesTheBranch_WhenAuthenticationIsEnabledAndNoUsersAreConfigured() {
        enableAuthentication();
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);
        setUser("user@example.com");

        final StringBuilder buf = new StringBuilder();
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return new WorkspaceMetaData(1);
            }

            @Override
            public boolean deleteBranch(long workspaceId, String branch) {
                buf.append(workspaceId + ":" + branch);
                return true;
            }
        });

        String view = controller.deleteBranch(1, "branch", model);
        assertEquals("redirect:/workspace/1", view);
        assertEquals("1:branch", buf.toString());
    }

    @Test
    void deleteBranch_DeletesTheBranch_WhenAuthenticationIsEnabledAndTheUserHasWriteAccess() {
        enableAuthentication();
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);
        setUser("user@example.com");

        final StringBuilder buf = new StringBuilder();
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
                workspaceMetaData.addWriteUser("user@example.com");

                return workspaceMetaData;
            }

            @Override
            public boolean deleteBranch(long workspaceId, String branch) {
                buf.append(workspaceId + ":" + branch);
                return true;
            }
        });

        String view = controller.deleteBranch(1, "branch", model);
        assertEquals("redirect:/workspace/1", view);
        assertEquals("1:branch", buf.toString());
    }

    @Test
    void deleteBranch_ReturnsThe404Page_WhenAuthenticationIsEnabledAndTheUserHasReadAccess() {
        enableAuthentication();
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);
        setUser("user@example.com");

        final StringBuilder buf = new StringBuilder();
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
                workspaceMetaData.addReadUser("user@example.com");

                return workspaceMetaData;
            }

            @Override
            public boolean deleteBranch(long workspaceId, String branch) {
                buf.append(workspaceId + ":" + branch);
                return true;
            }
        });

        String view = controller.deleteBranch(1, "branch", model);
        assertEquals("404", view);
        assertEquals("", buf.toString());
    }

    @Test
    void deleteBranch_ReturnsThe404Page_WhenAuthenticationIsEnabledAndTheUserDoesNotHaveAccess() {
        enableAuthentication();
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);
        setUser("user2@example.com");

        final StringBuilder buf = new StringBuilder();
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
                workspaceMetaData.addReadUser("user1@example.com");

                return workspaceMetaData;
            }

            @Override
            public boolean deleteBranch(long workspaceId, String branch) {
                buf.append(workspaceId + ":" + branch);
                return true;
            }
        });

        String view = controller.deleteBranch(1, "branch", model);
        assertEquals("404", view);
        assertEquals("", buf.toString());
    }

}