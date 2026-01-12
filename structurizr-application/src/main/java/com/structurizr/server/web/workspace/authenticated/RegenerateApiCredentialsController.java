package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.domain.Permission;
import com.structurizr.server.domain.User;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.workspace.AbstractWorkspaceController;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Set;
import java.util.UUID;

@Controller
@Profile("command-server")
public class RegenerateApiCredentialsController extends AbstractWorkspaceController {

    @RequestMapping(value="/workspace/{workspaceId}/settings/regenerate-api-credentials", method = RequestMethod.POST)
    public String regenerateApiCredentials(
            @PathVariable("workspaceId")long workspaceId,
            ModelMap model) {

        WorkspaceMetadata workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
        if (workspaceMetadata == null) {
            return show404Page(model);
        }

        User user = getUser();
        Set<Permission> permissions = workspaceMetadata.getPermissions(user);

        if (permissions.contains(Permission.Admin)) {
            workspaceMetadata.setApiKey(UUID.randomUUID().toString());
            workspaceMetadata.setApiSecret(UUID.randomUUID().toString());
            workspaceComponent.putWorkspaceMetadata(workspaceMetadata);
        }

        return "redirect:/workspace/" + workspaceId + "/settings";
    }

}