package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.server.component.workspace.WorkspaceLockResponse;
import com.structurizr.server.domain.Permission;
import com.structurizr.server.domain.User;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.util.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Set;

@Controller
@Profile("command-server")
public class LockController extends AbstractWorkspaceController {

    private static final Log log = LogFactory.getLog(LockController.class);

    @RequestMapping(value = "/workspace/{workspaceId}/lock", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    WorkspaceLockResponse lockWorkspace(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = true) String agent
    ) {
        WorkspaceMetadata workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
        if (workspaceMetadata == null) {
            return new WorkspaceLockResponse(false, false);
        }

        User user = getUser();

        if (!workspaceMetadata.isLocked() || workspaceMetadata.isLockedBy(user.getUsername(), agent)) {
            try {
                if (workspaceComponent.lockWorkspace(workspaceId, user.getUsername(), agent)) {
                    return new WorkspaceLockResponse(true, true);
                }
            } catch (WorkspaceComponentException e) {
                log.error(e);
            }
        }

        // not locked - refresh the metadata and try to figure out why
        workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
        if (workspaceMetadata.isLocked() && !workspaceMetadata.isLockedBy(user.getUsername(), agent)) {
            // locked by somebody else
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.USER_FRIENDLY_DATE_FORMAT);

            return new WorkspaceLockResponse(false, true, "The workspace could not be locked; it was locked by " + workspaceMetadata.getLockedUser() + " using " + workspaceMetadata.getLockedAgent() + " at " + sdf.format(workspaceMetadata.getLockedDate()) + ".");
        } else {
            // other problem
            return new WorkspaceLockResponse(false, workspaceMetadata.isLocked(), "The workspace could not be locked - please save your workspace and refresh the page.");
        }
    }

    @RequestMapping(value = "/workspace/{workspaceId}/unlock", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    WorkspaceLockResponse unlockWorkspace(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = true) String agent
    ) {
        WorkspaceMetadata workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
        if (workspaceMetadata == null) {
            return new WorkspaceLockResponse(false, false);
        }

        User user = getUser();

        if (workspaceMetadata.isLockedBy(user.getUsername(), agent)) {
            workspaceComponent.unlockWorkspace(workspaceId);
            return new WorkspaceLockResponse(true, false);
        }

        return new WorkspaceLockResponse(false, workspaceMetadata.isLocked());
    }

    @RequestMapping(value = "/workspace/{workspaceId}/unlock", method = RequestMethod.GET)
    String unlockWorkspace(@PathVariable("workspaceId") long workspaceId, ModelMap model) {
        WorkspaceMetadata workspaceMetadata = workspaceComponent.getWorkspaceMetadata(workspaceId);
        if (workspaceMetadata == null) {
            return show404Page(model);
        }

        User user = getUser();
        Set<Permission> permissions = workspaceMetadata.getPermissions(user);
        if (permissions.contains(Permission.Write)) {
            try {
                workspaceComponent.unlockWorkspace(workspaceId);
            } catch (WorkspaceComponentException e) {
                log.error(e);
            }
        } else {
            return show404Page(model);
        }

        return "redirect:/workspace/" + workspaceId;
    }

}