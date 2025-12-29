package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.Views;
import com.structurizr.util.DocumentationScope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.structurizr.configuration.StructurizrProperties.AUTO_REFRESH_INTERVAL_PROPERTY;

@Controller
public class DocumentationController extends AbstractWorkspaceController {

    @RequestMapping(value = "/workspace/{workspaceId}/documentation", method = RequestMethod.GET)
    public String showAuthenticatedDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        return showAuthenticatedDocumentation(workspaceId, branch, version, null, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/documentation/{softwareSystem}", method = RequestMethod.GET)
    public String showAuthenticatedDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            ModelMap model
    ) {
        return showAuthenticatedDocumentation(workspaceId, branch, version, softwareSystem, null, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/documentation/{softwareSystem}/{container}", method = RequestMethod.GET)
    public String showAuthenticatedDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("container") String container,
            ModelMap model
    ) {
        return showAuthenticatedDocumentation(workspaceId, branch, version, softwareSystem, container, null, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/documentation/{softwareSystem}/{container}/{component}", method = RequestMethod.GET)
    public String showAuthenticatedDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("container") String container,
            @PathVariable("component") String component,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        model.addAttribute("scope", Base64.getEncoder().encodeToString(DocumentationScope.format(softwareSystem, container, component).getBytes(StandardCharsets.UTF_8)));
        model.addAttribute("showHeader", true);

        if (Configuration.getInstance().getProfile() == Profile.Local) {
            model.addAttribute("autoRefreshInterval", Integer.parseInt(Configuration.getInstance().getProperty(AUTO_REFRESH_INTERVAL_PROPERTY)));
            model.addAttribute("autoRefreshLastModifiedDate", workspaceComponent.getLastModifiedDate());
        }

        return showAuthenticatedView(Views.DOCUMENTATION, workspaceMetaData, branch, version, model, false, false);
    }

}