package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.configuration.Profile;
import com.structurizr.server.web.Views;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class WorkspaceSummaryController extends AbstractWorkspaceController {

    @RequestMapping(value = "/workspace/{workspaceId}", method = RequestMethod.GET)
    public String showAuthenticatedWorkspaceSummary(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false, defaultValue = "") String branch,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        return showAuthenticatedView(
                Views.WORKSPACE_SUMMARY, workspaceId,
                workspaceMetaData -> {
                    if (Configuration.getInstance().getProfile() == Profile.Server) {
                        if (Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_BRANCHES)) {
                            model.addAttribute("branchesEnabled", true);
                            model.addAttribute("branch", branch);
                            model.addAttribute("branches", workspaceComponent.getWorkspaceBranches(workspaceId));
                        }

                        model.addAttribute("versions", workspaceComponent.getWorkspaceVersions(workspaceId, workspaceMetaData.getBranch()));
                    }
                },
                branch, version, model, true, true
        );
    }

}