package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.component.workspace.WorkspaceVersion;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WorkspaceSummaryControllerTests extends ControllerTestsBase {

    private WorkspaceSummaryController controller;
    private ModelMap model;

    @BeforeEach
    void setUp() {
        controller = new WorkspaceSummaryController();
        model = new ModelMap();
    }

    @Test
    void showAuthenticatedWorkspaceSummary_WhenBranchesAreNotEnabled()  {
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

            @Override
            public List<WorkspaceVersion> getWorkspaceVersions(long workspaceId, String branch) {
                return List.of(
                        new WorkspaceVersion("3", new Date(3)),
                        new WorkspaceVersion("2", new Date(2)),
                        new WorkspaceVersion("1", new Date(1))
                );
            }
        });

        String view = controller.showAuthenticatedWorkspaceSummary(1, "", "", model);
        assertEquals("workspace-summary", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));

        List<WorkspaceVersion> versions = (List<WorkspaceVersion>)model.getAttribute("versions");
        assertEquals(3, versions.size());
        assertEquals("3", versions.get(0).getVersionId());
        assertEquals("2", versions.get(1).getVersionId());
        assertEquals("1", versions.get(2).getVersionId());
    }

    @Test
    void showAuthenticatedWorkspaceSummary_WhenBranchesAreEnabled()  {
        disableAuthentication();
        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_BRANCHES);

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
            public List<WorkspaceBranch> getWorkspaceBranches(long workspaceId) {
                return List.of(
                        new WorkspaceBranch("branch1"),
                        new WorkspaceBranch("branch2"),
                        new WorkspaceBranch("branch3")
                );
            }
        });

        String view = controller.showAuthenticatedWorkspaceSummary(1, "branch1", "", model);
        assertEquals("workspace-summary", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));

        assertEquals(true, model.getAttribute("branchesEnabled"));
        assertEquals("branch1", model.getAttribute("branch"));

        List<WorkspaceVersion> versions = (List<WorkspaceVersion>)model.getAttribute("versions");
        assertEquals(0, versions.size());

        List<WorkspaceBranch> branches = (List<WorkspaceBranch>)model.getAttribute("branches");
        assertEquals(3, branches.size());
        assertEquals("branch1", branches.get(0).getName());
        assertEquals("branch2", branches.get(1).getName());
        assertEquals("branch3", branches.get(2).getName());
    }

    @Test
    void showAuthenticatedWorkspaceSummary_WhenRunningInLocalMode()  {
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
        });

        String view = controller.showAuthenticatedWorkspaceSummary(1, "", "", model);
        assertEquals("workspace-summary", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertNull(model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertNull(model.getAttribute("versions"));
    }

}