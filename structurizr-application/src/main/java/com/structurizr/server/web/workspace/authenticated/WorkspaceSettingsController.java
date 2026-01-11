package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.domain.Permission;
import com.structurizr.server.domain.User;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.Views;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Controller
@Profile("command-server")
public class WorkspaceSettingsController extends AbstractWorkspaceController {

    @RequestMapping(value = "/workspace/{workspaceId}/settings", method = RequestMethod.GET)
    public String showAuthenticatedWorkspaceSettings(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        WorkspaceMetadata workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
        if (workspaceMetadata == null) {
            return show404Page(model);
        }

        User user = getUser();
        Set<Permission> permissions = workspaceMetadata.getPermissions(user);


        if (permissions.contains(Permission.Write)) {
            model.addAttribute("showAdminFeatures", permissions.contains(Permission.Admin));
        } else {
            return show404Page(model);
        }

        return showAuthenticatedView(Views.WORKSPACE_SETTINGS, workspaceId, null, version, model, true, true);
    }

}