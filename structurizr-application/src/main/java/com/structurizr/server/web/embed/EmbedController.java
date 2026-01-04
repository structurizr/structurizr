package com.structurizr.server.web.embed;

import com.structurizr.configuration.Configuration;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.Views;
import com.structurizr.server.web.workspace.AbstractWorkspaceController;
import com.structurizr.util.HtmlUtils;
import com.structurizr.util.JsonUtils;
import com.structurizr.util.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Profile("command-server")
public class EmbedController extends AbstractEmbedController {

    @RequestMapping(value = "/embed/{workspaceId}")
    public String embedDiagrams(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(value = "diagram", required = false) String diagramIdentifier,
            @RequestParam(required = false) boolean diagramSelector,
            @RequestParam(required = false, defaultValue = "") String iframe,
            @RequestParam(required = false) boolean health,
            @RequestParam(required = false) String perspective,
            ModelMap model) {

        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return "404";
        }

        if (!Configuration.getInstance().isAuthenticationEnabled()) {
            model.addAttribute("urlPrefix", "/workspace/" + workspaceId);
            return showEmbed(workspaceMetaData, diagramIdentifier, diagramSelector, iframe, health, perspective, model);
        }

        if (workspaceMetaData.isPublicWorkspace()) {
            model.addAttribute("urlPrefix", "/share/" + workspaceId);
            return showEmbed(workspaceMetaData, diagramIdentifier, diagramSelector, iframe, health, perspective, model);
        }

        return "404";
    }

}