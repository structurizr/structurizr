package com.structurizr.playground;

import com.structurizr.util.HtmlUtils;
import com.structurizr.util.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class EmbedController extends AbstractController {

    @RequestMapping(value = "/embed", method = RequestMethod.GET)
    String embedFromParent(@RequestParam(required = false) String view,
                                  @RequestParam(required = false) String perspective,
                                  @RequestParam(required = false, defaultValue = "false") boolean editable,
                                  @RequestParam(required = false, defaultValue = "") String iframe,
                                  ModelMap model) {

        view = HtmlUtils.filterHtml(view);
        view = HtmlUtils.escapeQuoteCharacters(view);
        perspective = HtmlUtils.filterHtml(perspective);
        iframe = HtmlUtils.filterHtml(iframe);

        model.addAttribute("workspace", new WorkspaceMetaData());
        model.addAttribute("loadWorkspaceFromParent", true);
        model.addAttribute("embed", true);
        model.addAttribute("iframe", iframe);

        if (!StringUtils.isNullOrEmpty(view)) {
            model.addAttribute("diagramIdentifier", view);
        }

        model.addAttribute("publishThumbnails", false);
        model.addAttribute("showToolbar", editable);
        model.addAttribute("showDiagramSelector", false);
        model.addAttribute("perspective", perspective);

        return "diagrams";
    }

}