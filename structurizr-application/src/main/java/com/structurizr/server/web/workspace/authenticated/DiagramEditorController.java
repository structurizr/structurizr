package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.Profile;
import com.structurizr.configuration.Configuration;
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
import static com.structurizr.configuration.StructurizrProperties.AUTO_REFRESH_INTERVAL_PROPERTY;
import static com.structurizr.configuration.StructurizrProperties.AUTO_SAVE_INTERVAL_PROPERTY;

@Controller
public class DiagramEditorController extends AbstractWorkspaceController {

    @RequestMapping(value = "/workspace/{workspaceId}/diagram-editor", method = RequestMethod.GET)
    public String showAuthenticatedDiagramEditor(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        model.addAttribute("publishThumbnails", StringUtils.isNullOrEmpty(version));
        model.addAttribute("createReviews", true);
        model.addAttribute("quickNavigationPath", "diagram-editor");
        model.addAttribute("paperSizes", PaperSize.getOrderedPaperSizes());

        if (Configuration.getInstance().getProfile() == com.structurizr.configuration.Profile.Local) {
            model.addAttribute("autoSaveInterval", Integer.parseInt(getInstance().getProperty(AUTO_SAVE_INTERVAL_PROPERTY)));
            enableLocalRefresh(model);
        }

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