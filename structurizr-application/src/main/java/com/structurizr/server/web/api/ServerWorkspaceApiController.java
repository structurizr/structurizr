package com.structurizr.server.web.api;

import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetadata;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

/**
 * An implementation of the Structurizr workspace API.
 *
 *  - GET /api/workspace/{id}
 *  - PUT /api/workspace/{id}
 *  - GET /api/workspace/{id}/branch
 *  - PUT /api/workspace/{id}/branch
 *  - PUT /api/workspace/{id}/lock
 *  - DELETE /api/workspace/{id}/lock
 */
@RestController
@org.springframework.context.annotation.Profile("command-server")
public class ServerWorkspaceApiController extends AbstractWorkspaceApiController {

    @CrossOrigin
    @RequestMapping(value = "/api/workspace/{workspaceId}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public String getWorkspace(@PathVariable("workspaceId") long workspaceId,
                               @RequestParam(required = false) String version,
                               HttpServletRequest request, HttpServletResponse response) {

        return get(workspaceId, WorkspaceBranch.MAIN_BRANCH, version, request, response);
    }

    @CrossOrigin
    @RequestMapping(value = "/api/workspace/{workspaceId}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json; charset=UTF-8")
    public @ResponseBody ApiResponse putWorkspace(@PathVariable("workspaceId")long workspaceId,
                                                  @RequestBody String json,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) {

        return put(workspaceId, WorkspaceBranch.MAIN_BRANCH, json, request, response);
    }

    @CrossOrigin
    @RequestMapping(value = "/api/workspace/{workspaceId}/branch/{branch}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public String getWorkspace(@PathVariable("workspaceId") long workspaceId,
                               @PathVariable("branch") String branch,
                               @RequestParam(required = false) String version,
                               HttpServletRequest request, HttpServletResponse response) {
        return get(workspaceId, branch, version, request, response);
    }

    @CrossOrigin
    @RequestMapping(value = "/api/workspace/{workspaceId}/branch/{branch}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json; charset=UTF-8")
    public @ResponseBody ApiResponse putWorkspace(@PathVariable("workspaceId")long workspaceId,
                                                  @PathVariable("branch") String branch,
                                                  @RequestBody String json,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) {
        return put(workspaceId, branch, json, request, response);
    }

    @RequestMapping(value = "/api/workspace/{workspaceId}/lock", method = RequestMethod.PUT, produces = "application/json; charset=UTF-8")
    public @ResponseBody ApiResponse lockWorkspace(@PathVariable("workspaceId") long workspaceId,
                                                   @RequestParam(required = true) String user,
                                                   @RequestParam(required = true) String agent,
                                                   HttpServletRequest request, HttpServletResponse response) {
        try {
            if (workspaceId > 0) {
                authoriseRequest(workspaceId, "PUT", getPath(request, workspaceId, null) + "/lock?user=" + user + "&agent=" + agent, null, request, response);

                if (workspaceComponent.lockWorkspace(workspaceId, user, agent)) {
                    return new ApiResponse("OK");
                } else {
                    WorkspaceMetadata workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
                    return new ApiResponse(false, "The workspace is already locked by " + workspaceMetadata.getLockedUser() + " using " + workspaceMetadata.getLockedAgent() + ".");
                }
            } else {
                throw new ApiException("Workspace ID must be greater than 1");
            }
        } catch (WorkspaceComponentException e) {
            log.error(e);
            throw new ApiException("Could not lock workspace.");
        }
    }

    @RequestMapping(value = "/api/workspace/{workspaceId}/lock", method = RequestMethod.DELETE, produces = "application/json; charset=UTF-8")
    public @ResponseBody ApiResponse unlockWorkspace(@PathVariable("workspaceId") long workspaceId,
                                                     @RequestParam(required = true) String user,
                                                     @RequestParam(required = true) String agent,
                                                     HttpServletRequest request, HttpServletResponse response) {
        try {
            if (workspaceId > 0) {
                authoriseRequest(workspaceId, "DELETE", getPath(request, workspaceId, null) + "/lock?user=" + user + "&agent=" + agent, null, request, response);

                if (workspaceComponent.unlockWorkspace(workspaceId)) {
                    return new ApiResponse("OK");
                } else {
                    return new ApiResponse(false, "Could not unlock workspace.");
                }
            } else {
                throw new ApiException("Workspace ID must be greater than 1");
            }
        } catch (WorkspaceComponentException e) {
            log.error(e);
            throw new ApiException("Could not unlock workspace.");
        }
    }

}