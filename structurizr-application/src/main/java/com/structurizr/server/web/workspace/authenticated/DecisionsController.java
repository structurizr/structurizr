package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.Configuration;
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

@Controller(value = "AnonymousDecisionsController")
class DecisionsController extends AbstractWorkspaceController {

    @RequestMapping(value = "/workspace/{workspaceId}/decisions", method = RequestMethod.GET)
    String showAuthenticatedDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        return showAuthenticatedDecisions(workspaceId, branch, version, null, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/decisions/{softwareSystem}", method = RequestMethod.GET)
    String showAuthenticatedDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            ModelMap model
    ) {
        return showAuthenticatedDecisions(workspaceId, branch, version, softwareSystem, null, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/decisions/{softwareSystem}/{container}", method = RequestMethod.GET)
    String showAuthenticatedDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("container") String container,
            ModelMap model
    ) {
        return showAuthenticatedDecisions(workspaceId, branch, version, softwareSystem, container, null, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/decisions/{softwareSystem}/{container}/{component}", method = RequestMethod.GET)
    String showAuthenticatedDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("container") String container,
            @PathVariable("component") String component,
            ModelMap model
    ) {
        model.addAttribute("showHeader", true);
        model.addAttribute("scope", Base64.getEncoder().encodeToString(DocumentationScope.format(softwareSystem, container, component).getBytes(StandardCharsets.UTF_8)));

        if (Configuration.getInstance().getProfile() == com.structurizr.configuration.Profile.Local) {
            enableLocalRefresh(model);
        }

        return showAuthenticatedView(Views.DECISIONS, workspaceId, branch, version, model, false, false);
    }

}