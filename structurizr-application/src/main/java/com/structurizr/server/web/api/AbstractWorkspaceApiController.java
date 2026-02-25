package com.structurizr.server.web.api;

import com.structurizr.Workspace;
import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
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
                               String apiKey) {
        try {
            if (WorkspaceBranch.isMainBranch(branch)) {
                branch = "";
            }

            try {
                WorkspaceBranch.validateBranchName(branch);
            } catch (IllegalArgumentException e) {
                throw new ApiException(e.getMessage());
            }

            authoriseRequest(workspaceId, Permission.Read, apiKey);

            if (!StringUtils.isNullOrEmpty(branch) && !Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_BRANCHES)) {
                throw new ApiException("Workspace branches are not enabled for this installation");
            }

            return workspaceComponent.getWorkspace(workspaceId, branch, version);
        } catch (WorkspaceComponentException e) {
            log.error(e);
            throw new ApiException(e.getMessage());
        }
    }

    public ApiResponse put(long workspaceId,
                          String branch,
                          String json,
                          String apiKey) {
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
                authoriseRequest(workspaceId, Permission.Write, apiKey);

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

    protected final void authoriseRequest(long workspaceId, Permission requiredPermission, String apiKey) {
        if (workspaceId < 1) {
            throw new ApiException("Workspace ID must be greater than 1");
        }

        WorkspaceMetadata workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
        if (workspaceMetadata == null) {
            throw new ApiException("404");
        }

        User user = getUser();
        if (user != null && !workspaceMetadata.getPermissions(user).isEmpty()) {
            if (!workspaceMetadata.getPermissions(user).contains(requiredPermission)) {
                throw new HttpUnauthorizedException("Missing permission " + requiredPermission);
            }
        } else {
            if (StringUtils.isNullOrEmpty(apiKey)) {
                throw new HttpUnauthorizedException("API key must be provided");
            }

            if (workspaceMetadata.isApiKeyValid(apiKey)) {
                return;
            }

            throw new HttpUnauthorizedException("Incorrect API key");
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