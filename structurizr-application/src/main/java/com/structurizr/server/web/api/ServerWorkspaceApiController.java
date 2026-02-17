package com.structurizr.server.web.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurizr.api.HttpHeaders;
import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.domain.Permission;
import com.structurizr.server.domain.User;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.util.DateUtils;
import com.structurizr.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
                               @RequestHeader(name = HttpHeaders.X_AUTHORIZATION, required = false) String apiKey) {

        return get(workspaceId, WorkspaceBranch.MAIN_BRANCH, version, apiKey);
    }

    @CrossOrigin
    @RequestMapping(value = "/api/workspace/{workspaceId}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json; charset=UTF-8")
    public @ResponseBody ApiResponse putWorkspace(@PathVariable("workspaceId")long workspaceId,
                                                  @RequestBody String json,
                                                  @RequestHeader(name = HttpHeaders.X_AUTHORIZATION, required = false) String apiKey) {

        return put(workspaceId, WorkspaceBranch.MAIN_BRANCH, json, apiKey);
    }

    @CrossOrigin
    @RequestMapping(value = "/api/workspace/{workspaceId}/branch/{branch}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public String getWorkspace(@PathVariable("workspaceId") long workspaceId,
                               @PathVariable("branch") String branch,
                               @RequestParam(required = false) String version,
                               @RequestHeader(name = HttpHeaders.X_AUTHORIZATION, required = false) String apiKey) {
        return get(workspaceId, branch, version, apiKey);
    }

    @CrossOrigin
    @RequestMapping(value = "/api/workspace/{workspaceId}/branch/{branch}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json; charset=UTF-8")
    public @ResponseBody ApiResponse putWorkspace(@PathVariable("workspaceId")long workspaceId,
                                                  @PathVariable("branch") String branch,
                                                  @RequestBody String json,
                                                  @RequestHeader(name = HttpHeaders.X_AUTHORIZATION, required = false) String apiKey) {
        return put(workspaceId, branch, json, apiKey);
    }

    @RequestMapping(value = "/api/workspace/{workspaceId}/branch", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public String getBranches(@PathVariable("workspaceId") long workspaceId,
                              @RequestHeader(name = HttpHeaders.X_AUTHORIZATION, required = false) String apiKey) {

        if (!Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_BRANCHES)) {
            throw new ApiException("Workspace branches are not enabled for this installation");
        }

        authoriseRequest(workspaceId, Permission.Read, apiKey);

        try {
            List<WorkspaceBranch> branches = workspaceComponent.getWorkspaceBranches(workspaceId);
            branches = new ArrayList<>(branches);
            branches.sort(Comparator.comparing(WorkspaceBranch::getName));

            WorkspaceBranchesApiResponse apiResponse = new WorkspaceBranchesApiResponse(workspaceId, branches.stream().map(WorkspaceBranch::getName).toList());

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.writeValueAsString(apiResponse);
            } catch (JsonProcessingException e) {
                log.error(e);
                throw new ApiException("Could not get workspace branches");
            }
        } catch (WorkspaceComponentException e) {
            log.error(e);
            throw new ApiException("Could not get workspace branches");
        }
    }

    @RequestMapping(value = "/api/workspace/{workspaceId}/branch/{branch}", method = RequestMethod.DELETE, produces = "application/json; charset=UTF-8")
    public @ResponseBody ApiResponse deleteBranch(@PathVariable("workspaceId") long workspaceId,
                               @PathVariable("branch") String branch,
                               @RequestHeader(name = HttpHeaders.X_AUTHORIZATION, required = false) String apiKey) {

        if (!Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_BRANCHES)) {
            throw new ApiException("Workspace branches are not enabled for this installation");
        }

        authoriseRequest(workspaceId, Permission.Write, apiKey);

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
    }

    @RequestMapping(value = "/api/workspace/{workspaceId}/lock", method = RequestMethod.PUT, produces = "application/json; charset=UTF-8")
    public @ResponseBody ApiResponse lockWorkspace(@PathVariable("workspaceId") long workspaceId,
                                                   @RequestParam(required = false) String user,
                                                   @RequestParam(required = true) String agent,
                                                   @RequestHeader(name = HttpHeaders.X_AUTHORIZATION, required = false) String apiKey) {

        User u = getUser();
        if (StringUtils.isNullOrEmpty(user) && u != null) {
            user = u.getUsername();
        }

        authoriseRequest(workspaceId, Permission.Write, apiKey);

        if (workspaceComponent.lockWorkspace(workspaceId, user, agent)) {
            return new ApiResponse("OK");
        } else {
            WorkspaceMetadata workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.USER_FRIENDLY_DATE_FORMAT);

            return new ApiResponse(false, "The workspace could not be locked; it was locked by " + workspaceMetadata.getLockedUser() + " using " + workspaceMetadata.getLockedAgent() + " at " + sdf.format(workspaceMetadata.getLockedDate()));
        }
    }

    @RequestMapping(value = "/api/workspace/{workspaceId}/lock", method = RequestMethod.DELETE, produces = "application/json; charset=UTF-8")
    public @ResponseBody ApiResponse unlockWorkspace(@PathVariable("workspaceId") long workspaceId,
                                                     @RequestParam(required = false) String user,
                                                     @RequestParam(required = true) String agent,
                                                     @RequestHeader(name = HttpHeaders.X_AUTHORIZATION, required = false) String apiKey) {

        User u = getUser();
        if (StringUtils.isNullOrEmpty(user)) {
            user = u.getUsername();
        }

        authoriseRequest(workspaceId, Permission.Write, apiKey);

        WorkspaceMetadata workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
        if (workspaceMetadata.isLockedBy(user, agent)) {
            if (workspaceComponent.unlockWorkspace(workspaceId)) {
                return new ApiResponse("OK");
            }
        }

        return new ApiResponse(false, "Could not unlock workspace");
    }

}