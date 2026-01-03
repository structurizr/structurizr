package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.util.WorkspaceUtils;
import com.structurizr.view.ThemeUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@org.springframework.context.annotation.Profile("command-server")
public class ThemeController extends AbstractWorkspaceController {

    @RequestMapping(value = "/workspace/{workspaceId}/theme", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    String showAuthenticatedTheme(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            HttpServletResponse response
    ) throws Exception {

        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData != null && workspaceMetaData.hasAccess(getUser())) {
            String workspaceAsJson = workspaceComponent.getWorkspace(workspaceId, branch, version);
            return ThemeUtils.toJson(WorkspaceUtils.fromJson(workspaceAsJson));
        } else {
            response.setStatus(404);
            return null;
        }
    }

}