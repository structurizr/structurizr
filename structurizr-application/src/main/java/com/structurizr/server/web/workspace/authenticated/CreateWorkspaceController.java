package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.Configuration;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.User;
import com.structurizr.server.web.workspace.AbstractWorkspaceController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@Profile("command-server")
public class CreateWorkspaceController extends AbstractWorkspaceController {

    private static final Log log = LogFactory.getLog(CreateWorkspaceController.class);

    @RequestMapping(value = "/workspace/create", method = RequestMethod.GET)
    String createWorkspace(ModelMap model) {
        try {
            User user = getUser();

            if (!Configuration.getInstance().isAuthenticationEnabled() || !Configuration.getInstance().adminUsersEnabled() || user.isAdmin()) {
                long workspaceId = workspaceComponent.createWorkspace(user);
                return "redirect:/workspace/" + workspaceId;
            } else {
                return show404Page(model);
            }
        } catch (WorkspaceComponentException e) {
            log.error(e);
        }

        return "redirect:/";
    }

}