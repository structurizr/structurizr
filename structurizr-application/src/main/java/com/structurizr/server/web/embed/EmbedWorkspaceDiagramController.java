package com.structurizr.server.web.embed;

import com.structurizr.configuration.Configuration;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.Views;
import com.structurizr.server.web.workspace.AbstractWorkspaceController;
import com.structurizr.util.HtmlUtils;
import com.structurizr.util.JsonUtils;
import com.structurizr.util.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EmbedWorkspaceDiagramController extends AbstractWorkspaceController {

    @Override
    protected void addXFrameOptionsHeader(HttpServletResponse response) {
        // do nothing ... this page is supposed to be iframe'd
    }

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
            return show404Page(model);
        }

        if (!Configuration.getInstance().isAuthenticationEnabled()) {
            model.addAttribute("urlPrefix", "/workspace/" + workspaceId);
            return showEmbed(workspaceMetaData, diagramIdentifier, diagramSelector, iframe, health, perspective, model);
        }

        if (workspaceMetaData.isPublicWorkspace()) {
            model.addAttribute("urlPrefix", "/share/" + workspaceId);
            return showEmbed(workspaceMetaData, diagramIdentifier, diagramSelector, iframe, health, perspective, model);
        }

        return show404Page(model);
    }

    @RequestMapping(value = "/embed/{workspaceId}/{token}", method = RequestMethod.GET)
    public String embedDiagramsViaSharingToken(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable("token") String token,
            @RequestParam(value = "diagram", required = false) String diagramIdentifier,
            @RequestParam(required = false) boolean diagramSelector,
            @RequestParam(required = false, defaultValue = "") String iframe,
            @RequestParam(required = false) boolean health,
            @RequestParam(required = false) String perspective,
            ModelMap model) {

        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        if (workspaceMetaData.isShareable() && workspaceMetaData.getSharingToken().equals(token)) {
            model.addAttribute("urlPrefix", "/share/" + workspaceId + "/" + workspaceMetaData.getSharingToken());
            return showEmbed(workspaceMetaData, diagramIdentifier, diagramSelector, iframe, health, perspective, model);
        }

        return show404Page(model);
    }

    private String showEmbed(
            WorkspaceMetaData workspaceMetaData,
            String diagramIdentifier,
            boolean diagramSelector,
            String iframe,
            boolean health,
            String perspective,
            ModelMap model) {

        diagramIdentifier = HtmlUtils.filterHtml(diagramIdentifier);
        diagramIdentifier = HtmlUtils.escapeQuoteCharacters(diagramIdentifier);
        iframe = HtmlUtils.filterHtml(iframe);
        perspective = HtmlUtils.filterHtml(perspective);

        if (!StringUtils.isNullOrEmpty(diagramIdentifier)) {
            model.addAttribute("diagramIdentifier", diagramIdentifier);
        }

        addCommonAttributes(model, "", false);

        workspaceMetaData.setEditable(false);
        model.addAttribute("workspace", workspaceMetaData);

        String json = workspaceComponent.getWorkspace(workspaceMetaData.getId(), null, null);
        model.addAttribute("workspaceAsJson", JsonUtils.base64(json));
        model.addAttribute("showToolbar", diagramSelector);
        model.addAttribute("showDiagramSelector", diagramSelector);
        model.addAttribute("embed", true);
        model.addAttribute("iframe", iframe);
        model.addAttribute("health", health);
        model.addAttribute("perspective", perspective);
        model.addAttribute("publishThumbnails", false);

        return Views.DIAGRAMS;
    }

}