package com.structurizr.server.web.embed;

import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.Views;
import com.structurizr.server.web.workspace.AbstractWorkspaceController;
import com.structurizr.util.HtmlUtils;
import com.structurizr.util.StringUtils;
import com.structurizr.view.PaperSize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class
EmbedFromParentController extends AbstractWorkspaceController {

    @RequestMapping(value = "/embed", method = RequestMethod.GET)
    public String embedFromParent(@RequestParam(required = false, defaultValue = "0") long workspace,
                                  @RequestParam(required = false) String branch,
                                  @RequestParam(required = false) String version,
                                  @RequestParam(required = false) String type,
                                  @RequestParam(required = false) String view,
                                  @RequestParam(required = false) String perspective,
                                  @RequestParam(required = false, defaultValue = "false") boolean editable,
                                  @RequestParam(required = false) String urlPrefix,
                                  ModelMap model) {

        type = HtmlUtils.filterHtml(type);
        view = HtmlUtils.filterHtml(view);
        view = HtmlUtils.escapeQuoteCharacters(view);
        perspective = HtmlUtils.filterHtml(perspective);
        urlPrefix = HtmlUtils.filterHtml(urlPrefix);

        WorkspaceMetadata workspaceMetadata = new WorkspaceMetadata(workspace);
        workspaceMetadata.setName("Embedded workspace");
        workspaceMetadata.setEditable(editable);

        model.addAttribute("workspace", workspaceMetadata);
        model.addAttribute("loadWorkspaceFromParent", true);
        model.addAttribute("embed", true);
        addCommonAttributes(model, "", false);

        if (!StringUtils.isNullOrEmpty(urlPrefix)) {
            model.addAttribute("urlPrefix", urlPrefix);
        }

        addUrlSuffix(branch, version, model);

        if ("graph".equals(type)) {
            model.addAttribute("view", view);

            return Views.GRAPH;
        } else if ("tree".equals(type)) {
            model.addAttribute("view", view);

            return Views.TREE;
        } else {
            if (!StringUtils.isNullOrEmpty(view)) {
                model.addAttribute("diagramIdentifier", view);
            }

            if (!StringUtils.isNullOrEmpty(urlPrefix) && urlPrefix.startsWith("/workspace")) {
                model.addAttribute("publishThumbnails", StringUtils.isNullOrEmpty(branch) && StringUtils.isNullOrEmpty(version));
            } else {
                model.addAttribute("publishThumbnails", false);
            }

            if (workspaceMetadata.isEditable()) {
                model.addAttribute("paperSizes", PaperSize.getOrderedPaperSizes());
            }

            model.addAttribute("showToolbar", editable);
            model.addAttribute("perspective", perspective);

            return Views.DIAGRAMS;
        }
    }

}