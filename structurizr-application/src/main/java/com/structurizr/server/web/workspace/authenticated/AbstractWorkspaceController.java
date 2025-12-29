package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.configuration.Profile;
import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.User;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.util.RandomGuidGenerator;
import com.structurizr.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ui.ModelMap;

/**
 * Base class for all controllers underneath /workspace.
 */
abstract class AbstractWorkspaceController extends com.structurizr.server.web.workspace.AbstractWorkspaceController {

    private static final Log log = LogFactory.getLog(AbstractWorkspaceController.class);

    private static final String AGENT = "structurizr";

    protected final String showAuthenticatedView(String view, WorkspaceMetaData workspaceMetaData, String branch, String version, ModelMap model, boolean showHeaderAndFooter, boolean editable) {
        User user = getUser();
//        if (Configuration.getInstance().getProfile() == Profile.Server) {
//            boolean authenticationEnabled = Configuration.getInstance().isAuthenticationEnabled();
//            if (user == null && authenticationEnabled) {
//                return show404Page(model);
//            }
//        }

        if (workspaceMetaData != null) {
            String urlPrefix = "/workspace/" + workspaceMetaData.getId();
            model.addAttribute(URL_PREFIX, urlPrefix);
            if (!StringUtils.isNullOrEmpty(branch)) {
                model.addAttribute("thumbnailUrl", urlPrefix + "/branch/" + branch + "/images/");
            } else {
                model.addAttribute("thumbnailUrl", urlPrefix + "/images/");
            }

            if (Configuration.getInstance().getProfile() == Profile.Server) {
                if (workspaceMetaData.isOpen()) {
                    model.addAttribute("sharingUrlPrefix", "/share/" + workspaceMetaData.getId());
                } else if (workspaceMetaData.isShareable()) {
                    model.addAttribute("sharingUrlPrefix", "/share/" + workspaceMetaData.getId() + "/" + workspaceMetaData.getSharingToken());
                }
            }

            if (WorkspaceBranch.isMainBranch(branch)) {
                branch = "";
            }

            if (!StringUtils.isNullOrEmpty(branch) && !Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_BRANCHES)) {
                return showError("workspace-branches-not-enabled", model);
            }

            addUrlSuffix(branch, version, model);

            if (workspaceMetaData.hasNoUsersConfigured() || workspaceMetaData.isWriteUser(user)) {
                return showView(view, workspaceMetaData, branch, version, model, editable, showHeaderAndFooter);
            } else if (workspaceMetaData.isReadUser(user)) {
                return showView(view, workspaceMetaData, branch, version, model, false, showHeaderAndFooter);
            }
        }

        return show404Page(model);
    }

    protected final String lockWorkspaceAndShowAuthenticatedView(String view, WorkspaceMetaData workspaceMetaData, String branch, String version, ModelMap model, boolean showHeaderAndFooter) {
        boolean success = false;
        String agent = AGENT + "/" + view + "/" + new RandomGuidGenerator().generate();

        if (Configuration.getInstance().getProfile() == Profile.Server) {
            try {
                success = workspaceComponent.lockWorkspace(workspaceMetaData.getId(), getUser().getUsername(), agent);
            } catch (WorkspaceComponentException e) {
                log.error(e);
            }
        } else {
            success = true;
        }

        if (!success) {
            if (workspaceMetaData.isLocked()) {
                model.addAttribute("showHeader", true);
                model.addAttribute("showFooter", true);
                addCommonAttributes(model, "Workspace locked", true);
                model.addAttribute("workspace", workspaceMetaData);

                return showError("workspace-locked", model);
            } else {
                workspaceMetaData.setEditable(false);
                model.addAttribute("showHeader", true);
                model.addAttribute("showFooter", true);
                addCommonAttributes(model, "Workspace could not be locked", true);
                model.addAttribute("workspace", workspaceMetaData);

                return showError("workspace-could-not-be-locked", model);
            }
        } else {
            workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceMetaData.getId()); // refresh metadata
            model.addAttribute("userAgent", agent);
            return showAuthenticatedView(view, workspaceMetaData, branch, version, model, showHeaderAndFooter, true);
        }
    }

}