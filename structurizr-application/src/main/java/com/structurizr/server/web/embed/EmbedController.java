package com.structurizr.server.web.embed;

import com.structurizr.configuration.Configuration;
import com.structurizr.server.domain.WorkspaceMetadata;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Profile("command-server")
public class EmbedController extends AbstractEmbedController {

    @RequestMapping(value = "/embed/{workspaceId}")
    public String embedDiagrams(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(value = "branch", required = false) String branch,
            @RequestParam(value = "diagram", required = false) String diagramIdentifier,
            @RequestParam(required = false) boolean health,
            @RequestParam(required = false) String perspective,
            ModelMap model) {

        WorkspaceMetadata workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
        if (workspaceMetadata == null) {
            return "404";
        }

        if (!Configuration.getInstance().isAuthenticationEnabled()) {
            model.addAttribute("urlPrefix", "/workspace/" + workspaceId);
            return showEmbed(workspaceMetadata, branch, diagramIdentifier, health, perspective, model);
        }

        if (workspaceMetadata.isPublicWorkspace()) {
            model.addAttribute("urlPrefix", "/share/" + workspaceId);
            return showEmbed(workspaceMetadata, branch, diagramIdentifier, health, perspective, model);
        }

        return "404";
    }

}