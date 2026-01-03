package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.Configuration;
import com.structurizr.server.domain.Image;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.Views;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Profile("command-server")
class ImagesController extends AbstractWorkspaceController {

    @RequestMapping(value = "/workspace/{workspaceId}/images", method = RequestMethod.GET)
    public String showAuthenticatedImages(
            @PathVariable("workspaceId") long workspaceId,
            ModelMap model
    ) {
            return showAuthenticatedView(
                    Views.PUBLISHED_IMAGES, workspaceId,
                    workspaceMetaData -> {
                        List<Image> images = workspaceComponent.getImages(workspaceId);
                        for (Image image : images) {
                            if (!Configuration.getInstance().isAuthenticationEnabled()) {
                                image.setUrl("/workspace/" + workspaceId + "/images/" + image.getName());
                            } else if (workspaceMetaData.isPublicWorkspace()) {
                                image.setUrl("/share/" + workspaceId + "/images/" + image.getName());
                            } else if (workspaceMetaData.isShareable()) {
                                image.setUrl("/share/" + workspaceId + "/" + workspaceMetaData.getSharingToken() + "/images/" + image.getName());
                            }
                        }

                        images = images.stream().filter(i -> !i.getName().endsWith("thumbnail.png") && !i.getName().endsWith("thumbnail-dark.png")).collect(Collectors.toList());
                        images.sort(Comparator.comparing(i -> i.getName().toLowerCase()));

                        model.addAttribute("images", images);
                    }
                    ,null, null, model, true, true);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/images/delete", method = RequestMethod.POST)
    public String deletePublishedImages(@PathVariable("workspaceId") long workspaceId, ModelMap model) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }
        if (workspaceMetaData.hasNoUsersConfigured() || workspaceMetaData.isWriteUser(getUser())) {
            workspaceComponent.deleteImages(workspaceId);
        }

        return "redirect:/workspace/" + workspaceId + "/images";
    }

}