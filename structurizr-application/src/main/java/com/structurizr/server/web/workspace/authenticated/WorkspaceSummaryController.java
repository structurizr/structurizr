package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.Views;
import com.structurizr.util.StringUtils;
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
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        if (WorkspaceBranch.isMainBranch(branch)) {
            branch = "";
        }

        if (Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_BRANCHES)) {
            model.addAttribute("branchesEnabled", true);
            model.addAttribute("branch", branch);
            model.addAttribute("branches", workspaceComponent.getWorkspaceBranches(workspaceId));
        }

        if (!StringUtils.isNullOrEmpty(branch) && !Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_BRANCHES)) {
            return showError("workspace-branches-not-enabled", model);
        }

        model.addAttribute("versions", workspaceComponent.getWorkspaceVersions(workspaceId, branch));

        boolean editable = workspaceMetaData.hasNoUsersConfigured() || workspaceMetaData.isWriteUser(getUser());

        return showAuthenticatedView(Views.WORKSPACE_SUMMARY, workspaceMetaData, branch, version, model, true, editable);
    }

}