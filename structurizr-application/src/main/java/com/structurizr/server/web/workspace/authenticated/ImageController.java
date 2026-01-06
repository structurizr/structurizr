package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.api.ApiException;
import com.structurizr.server.web.api.ApiResponse;
import com.structurizr.server.web.workspace.AbstractImageController;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

@Controller
class ImageController extends AbstractImageController {

    @ResponseBody
    @RequestMapping(value = "/workspace/{workspaceId}/images/{diagramKey}.png", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public Resource getAuthenticatedImage(@PathVariable("workspaceId") long workspaceId,
                                          @PathVariable("diagramKey") String diagramKey,
                                          HttpServletResponse response) {
        WorkspaceMetadata workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
        if (workspaceMetadata == null) {
            response.setStatus(404);
            return null;
        }

        if (workspaceMetadata.hasAccess(getUser())) {
            return getImage(workspaceMetadata, WorkspaceBranch.NO_BRANCH, diagramKey, response);
        } else {
            response.setStatus(404);
            return null;
        }
    }

    @ResponseBody
    @RequestMapping(value = "/workspace/{workspaceId}/branch/{branch}/images/{diagramKey}.png", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public Resource getAuthenticatedImage(@PathVariable("workspaceId") long workspaceId,
                                          @PathVariable("branch") String branch,
                                          @PathVariable("diagramKey") String diagramKey,
                                          HttpServletResponse response) {
        WorkspaceMetadata workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
        if (workspaceMetadata == null) {
            response.setStatus(404);
            return null;
        }

        if (workspaceMetadata.hasAccess(getUser())) {
            return getImage(workspaceMetadata, branch, diagramKey, response);
        }

        response.setStatus(404);
        return null;
    }

    public boolean isAnonymousThumbnailsEnabled() {
        return Configuration.getInstance().isFeatureEnabled(Features.DIAGRAM_ANONYMOUS_THUMBNAILS);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/branch/{branch}/images/{filename:.+}", method = RequestMethod.OPTIONS)
    @PreAuthorize("isAuthenticated() || this.isAnonymousThumbnailsEnabled()")
    public void optionsImage(@PathVariable("workspaceId") long workspaceId, @PathVariable("branch") String branch, @PathVariable("filename") String filename, HttpServletResponse response) {
        addAccessControlAllowHeaders(response);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/images/{filename:.+}", method = RequestMethod.OPTIONS)
    @PreAuthorize("isAuthenticated() || this.isAnonymousThumbnailsEnabled()")
    public void optionsImage(@PathVariable("workspaceId") long workspaceId, @PathVariable("filename") String filename, HttpServletResponse response) {
        addAccessControlAllowHeaders(response);
    }

    private void addAccessControlAllowHeaders(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Headers", "accept, origin, " + HttpHeaders.CONTENT_TYPE);
        response.addHeader("Access-Control-Allow-Methods", "GET, PUT");
    }

    @RequestMapping(value = "/workspace/{workspaceId}/branch/{branch}/images/{filename:.+}", method = RequestMethod.PUT, consumes = "text/plain", produces = "application/json; charset=UTF-8")
    @PreAuthorize("isAuthenticated() || this.isAnonymousThumbnailsEnabled()")
    public @ResponseBody ApiResponse putImage(@PathVariable("workspaceId")long workspaceId,
                                              @PathVariable("branch")String branch,
                                              @PathVariable("filename")String filename,
                                              @RequestBody String imageAsBase64EncodedDataUri,
                                              @ModelAttribute("remoteIpAddress") String ipAddress) {

        return storeImage(workspaceId, branch, filename, imageAsBase64EncodedDataUri, ipAddress);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/images/{filename:.+}", method = RequestMethod.PUT, consumes = "text/plain", produces = "application/json; charset=UTF-8")
    @PreAuthorize("isAuthenticated() || this.isAnonymousThumbnailsEnabled()")
    public @ResponseBody ApiResponse putImage(@PathVariable("workspaceId")long workspaceId,
                                              @PathVariable("filename")String filename,
                                              @RequestBody String imageAsBase64EncodedDataUri,
                                              @ModelAttribute("remoteIpAddress") String ipAddress) {

        return storeImage(workspaceId, WorkspaceBranch.NO_BRANCH, filename, imageAsBase64EncodedDataUri, ipAddress);
    }

    private ApiResponse storeImage(long workspaceId, String branch, String filename, String imageAsBase64EncodedDataUri, String ipAddress) {
        WorkspaceBranch.validateBranchName(branch);

        WorkspaceMetadata workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
        if (workspaceMetadata == null) {
            throw new ApiException("404");
        }

        if (workspaceMetadata.hasAccess(getUser())) {
            try {
                String base64Image = imageAsBase64EncodedDataUri.split(",")[1];
                byte[] decodedImage = Base64.getDecoder().decode(base64Image.getBytes(StandardCharsets.UTF_8));
                File file = File.createTempFile("structurizr", ".png");
                Files.write(file.toPath(), decodedImage);

                if (workspaceComponent.putImage(workspaceId, branch, filename, file)) {
                    return new ApiResponse("OK");
                } else {
                    throw new ApiException("Failed to save image");
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new ApiException("Failed to save image");
            }
        }

        throw new ApiException("Failed to save image");
    }

}