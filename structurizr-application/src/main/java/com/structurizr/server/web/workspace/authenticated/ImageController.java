package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.server.domain.Permission;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.api.ApiException;
import com.structurizr.server.web.api.ApiResponse;
import com.structurizr.server.web.api.NotFoundApiException;
import com.structurizr.server.web.workspace.AbstractImageController;
import com.structurizr.util.ImageUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.Set;

@Controller
class ImageController extends AbstractImageController {

    @ResponseBody
    @RequestMapping(value = "/workspace/{workspaceId}/images/{filename}.png", method = RequestMethod.GET, produces = ImageUtils.CONTENT_TYPE_IMAGE_PNG)
    public Resource getAuthenticatedPngImage(@PathVariable("workspaceId") long workspaceId,
                                          @PathVariable("filename") String filename,
                                          HttpServletResponse response) {
        return getImage(workspaceId, WorkspaceBranch.NO_BRANCH, filename + ImageUtils.PNG_EXTENSION, response);
    }

    @ResponseBody
    @RequestMapping(value = "/workspace/{workspaceId}/images/{filename}.svg", method = RequestMethod.GET, produces = ImageUtils.CONTENT_TYPE_IMAGE_SVG)
    public Resource getAuthenticatedSvgImage(@PathVariable("workspaceId") long workspaceId,
                                          @PathVariable("filename") String filename,
                                          HttpServletResponse response) {
        return getImage(workspaceId, WorkspaceBranch.NO_BRANCH, filename + ImageUtils.SVG_EXTENSION, response);
    }

    @ResponseBody
    @RequestMapping(value = "/workspace/{workspaceId}/branch/{branch}/images/{filename}.png", method = RequestMethod.GET, produces = ImageUtils.CONTENT_TYPE_IMAGE_PNG)
    public Resource getAuthenticatedPngImage(@PathVariable("workspaceId") long workspaceId,
                                          @PathVariable("branch") String branch,
                                          @PathVariable("filename") String filename,
                                          HttpServletResponse response) {
        return getImage(workspaceId, branch, filename + ImageUtils.PNG_EXTENSION, response);
    }

    protected Resource getImage(long workspaceId, String branch, String filename, HttpServletResponse response) {
        WorkspaceMetadata workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
        if (workspaceMetadata == null) {
            response.setStatus(404);
            return null;
        }

        Set<Permission> permissions = workspaceMetadata.getPermissions(getUser());
        if (!permissions.isEmpty()) {
            return getImage(workspaceMetadata, branch, filename, response);
        }

        response.setStatus(404);
        return null;
    }

    @RequestMapping(value = "/workspace/{workspaceId}/branch/{branch}/images/{filename:.+}", method = RequestMethod.OPTIONS)
    public void optionsImage(@PathVariable("workspaceId") long workspaceId, @PathVariable("branch") String branch, @PathVariable("filename") String filename, HttpServletResponse response) {
        addAccessControlAllowHeaders(response);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/images/{filename:.+}", method = RequestMethod.OPTIONS)
    public void optionsImage(@PathVariable("workspaceId") long workspaceId, @PathVariable("filename") String filename, HttpServletResponse response) {
        addAccessControlAllowHeaders(response);
    }

    private void addAccessControlAllowHeaders(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Headers", "accept, origin, " + HttpHeaders.CONTENT_TYPE);
        response.addHeader("Access-Control-Allow-Methods", "GET, PUT");
    }

    @RequestMapping(value = "/workspace/{workspaceId}/images/{filename:.+}", method = RequestMethod.PUT, consumes = "text/plain", produces = "application/json; charset=UTF-8")
    public @ResponseBody ApiResponse putImage(@PathVariable("workspaceId")long workspaceId,
                                              @PathVariable("filename")String filename,
                                              @RequestBody String imageAsBase64EncodedDataUri) {

        return storeImage(workspaceId, WorkspaceBranch.NO_BRANCH, filename, imageAsBase64EncodedDataUri);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/branch/{branch}/images/{filename:.+}", method = RequestMethod.PUT, consumes = "text/plain", produces = "application/json; charset=UTF-8")
    public @ResponseBody ApiResponse putImage(@PathVariable("workspaceId")long workspaceId,
                                              @PathVariable("branch")String branch,
                                              @PathVariable("filename")String filename,
                                              @RequestBody String imageAsBase64EncodedDataUri) {

        return storeImage(workspaceId, branch, filename, imageAsBase64EncodedDataUri);
    }

    private ApiResponse storeImage(long workspaceId, String branch, String filename, String imageAsBase64EncodedDataUri) {
        WorkspaceBranch.validateBranchName(branch);

        WorkspaceMetadata workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
        if (workspaceMetadata == null) {
            throw new NotFoundApiException();
        }

        Set<Permission> permissions = workspaceMetadata.getPermissions(getUser());
        if (!permissions.isEmpty()) {
            try {
                File file;
                if (filename.toLowerCase().endsWith(ImageUtils.PNG_EXTENSION)) {
                    file = File.createTempFile("structurizr", ImageUtils.PNG_EXTENSION);
                } else if (filename.endsWith(ImageUtils.SVG_EXTENSION)) {
                    file = File.createTempFile("structurizr", ImageUtils.SVG_EXTENSION);
                } else {
                    throw new NotFoundApiException();
                }

                ImageUtils.writeDataUriToFile(imageAsBase64EncodedDataUri, file);

                if (workspaceComponent.putImage(workspaceId, branch, filename, file)) {
                    return new ApiResponse("OK");
                } else {
                    throw new ApiException("Failed to save image");
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                throw new ApiException("Failed to save image");
            }
        }

        throw new NotFoundApiException();
    }

}