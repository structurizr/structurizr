package com.structurizr.playground;

import com.structurizr.Workspace;
import com.structurizr.configuration.Configuration;
import com.structurizr.dsl.DslUtils;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.dsl.StructurizrDslParserException;
import com.structurizr.http.HttpClientException;
import com.structurizr.util.*;
import com.structurizr.validation.WorkspaceScopeValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

@Controller
class PlaygroundController extends AbstractController {

    private static final Log log = LogFactory.getLog(PlaygroundController.class);

    private static final String JSON_EXTENSION = ".json";

    @RequestMapping(value = "/", method = RequestMethod.GET)
    String getPlayground(ModelMap model,
                         @RequestParam(required = false, defaultValue = "") String src,
                         @RequestParam(required = false, defaultValue = "") String view
                         ) throws Exception {

        model.addAttribute("method", "get");
        model.addAttribute("loadFromLocalStorage", true);

        String dsl = "";
        String json = "";

        try {
            if (!StringUtils.isNullOrEmpty(src) && (Url.isHttpsUrl(src) || Url.isHttpUrl(src))) {
                model.addAttribute("loadFromLocalStorage", false);

                if (src.endsWith(JSON_EXTENSION)) {
                    json = Configuration.getInstance().createHttpClient().get(src).getContentAsString();
                    Workspace workspace = WorkspaceUtils.fromJson(json);
                    dsl = DslUtils.getDsl(workspace);
                } else {
                    dsl = Configuration.getInstance().createHttpClient().get(src).getContentAsString();
                }
            }
        } catch (HttpClientException hce) {
            log.error(hce);
            model.addAttribute("errorMessage", hce.getMessage());
        }

        return show(model, dsl, json, view);
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    String postDsl(ModelMap model,
                       @RequestParam(required = true) String dsl,
                       @RequestParam(required = false) String json,
                       @RequestParam(required = false, defaultValue = "") String view) throws Exception {

        model.addAttribute("method", "post");

        if (dsl.startsWith("{")) {
            // JSON has been provided rather than DSL ... parse this and extract the DSL
            json = dsl;
            Workspace workspace = WorkspaceUtils.fromJson(json);
            dsl = DslUtils.getDsl(workspace);
        }

        return show(model, dsl, json, view);
    }

    String show(ModelMap model, String dsl, String json, String view) throws Exception {
        if (StringUtils.isNullOrEmpty(dsl)) {
            dsl = DslTemplate.generate("Name", "Description");
        }

        view = HtmlUtils.filterHtml(view);

        Workspace workspace = null;

        try {
            workspace = fromDsl(dsl);
        } catch (StructurizrDslParserException e) {
            model.addAttribute("line", e.getLineNumber());
            model.addAttribute("errorMessage", e.getMessage());
        } catch (WorkspaceScopeValidationException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        model.addAttribute("dslVersion", Class.forName(StructurizrDslParser.class.getCanonicalName()).getPackage().getImplementationVersion());
        model.addAttribute("workspaceAsDsl", dsl);
        model.addAttribute("view", view);
        model.addAttribute("workspace", new WorkspaceMetaData());

        if (workspace != null) {
            workspace.setLastModifiedDate(new Date());

            if (!StringUtils.isNullOrEmpty(json)) {
                Workspace oldWorkspace = WorkspaceUtils.fromJson(json);

                try {
                    workspace.getViews().copyLayoutInformationFrom(oldWorkspace.getViews());
                } catch (Exception e) {
                    // ignore
                }
            }

            model.addAttribute("workspaceAsJson", JsonUtils.base64(WorkspaceUtils.toJson(workspace, false)));
        } else {
            if (!StringUtils.isNullOrEmpty(json)) {
                Workspace oldWorkspace = WorkspaceUtils.fromJson(json);
                model.addAttribute("workspaceAsJson", JsonUtils.base64(WorkspaceUtils.toJson(oldWorkspace, false)));
            } else {
                model.addAttribute("workspaceAsJson", JsonUtils.base64(WorkspaceUtils.toJson(new Workspace("Workspace", ""), false)));
            }
        }

        return "playground";
    }

    private Workspace fromDsl(String dsl) throws StructurizrDslParserException, WorkspaceScopeValidationException {
        StructurizrDslParser parser = Configuration.getInstance().createStructurizrDslParser();
        parser.parse(dsl);

        Workspace workspace = parser.getWorkspace();
        DslUtils.setDsl(workspace, dsl);

        // add default views if no views are explicitly defined
        if (!workspace.getModel().isEmpty() && workspace.getViews().isEmpty()) {
            workspace.getViews().createDefaultViews();
        }

        return workspace;
    }

}