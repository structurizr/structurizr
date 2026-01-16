package com.structurizr.server.web.api;

import com.structurizr.Workspace;
import com.structurizr.api.HttpHeaders;
import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.io.WorkspaceReaderException;
import com.structurizr.io.json.JsonReader;
import com.structurizr.server.component.search.SearchComponent;
import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.server.component.workspace.WorkspaceComponent;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.Permission;
import com.structurizr.server.domain.User;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.AbstractController;
import com.structurizr.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.StringReader;

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
                WorkspaceMetadata workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
                if (workspaceMetadata == null) {
                    throw new ApiException("404");
                }

                User user = getUser();
                if (user != null && !workspaceMetadata.getPermissions(user).isEmpty()) {
                    authoriseRequest(workspaceMetadata, user, Permission.Read);
                } else {
                    authoriseRequest(workspaceMetadata, request);
                }

                if (!StringUtils.isNullOrEmpty(branch) && !Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_BRANCHES)) {
                    throw new ApiException("Workspace branches are not enabled for this installation");
                }

                return workspaceComponent.getWorkspace(workspaceId, branch, version);
            } else {
                throw new ApiException("Workspace ID must be greater than 1");
            }
        } catch (WorkspaceComponentException e) {
            log.error(e);
            throw new ApiException(e.getMessage());
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
                WorkspaceMetadata workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
                if (workspaceMetadata == null) {
                    throw new ApiException("404");
                }

                User user = getUser();
                if (user != null && !workspaceMetadata.getPermissions(user).isEmpty()) {
                    authoriseRequest(workspaceMetadata, user, Permission.Write);
                } else {
                    authoriseRequest(workspaceMetadata, request);
                }

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

    protected void authoriseRequest(WorkspaceMetadata workspaceMetadata, User user, Permission requiredPermission) throws WorkspaceComponentException {
        if (!workspaceMetadata.getPermissions(user).contains(requiredPermission)) {
            throw new HttpUnauthorizedException("Missing permission " + requiredPermission);
        }
    }

    protected void authoriseRequest(WorkspaceMetadata workspaceMetadata, HttpServletRequest request) throws WorkspaceComponentException {
        String authorizationHeaderAsString = request.getHeader(HttpHeaders.X_AUTHORIZATION);
        if (StringUtils.isNullOrEmpty(authorizationHeaderAsString)) {
            // fallback on the regular header
            authorizationHeaderAsString = request.getHeader(HttpHeaders.AUTHORIZATION);
        }

        if (StringUtils.isNullOrEmpty(authorizationHeaderAsString)) {
            throw new HttpUnauthorizedException("Authorization header must be provided");
        }

        String apiKeyFromAuthorizationHeader = authorizationHeaderAsString;
        String workspaceApiKey = workspaceMetadata.getApiKey();

        BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();

        if (bcryptEncoder.matches(apiKeyFromAuthorizationHeader, workspaceApiKey)) {
            // the given API key matches the bcrypt encoded workspace API key
            return;
        }

        if (apiKeyFromAuthorizationHeader.equals(workspaceApiKey)) {
            // the given API key matches the plaintext workspace API key (this is for backwards compatibility with existing workspace data)
            return;
        }

        String adminApiKey = Configuration.getInstance().getProperty(StructurizrProperties.API_KEY);
        if (bcryptEncoder.matches(apiKeyFromAuthorizationHeader, adminApiKey)) {
            // the given API key matches the bcrypt encoded admin API key
            return;
        }

        throw new HttpUnauthorizedException("Incorrect API key");
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