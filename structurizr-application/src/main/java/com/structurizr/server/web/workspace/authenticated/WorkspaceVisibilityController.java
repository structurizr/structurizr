package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.Configuration;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.User;
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
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Profile("command-server")
public class WorkspaceVisibilityController extends AbstractWorkspaceController {

    private static final Log log = LogFactory.getLog(WorkspaceVisibilityController.class);

    private static final String PUBLIC_ACTION = "public";
    private static final String PRIVATE_ACTION = "private";
    private static final String SHARE_ACTION = "share";
    private static final String UNSHARE_ACTION = "unshare";

    @RequestMapping(value="/workspace/{workspaceId}/visibility", method = RequestMethod.POST)
    public String changeVisibility(
            @PathVariable("workspaceId")long workspaceId,
            @RequestParam("action")String action,
            ModelMap model) {

        if (!Configuration.getInstance().isAuthenticationEnabled()) {
            return showFeatureNotAvailablePage(model);
        }

        try {
            WorkspaceMetaData workspace = workspaceComponent.getWorkspaceMetaData(workspaceId);
            if (workspace != null) {
                User user = getUser();
                if (!Configuration.getInstance().isAuthenticationEnabled() || Configuration.getInstance().getAdminUsersAndRoles().isEmpty() || user.isAdmin()) {
                    switch (action.toLowerCase()) {
                        case PUBLIC_ACTION:
                            workspaceComponent.makeWorkspacePublic(workspaceId);
                            break;
                        case PRIVATE_ACTION:
                            workspaceComponent.makeWorkspacePrivate(workspaceId);
                            break;
                        case SHARE_ACTION:
                            workspaceComponent.shareWorkspace(workspaceId);
                            break;
                        case UNSHARE_ACTION:
                            workspaceComponent.unshareWorkspace(workspaceId);
                            break;
                        default:
                            return show404Page(model);
                    }
                }
            } else {
                return show404Page(model);
            }
        } catch (WorkspaceComponentException e) {
            log.error(e);
        }

        return "redirect:/workspace/" + workspaceId + "/settings";
    }

}