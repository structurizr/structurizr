package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.Views;
import com.structurizr.util.HtmlUtils;
import com.structurizr.util.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import static com.structurizr.configuration.StructurizrProperties.AUTO_REFRESH_INTERVAL_PROPERTY;

@Controller
class DiagramViewerController extends AbstractWorkspaceController {

    @RequestMapping(value = "/workspace/{workspaceId}/diagrams", method = RequestMethod.GET)
    String showAuthenticatedDiagramViewer(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false, defaultValue = "") String branch,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String perspective,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        model.addAttribute("publishThumbnails", StringUtils.isNullOrEmpty(version));
        model.addAttribute("quickNavigationPath", "diagrams");
        model.addAttribute("perspective", HtmlUtils.filterHtml(perspective));

        boolean editable = !Configuration.getInstance().isAuthenticationEnabled() || workspaceMetaData.hasNoUsersConfigured() || workspaceMetaData.isWriteUser(getUser());
        model.addAttribute("includeEditButton", editable);

        if (Configuration.getInstance().getProfile() == Profile.Local) {
            model.addAttribute("autoRefreshInterval", Integer.parseInt(Configuration.getInstance().getProperty(AUTO_REFRESH_INTERVAL_PROPERTY)));
            model.addAttribute("autoRefreshLastModifiedDate", workspaceComponent.getLastModifiedDate());
        }

        return showAuthenticatedView(Views.DIAGRAMS, workspaceMetaData, branch, version, model, false, false);
    }

}