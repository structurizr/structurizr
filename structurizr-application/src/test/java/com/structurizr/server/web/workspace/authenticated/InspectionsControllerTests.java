package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.Workspace;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockWorkspaceComponent;
import com.structurizr.util.WorkspaceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class InspectionsControllerTests extends ControllerTestsBase {

    private InspectionsController controller;
    private ModelMap model;

    @BeforeEach
    void setUp() {
        controller = new InspectionsController();
        model = new ModelMap();
    }

    @Test
    void showAuthenticatedInspections() throws Exception {
        disableAuthentication();

        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        Workspace workspace = new Workspace("Name", "Description");
        workspace.getModel().addSoftwareSystem("Software System");
        String json = WorkspaceUtils.toJson(workspace, false);

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return json;
            }
        });

        String view = controller.showAuthenticatedInspections(1, "main", "version", model);
        assertEquals("inspections", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("eyJjb25maWd1cmF0aW9uIjp7fSwiZGVzY3JpcHRpb24iOiJEZXNjcmlwdGlvbiIsImRvY3VtZW50YXRpb24iOnt9LCJpZCI6MCwibW9kZWwiOnsic29mdHdhcmVTeXN0ZW1zIjpbeyJkb2N1bWVudGF0aW9uIjp7fSwiaWQiOiIxIiwibmFtZSI6IlNvZnR3YXJlIFN5c3RlbSIsInRhZ3MiOiJFbGVtZW50LFNvZnR3YXJlIFN5c3RlbSJ9XX0sIm5hbWUiOiJOYW1lIiwidmlld3MiOnsiY29uZmlndXJhdGlvbiI6eyJicmFuZGluZyI6e30sInN0eWxlcyI6e30sInRlcm1pbm9sb2d5Ijp7fX19fQ==", model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));

        assertEquals(16, model.getAttribute("numberOfInspections"));
        assertEquals(5, model.getAttribute("numberOfViolations"));
        assertEquals(5, model.getAttribute("numberOfErrors"));
        assertEquals(0, model.getAttribute("numberOfInfos"));
        assertEquals(0, model.getAttribute("numberOfWarnings"));
        assertEquals(0, model.getAttribute("numberOfIgnores"));
    }

}