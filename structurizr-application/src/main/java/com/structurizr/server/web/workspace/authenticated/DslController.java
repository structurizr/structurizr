package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.Workspace;
import com.structurizr.dsl.DslUtils;
import com.structurizr.server.domain.Permission;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.util.WorkspaceUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Controller
@Profile("command-server")
public class DslController extends AbstractWorkspaceController {

    @RequestMapping(value = "/workspace/{workspaceId}/dsl", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String showAuthenticatedDsl(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            HttpServletResponse response
    ) throws Exception {

        WorkspaceMetadata workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
        if (workspaceMetadata == null) {
            response.setStatus(404);
            return null;
        }

        Set<Permission> permissions = workspaceMetadata.getPermissions(getUser());
        if (!permissions.isEmpty()) {
            String workspaceAsJson = workspaceComponent.getWorkspace(workspaceId, branch, version);
            Workspace workspace = WorkspaceUtils.fromJson(workspaceAsJson);
            return DslUtils.getDsl(workspace);
        } else {
            response.setStatus(404);
            return null;
        }
    }

}
