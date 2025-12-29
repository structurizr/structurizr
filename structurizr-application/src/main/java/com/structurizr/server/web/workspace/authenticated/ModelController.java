package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.Views;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class ModelController extends AbstractWorkspaceController {

    @RequestMapping(value = "/workspace/{workspaceId}/explore/model", method = RequestMethod.GET)
    public String showAuthenticatedModel(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        return showAuthenticatedView(Views.MODEL, workspaceMetaData, branch, version, model, true, false);
    }

}