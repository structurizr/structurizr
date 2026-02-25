package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.Configuration;
import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.server.domain.Image;
import com.structurizr.server.domain.Permission;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.Views;
import com.structurizr.util.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@Profile("command-server")
class ImagesController extends AbstractWorkspaceController {

    @RequestMapping(value = "/workspace/{workspaceId}/images", method = RequestMethod.GET)
    public String showAuthenticatedImages(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            ModelMap model
    ) {
        return showAuthenticatedView(
                    Views.PUBLISHED_IMAGES, workspaceId,
                    workspaceMetadata -> {
                        List<Image> images = workspaceComponent.getImages(workspaceId, branch);
                        for (Image image : images) {
                            String url = "/workspace/" + workspaceId;
                            if (!Configuration.getInstance().isAuthenticationEnabled()) {
                                url = "/workspace/" + workspaceId;
                            } else if (workspaceMetadata.isPublicWorkspace()) {
                                url = "/share/" + workspaceId;
                            } else if (workspaceMetadata.isShareable()) {
                                url = "/share/" + workspaceId + "/" + workspaceMetadata.getSharingToken();
                            }

                            if (!StringUtils.isNullOrEmpty(branch)) {
                                url += "/branch/" + branch;
                            }
                            url += "/images/" + image.getName();
                            image.setUrl(url);
                        }

                        images = images.stream().filter(i -> !i.getName().endsWith("thumbnail.png") && !i.getName().endsWith("thumbnail-dark.png")).collect(Collectors.toList());
                        images.sort(Comparator.comparing(i -> i.getName().toLowerCase()));

                        model.addAttribute("images", images);
                    },
                    branch, null, model, true, true);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/images/delete", method = RequestMethod.POST)
    public String deletePublishedImages(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            ModelMap model) {

        WorkspaceMetadata workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
        if (workspaceMetadata == null) {
            return show404Page(model);
        }

        if (WorkspaceBranch.isMainBranch(branch)) {
            branch = "";
        }

        Set<Permission> permissions = workspaceMetadata.getPermissions(getUser());
        if (permissions.contains(Permission.Write)) {
            workspaceComponent.deleteImages(workspaceId, branch);
        }

        if (StringUtils.isNullOrEmpty(branch)) {
            return "redirect:/workspace/" + workspaceId + "/images";
        } else {
            return "redirect:/workspace/" + workspaceId + "/images?branch=" + branch;
        }
    }

}