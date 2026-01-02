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
class TreeController extends AbstractWorkspaceController {

    @RequestMapping(value = "/workspace/{workspaceId}/explore/tree", method = RequestMethod.GET)
    public String showAuthenticatedTree(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String view,
            ModelMap model
    ) {
        model.addAttribute("view", view);

        return showAuthenticatedView(Views.TREE, workspaceId, branch, version, model, true, false);
    }

}