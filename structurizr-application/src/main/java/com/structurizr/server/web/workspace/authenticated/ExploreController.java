package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.web.Views;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class ExploreController extends AbstractWorkspaceController {

    @RequestMapping(value = "/workspace/{workspaceId}/explore", method = RequestMethod.GET)
    public String showAuthenticatedExplorePage(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        return showAuthenticatedView(Views.EXPLORE, workspaceId, branch, version, model, true, false);
    }

}