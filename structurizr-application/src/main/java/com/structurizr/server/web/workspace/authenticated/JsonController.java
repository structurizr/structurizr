package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.domain.WorkspaceMetaData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

@Controller
public class JsonController extends AbstractWorkspaceController {

    @RequestMapping(value = "/workspace/{workspaceId}/json", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String showAuthenticatedDsl(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false, defaultValue = "") String branch,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        if (workspaceMetaData.hasAccess(getUser())) {
            return workspaceComponent.getWorkspace(workspaceId, branch, version);
        } else {
            return show404Page(model);
        }
    }

}