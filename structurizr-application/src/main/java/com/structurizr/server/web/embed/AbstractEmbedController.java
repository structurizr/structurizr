package com.structurizr.server.web.embed;

import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.Views;
import com.structurizr.server.web.workspace.AbstractWorkspaceController;
import com.structurizr.util.HtmlUtils;
import com.structurizr.util.JsonUtils;
import com.structurizr.util.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.ModelMap;

abstract class AbstractEmbedController extends AbstractWorkspaceController {

    @Override
    protected void addXFrameOptionsHeader(HttpServletResponse response) {
        // do nothing ... this page is supposed to be iframe'd
    }

    protected final String showEmbed(
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