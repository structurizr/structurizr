package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.domain.Permission;
import com.structurizr.server.domain.WorkspaceMetadata;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Controller
public class JsonController extends AbstractWorkspaceController {

    @RequestMapping(value = "/workspace/{workspaceId}/json", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    String showAuthenticatedJson(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            HttpServletResponse response
    ) {

        WorkspaceMetadata workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
        if (workspaceMetadata == null) {
            response.setStatus(404);
            return null;
        }

        Set<Permission> permissions = workspaceMetadata.getPermissions(getUser());
        if (!permissions.isEmpty()) {
            return workspaceComponent.getWorkspace(workspaceId, branch, version);
        } else {
            response.setStatus(404);
            return null;
        }
    }

}