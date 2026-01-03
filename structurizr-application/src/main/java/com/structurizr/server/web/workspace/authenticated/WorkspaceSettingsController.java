package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.Configuration;
import com.structurizr.server.web.Views;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Profile("command-server")
public class WorkspaceSettingsController extends AbstractWorkspaceController {

    @RequestMapping(value = "/workspace/{workspaceId}/settings", method = RequestMethod.GET)
    public String showAuthenticatedWorkspaceSettings(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        model.addAttribute("showAdminFeatures", !Configuration.getInstance().isAuthenticationEnabled() || Configuration.getInstance().getAdminUsersAndRoles().isEmpty() || getUser().isAdmin());
        return showAuthenticatedView(Views.WORKSPACE_SETTINGS, workspaceId, null, version, model, true, true);
    }

}