package com.structurizr.server.web.embed;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.server.component.workspace.WorkspaceVersion;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.Views;
import com.structurizr.server.web.workspace.AbstractWorkspaceController;
import com.structurizr.util.HtmlUtils;
import com.structurizr.util.JsonUtils;
import com.structurizr.util.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.ModelMap;

import java.util.List;

abstract class AbstractEmbedController extends AbstractWorkspaceController {

    @Override
    protected void addXFrameOptionsHeader(HttpServletResponse response) {
        // do nothing ... this page is supposed to be iframe'd
    }

    protected final String showEmbed(
            WorkspaceMetadata workspaceMetadata,
            String branch,
            String diagramIdentifier,
            boolean health,
            ModelMap model) {

        diagramIdentifier = HtmlUtils.filterHtml(diagramIdentifier);
        diagramIdentifier = HtmlUtils.escapeQuoteCharacters(diagramIdentifier);

        if (!StringUtils.isNullOrEmpty(diagramIdentifier)) {
            model.addAttribute("diagramIdentifier", diagramIdentifier);
        }

        if (WorkspaceBranch.isMainBranch(branch)) {
            branch = "";
        } else {
            if (!StringUtils.isNullOrEmpty(branch) && !Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_BRANCHES)) {
                return showError("workspace-branches-not-enabled", model);
            }

            // check branch exists
            WorkspaceBranch.validateBranchName(branch);

            final String requestedBranch = branch;
            List<WorkspaceBranch> branches = workspaceComponent.getWorkspaceBranches(workspaceMetadata.getId());

            if (branches.stream().noneMatch(b -> b.getName().equals(requestedBranch))) {
                model.addAttribute("errorMessage", "Branch \"" + requestedBranch + "\" does not exist");
                return show404Page(model);
            }

            workspaceMetadata.setBranch(branch);
        }

        addCommonAttributes(model, "", false);
        addUrlSuffix(branch, WorkspaceVersion.LATEST_VERSION, model);

        workspaceMetadata.setEditable(false);
        model.addAttribute("workspace", workspaceMetadata);

        String json = workspaceComponent.getWorkspace(workspaceMetadata.getId(), branch, WorkspaceVersion.LATEST_VERSION);
        model.addAttribute("workspaceAsJson", JsonUtils.base64(json));
        model.addAttribute("embed", true);
        model.addAttribute("health", health);
        model.addAttribute("publishThumbnails", false);

        return Views.DIAGRAMS;
    }

}