package com.structurizr.server.web.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    @RequestMapping(value = "/api/workspace/{workspaceId}/branch", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public String getBranches(@PathVariable("workspaceId") long workspaceId,
                              HttpServletRequest request, HttpServletResponse response) {
        try {
            if (!Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_BRANCHES)) {
                throw new ApiException("Workspace branches are not enabled for this installation");
            }

            if (workspaceId > 0) {
                authoriseRequest(workspaceId, "GET", getPath(request, workspaceId, null) + "/branch", null, request, response);

                List<WorkspaceBranch> branches = workspaceComponent.getWorkspaceBranches(workspaceId);

                branches = new ArrayList<>(branches);
                branches.sort(Comparator.comparing(WorkspaceBranch::getName));
                String[] array = branches.stream().map(WorkspaceBranch::getName).toArray(String[]::new);

                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    return objectMapper.writeValueAsString(array);
                } catch (JsonProcessingException e) {
                    log.error(e);
                    throw new ApiException("Could not get workspace branches");
                }
            } else {
                throw new ApiException("Workspace ID must be greater than 1");
            }
        } catch (WorkspaceComponentException e) {
            log.error(e);
            throw new ApiException("Could not get workspace branches");
        }
    }

    @RequestMapping(value = "/api/workspace/{workspaceId}/branch/{branch}", method = RequestMethod.DELETE, produces = "application/json; charset=UTF-8")
    public @ResponseBody ApiResponse deleteBranch(@PathVariable("workspaceId") long workspaceId,
                               @PathVariable("branch") String branch,
                              HttpServletRequest request, HttpServletResponse response) {

        try {
            if (!Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_BRANCHES)) {
                throw new ApiException("Workspace branches are not enabled for this installation");
            }

            if (workspaceId > 0) {
                authoriseRequest(workspaceId, "DELETE", getPath(request, workspaceId, null) + "/branch/" + branch, null, request, response);

                if (WorkspaceBranch.isMainBranch(branch)) {
                    throw new ApiException("The main branch cannot be deleted");
                }

                List<WorkspaceBranch> branches = workspaceComponent.getWorkspaceBranches(workspaceId);

                if (branches.stream().anyMatch(b -> b.getName().equals(branch))) {
                    boolean success = workspaceComponent.deleteBranch(workspaceId, branch);
                    return new ApiResponse(success);
                } else {
                    return new ApiResponse(false, "Workspace branch \"" + branch + "\" does not exist");
                }
            } else {
                throw new ApiException("Workspace ID must be greater than 1");
            }
        } catch (WorkspaceComponentException e) {
            log.error(e);
        }

        throw new ApiException("Could not delete workspace branch");
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