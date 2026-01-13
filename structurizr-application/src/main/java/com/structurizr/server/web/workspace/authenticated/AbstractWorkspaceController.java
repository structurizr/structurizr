package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.configuration.Profile;
import com.structurizr.dsl.StructurizrDslParserException;
import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.Permission;
import com.structurizr.server.domain.User;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.util.RandomGuidGenerator;
import com.structurizr.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ui.ModelMap;

import java.util.List;
import java.util.Set;

/**
 * Base class for all controllers underneath /workspace.
 */
abstract class AbstractWorkspaceController extends com.structurizr.server.web.workspace.AbstractWorkspaceController {

    private static final Log log = LogFactory.getLog(AbstractWorkspaceController.class);

    private static final String AGENT = "structurizr";

    protected final String showAuthenticatedView(String view, long workspaceId, String branch, String version, ModelMap model, boolean showHeaderAndFooter, boolean editable) {
        return showAuthenticatedView(view, workspaceId, (workspaceMetadata1 -> {}), branch, version, model, showHeaderAndFooter, editable);
    }

    protected final String showAuthenticatedView(String view, long workspaceId, AuthenticatedViewFunction function, String branch, String version, ModelMap model, boolean showHeaderAndFooter, boolean editable) {
        WorkspaceMetadata workspaceMetadata = null;

        try {
            workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
        } catch (WorkspaceComponentException e) {
            if (Configuration.getInstance().getProfile() == Profile.Local && e.getCause() instanceof StructurizrDslParserException) {
                return showError((StructurizrDslParserException)e.getCause(), model);
            } else {
                log.error(e);
            }
        }

        if (workspaceMetadata == null) {
            return show404Page(model);
        }

        User user = getUser();
        Set<Permission> permissions = workspaceMetadata.getPermissions(user);

        if (permissions.isEmpty()) {
            return show404Page(model);
        }

        if (WorkspaceBranch.isMainBranch(branch)) {
            branch = "";
        }

        String urlPrefix = "/workspace/" + workspaceMetadata.getId();
        model.addAttribute(URL_PREFIX, urlPrefix);

        if (!StringUtils.isNullOrEmpty(branch)) {
            model.addAttribute("thumbnailUrl", urlPrefix + "/branch/" + branch + "/images/");
        } else {
            model.addAttribute("thumbnailUrl", urlPrefix + "/images/");
        }

        if (Configuration.getInstance().getProfile() == Profile.Server) {
            if (workspaceMetadata.isPublicWorkspace()) {
                model.addAttribute("sharingUrlPrefix", "/share/" + workspaceMetadata.getId());
            } else if (workspaceMetadata.isShareable()) {
                model.addAttribute("sharingUrlPrefix", "/share/" + workspaceMetadata.getId() + "/" + workspaceMetadata.getSharingToken());
            }
        }

        function.run(workspaceMetadata);

        if (permissions.contains(Permission.Write)) {
            return showView(view, workspaceMetadata, branch, version, model, editable, showHeaderAndFooter);
        } else if (permissions.contains(Permission.Read)) {
            return showView(view, workspaceMetadata, branch, version, model, false, showHeaderAndFooter);
        } else {
            return show404Page(model);
        }
    }

    protected final String lockWorkspaceAndShowAuthenticatedView(String view, WorkspaceMetadata workspaceMetadata, String branch, String version, ModelMap model, boolean showHeaderAndFooter) {
        Set<Permission> permissions = workspaceMetadata.getPermissions(getUser());
        if (!permissions.contains(Permission.Write)) {
            if (permissions.contains(Permission.Read)) {
                return showError("workspace-is-readonly", model);
            } else {
                return show404Page(model);
            }
        }

        boolean success = false;
        String agent = AGENT + "/" + view + "/" + new RandomGuidGenerator().generate();

        if (Configuration.getInstance().getProfile() == Profile.Server) {
            try {
                success = workspaceComponent.lockWorkspace(workspaceMetadata.getId(), getUser().getUsername(), agent);
            } catch (WorkspaceComponentException e) {
                log.error(e);
            }
        } else {
            success = true;
        }

        if (!success) {
            if (workspaceMetadata.isLocked()) {
                model.addAttribute("showHeader", true);
                model.addAttribute("showFooter", true);
                addCommonAttributes(model, "Workspace locked", true);
                model.addAttribute("workspace", workspaceMetadata);

                return showError("workspace-locked", model);
            } else {
                workspaceMetadata.setEditable(false);
                model.addAttribute("showHeader", true);
                model.addAttribute("showFooter", true);
                addCommonAttributes(model, "Workspace could not be locked", true);
                model.addAttribute("workspace", workspaceMetadata);

                return showError("workspace-could-not-be-locked", model);
            }
        } else {
            model.addAttribute("userAgent", agent);
            return showAuthenticatedView(view, workspaceMetadata.getId(), branch, version, model, showHeaderAndFooter, true);
        }
    }

}