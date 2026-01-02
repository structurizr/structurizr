package com.structurizr.server.web.api;

import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.server.component.workspace.WorkspaceVersion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.*;

/**
 * An implementation of the Structurizr workspace API.
 *
 *  - GET /api/workspace/{id}
 *  - PUT /api/workspace/{id}
 */
@RestController
@org.springframework.context.annotation.Profile("command-local")
public class LocalWorkspaceApiController extends AbstractWorkspaceApiController {

    protected static final Log log = LogFactory.getLog(LocalWorkspaceApiController.class);

    @CrossOrigin
    @RequestMapping(value = "/api/workspace/{workspaceId}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public String getWorkspace(@PathVariable("workspaceId") long workspaceId,
                               HttpServletRequest request, HttpServletResponse response) {

        return get(workspaceId, WorkspaceBranch.MAIN_BRANCH, WorkspaceVersion.LATEST_VERSION, request, response);
    }

    @CrossOrigin
    @RequestMapping(value = "/api/workspace/{workspaceId}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json; charset=UTF-8")
    public @ResponseBody ApiResponse putWorkspace(@PathVariable("workspaceId")long workspaceId,
                                                  @RequestBody String json,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) {

        return put(workspaceId, WorkspaceBranch.MAIN_BRANCH, json, request, response);
    }

}