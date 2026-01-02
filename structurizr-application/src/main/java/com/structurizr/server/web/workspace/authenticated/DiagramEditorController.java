package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.Profile;
import com.structurizr.configuration.Configuration;
import com.structurizr.dsl.StructurizrDslParserException;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.Views;
import com.structurizr.util.StringUtils;
import com.structurizr.view.PaperSize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import static com.structurizr.configuration.Configuration.getInstance;
import static com.structurizr.configuration.StructurizrProperties.*;

@Controller
public class DiagramEditorController extends AbstractWorkspaceController {

    @RequestMapping(value = "/workspace/{workspaceId}/diagram-editor", method = RequestMethod.GET)
    public String showAuthenticatedDiagramEditor(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        if (Configuration.getInstance().getProfile() == com.structurizr.configuration.Profile.Local) {
            if ("false".equalsIgnoreCase(Configuration.getInstance().getProperty(EDITABLE_PROPERTY))) {
                return show404Page(model);
            }

            model.addAttribute("autoSaveInterval", Integer.parseInt(getInstance().getProperty(AUTO_SAVE_INTERVAL_PROPERTY)));
            enableLocalRefresh(model);
        }

        WorkspaceMetaData workspaceMetaData = null;

        try {
            workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        } catch (WorkspaceComponentException e) {
            if (Configuration.getInstance().getProfile() == Profile.Local && e.getCause() instanceof StructurizrDslParserException) {
                return showError((StructurizrDslParserException)e.getCause(), model);
            }
        }

        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        model.addAttribute("publishThumbnails", StringUtils.isNullOrEmpty(version));
        model.addAttribute("createReviews", true);
        model.addAttribute("quickNavigationPath", "diagram-editor");
        model.addAttribute("paperSizes", PaperSize.getOrderedPaperSizes());

        if (!workspaceMetaData.hasNoUsersConfigured() && !workspaceMetaData.isWriteUser(getUser())) {
            if (workspaceMetaData.isReadUser(getUser())) {
                return showError("workspace-is-readonly", model);
            } else {
                return show404Page(model);
            }
        }

        return lockWorkspaceAndShowAuthenticatedView(Views.DIAGRAMS, workspaceMetaData, branch, version, model, false);
    }

}