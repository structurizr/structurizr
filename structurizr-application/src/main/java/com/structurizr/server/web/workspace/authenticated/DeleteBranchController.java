package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.workspace.AbstractWorkspaceController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@Profile("command-server")
public class DeleteBranchController extends AbstractWorkspaceController {

    private static final Log log = LogFactory.getLog(DeleteBranchController.class);

    @RequestMapping(value="/workspace/{workspaceId}/branch/{branch}/delete", method = RequestMethod.POST)
    String deleteBranch(@PathVariable("workspaceId")long workspaceId,
                                  @PathVariable("branch")String branch,
                                  ModelMap model) {

        if (!Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_BRANCHES)) {
            return show404Page(model);
        }

        if (WorkspaceBranch.isMainBranch(branch)) {
            return show404Page(model);
        }

        try {
            WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
            if (workspaceMetaData != null) {
                if (!Configuration.getInstance().isAuthenticationEnabled() || workspaceMetaData.hasNoUsersConfigured() || workspaceMetaData.isWriteUser(getUser())) {
                    workspaceComponent.deleteBranch(workspaceId, branch);
                    return "redirect:/workspace/" + workspaceId;
                }
            }
        } catch (WorkspaceComponentException e) {
            log.error(e);
        }

        return show404Page(model);
    }

}