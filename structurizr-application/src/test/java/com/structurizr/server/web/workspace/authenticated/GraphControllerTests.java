package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockWorkspaceComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class GraphControllerTests extends ControllerTestsBase {

    private GraphController controller;
    private ModelMap model;

    @BeforeEach
    void setUp() {
        controller = new GraphController();
        model = new ModelMap();
    }

    @Test
    void showAuthenticatedGraph()  {
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

        String view = controller.showAuthenticatedGraph(1, "main", "version", "view", model);
        assertEquals("graph", view);
        assertSame(workspaceMetaData, model.getAttribute("workspace"));
        assertEquals("anNvbg==", model.getAttribute("workspaceAsJson"));
        assertEquals("/workspace/1", model.getAttribute("urlPrefix"));
        assertSame("view", model.getAttribute("view"));
    }

}