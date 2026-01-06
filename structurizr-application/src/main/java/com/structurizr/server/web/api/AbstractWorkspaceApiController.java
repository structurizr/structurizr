package com.structurizr.server.web.api;

import com.structurizr.Workspace;
import com.structurizr.api.HashBasedMessageAuthenticationCode;
import com.structurizr.api.HmacAuthorizationHeader;
import com.structurizr.api.HmacContent;
import com.structurizr.api.HttpHeaders;
import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.io.WorkspaceReaderException;
import com.structurizr.io.json.JsonReader;
import com.structurizr.server.component.search.SearchComponent;
import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.server.component.workspace.WorkspaceComponent;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.AbstractController;
import com.structurizr.util.Md5Digest;
import com.structurizr.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.StringReader;
import java.util.Base64;

/**
 * An implementation of the Structurizr workspace API.
 *
 *  - GET /api/workspace/{id}
 *  - PUT /api/workspace/{id}
 */
public class AbstractWorkspaceApiController extends AbstractController {

    protected static final Log log = LogFactory.getLog(AbstractWorkspaceApiController.class);

    protected WorkspaceComponent workspaceComponent;
    protected SearchComponent searchComponent;

    @Autowired
    public void setWorkspaceComponent(WorkspaceComponent workspaceComponent) {
        this.workspaceComponent = workspaceComponent;
    }

    @Autowired
    public void setSearchComponent(SearchComponent searchComponent) {
        this.searchComponent = searchComponent;
    }

    protected final String get(long workspaceId,
                               String branch,
                               String version,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        try {
            if (WorkspaceBranch.isMainBranch(branch)) {
                branch = "";
            }

            try {
                WorkspaceBranch.validateBranchName(branch);
            } catch (IllegalArgumentException e) {
                throw new ApiException(e.getMessage());
            }

            if (workspaceId > 0) {
                authoriseRequest(workspaceId, "GET", getPath(request, workspaceId, branch), null, request, response);

                if (!StringUtils.isNullOrEmpty(branch) && !Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_BRANCHES)) {
                    throw new ApiException("Workspace branches are not enabled for this installation");
                }

                return workspaceComponent.getWorkspace(workspaceId, branch, version);
            } else {
                throw new ApiException("Workspace ID must be greater than 1");
            }
        } catch (WorkspaceComponentException e) {
            log.error(e);
            throw new ApiException("Something went wrong.");
        }
    }

    public ApiResponse put(long workspaceId,
                          String branch,
                          String json,
                          HttpServletRequest request,
                          HttpServletResponse response) {
        try {
            if (WorkspaceBranch.isMainBranch(branch)) {
                branch = "";
            }

            try {
                WorkspaceBranch.validateBranchName(branch);
            } catch (IllegalArgumentException e) {
                throw new ApiException(e.getMessage());
            }

            if (workspaceId > 0) {
                authoriseRequest(workspaceId, "PUT", getPath(request, workspaceId, branch), json, request, response);

                if (!StringUtils.isNullOrEmpty(branch) && !Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_BRANCHES)) {
                    throw new ApiException("Workspace branches are not enabled for this installation");
                }

                workspaceComponent.putWorkspace(workspaceId, branch, json);

                if (json.contains("encryptionStrategy") && json.contains("ciphertext")) {
                    // remove client-side encrypted workspaces from the search index
                    try {
                        searchComponent.delete(workspaceId);
                    } catch (Exception e) {
                        log.error(e);
                    }
                } else {
                    try {
                        Workspace workspace;
                        try {
                            JsonReader jsonReader = new JsonReader();
                            StringReader stringReader = new StringReader(json);
                            workspace = jsonReader.read(stringReader);
                        } catch (WorkspaceReaderException e) {
                            throw new ApiException(e.getMessage());
                        }

                        if (WorkspaceBranch.isMainBranch(branch)) {
                            searchComponent.index(workspace);
                        }
                    } catch (Exception e) {
                        log.error(e);
                    }
                }

                return new ApiResponse("OK");
            } else {
                throw new ApiException("Workspace ID must be greater than 1");
            }
        } catch (WorkspaceComponentException e) {
            log.error(e);
            throw new ApiException(e.getMessage());
        }
    }

    protected String getPath(HttpServletRequest request, long workspaceId, String branch) {
        String contextPath = request.getContextPath();
        if (!contextPath.endsWith("/")) {
            contextPath = contextPath + "/";
        }

        if (StringUtils.isNullOrEmpty(branch)) {
            return contextPath + "api/workspace/" + workspaceId;
        } else {
            return contextPath + "api/workspace/" + workspaceId + "/branch/" + branch;
        }
    }

    protected void authoriseRequest(long workspaceId, String httpMethod, String path, String content, HttpServletRequest request, HttpServletResponse response) throws WorkspaceComponentException {
        try {
            String authorizationHeaderAsString = request.getHeader(HttpHeaders.X_AUTHORIZATION);
            if (authorizationHeaderAsString == null || authorizationHeaderAsString.trim().length() == 0) {
                // fallback on the regular header
                authorizationHeaderAsString = request.getHeader(HttpHeaders.AUTHORIZATION);
            }

            if (authorizationHeaderAsString == null || authorizationHeaderAsString.trim().length() == 0) {
                throw new HttpUnauthorizedException("Authorization header must be provided");
            }

            WorkspaceMetadata workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
            String apiKey = workspaceMetadata.getApiKey();

            HmacAuthorizationHeader hmacAuthorizationHeader = HmacAuthorizationHeader.parse(authorizationHeaderAsString);
            String apiKeyFromAuthorizationHeader = hmacAuthorizationHeader.getApiKey();

            if (!apiKeyFromAuthorizationHeader.equals(apiKey)) {
                throw new HttpUnauthorizedException("Incorrect API key");
            }

            String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
            if (StringUtils.isNullOrEmpty(contentType) || contentType.startsWith(";")) {
                contentType = "";
            } else if (!contentType.contains(" ")) {
                String[] parts = contentType.split(";");
                contentType = parts[0] + "; " + parts[1];
            }

            String nonce = request.getHeader(HttpHeaders.NONCE);
            if (nonce == null || nonce.length() == 0) {
                throw new HttpUnauthorizedException("Request header missing: " + HttpHeaders.NONCE);
            }

            String contentMd5InRequest;
            String contentMd5Header = request.getHeader(HttpHeaders.CONTENT_MD5);

            if (!StringUtils.isNullOrEmpty(content)) {
                if (contentMd5Header == null || contentMd5Header.length() == 0) {
                    throw new HttpUnauthorizedException("Request header missing: " + HttpHeaders.CONTENT_MD5);
                }

                contentMd5InRequest = new String(Base64.getDecoder().decode(contentMd5Header));

                String generatedContentMd5 = new Md5Digest().generate(content);
                if (!contentMd5InRequest.equals(generatedContentMd5)) {
                    // the content has been tampered with?
                    throw new HttpUnauthorizedException("MD5 hash doesn't match content");
                }
            } else {
                contentMd5InRequest = "d41d8cd98f00b204e9800998ecf8427e"; // this is the MD5 hash of an empty string
            }

            String apiSecret = workspaceMetadata.getApiSecret();
            HashBasedMessageAuthenticationCode code = new HashBasedMessageAuthenticationCode(apiSecret);
            String hmacInRequest = hmacAuthorizationHeader.getHmac();
            HmacContent hmacContent = new HmacContent(httpMethod, path, contentMd5InRequest, contentType, nonce);
            String generatedHmac = code.generate(hmacContent.toString());
            if (!hmacInRequest.equals(generatedHmac)) {
                throw new HttpUnauthorizedException("Authorization header doesn't match");
            }
        } catch (Exception e) {
            log.error(e);
            throw new HttpUnauthorizedException(e.getMessage());
        }
    }

    @ExceptionHandler(HttpUnauthorizedException.class)
    @ResponseBody
    public ApiResponse handleCustomException(HttpUnauthorizedException exception, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return new ApiResponse(exception);
    }

    @ExceptionHandler(ApiException.class)
    @ResponseBody
    public ApiResponse handleCustomException(ApiException exception, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return new ApiResponse(exception);
    }

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ApiResponse error(Throwable t, HttpServletResponse response) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        t.printStackTrace();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return new ApiResponse(false, t.getMessage());
    }

}