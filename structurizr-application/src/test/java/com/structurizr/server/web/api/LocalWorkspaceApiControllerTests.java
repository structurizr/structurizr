package com.structurizr.server.web.api;

import com.structurizr.Workspace;
import com.structurizr.api.HttpHeaders;
import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.ControllerTestsBase;
import com.structurizr.server.web.MockHttpServletRequest;
import com.structurizr.server.web.MockHttpServletResponse;
import com.structurizr.server.web.MockWorkspaceComponent;
import com.structurizr.util.Md5Digest;
import com.structurizr.util.WorkspaceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Base64;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class LocalWorkspaceApiControllerTests extends ControllerTestsBase {

    private LocalWorkspaceApiController controller;
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @BeforeEach
    void setUp() {
        controller = new LocalWorkspaceApiController();
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {});

        configureAsLocal();
    }

    @Test
    void getWorkspace_ReturnsAnApiError_WhenANegativeWorkspaceIdIsSpecified() {
        try {
            controller.getWorkspace(-1, request, response);
            fail();
        } catch (ApiException e) {
            assertEquals("Workspace ID must be greater than 1", e.getMessage());
        }
    }

    @Test
    void getWorkspace_ReturnsTheWorkspace() {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(1);

                return wmd;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) throws WorkspaceComponentException {
                return "json";
            }
        });

        String json = controller.getWorkspace(1, request, response);
        assertEquals("json", json);
    }

    @Test
    void putWorkspace_ReturnsAnApiError_WhenANegativeWorkspaceIdIsSpecified() {
        try {
            controller.putWorkspace(-1, "json", request, response);
            fail();
        } catch (ApiException e) {
            assertEquals("Workspace ID must be greater than 1", e.getMessage());
        }
    }

    @Test
    void putWorkspace_PutsTheWorkspace() throws Exception {
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                wmd.setApiKey("1234567890");

                return wmd;
            }
        });

        Workspace workspace = new Workspace("Name", "Description");
        String json = WorkspaceUtils.toJson(workspace, false);

        request.addHeader(HttpHeaders.AUTHORIZATION, "1234567890");

        controller.putWorkspace(1, json, request, response);
    }

}