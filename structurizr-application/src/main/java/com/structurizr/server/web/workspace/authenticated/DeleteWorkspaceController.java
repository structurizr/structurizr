package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.component.search.SearchComponent;
import com.structurizr.server.component.search.SearchComponentException;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.configuration.Configuration;
import com.structurizr.server.domain.User;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.workspace.AbstractWorkspaceController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@Profile("command-server")
public class DeleteWorkspaceController extends AbstractWorkspaceController {

    private static final Log log = LogFactory.getLog(DeleteWorkspaceController.class);

    private SearchComponent searchComponent;

    @Autowired
    public void setSearchComponent(SearchComponent searchComponent) {
        this.searchComponent = searchComponent;
    }

    @RequestMapping(value="/workspace/{workspaceId}/delete", method = RequestMethod.POST)
    String deleteWorkspace(@PathVariable("workspaceId")long workspaceId, ModelMap model) {
        User user = getUser();

        try {
            WorkspaceMetaData workspace = workspaceComponent.getWorkspaceMetaData(workspaceId);
            if (workspace != null) {
                if (!Configuration.getInstance().isAuthenticationEnabled() || Configuration.getInstance().getAdminUsersAndRoles().isEmpty() || user.isAdmin()) {
                    if (workspaceComponent.deleteWorkspace(workspaceId)) {
                        try {
                            searchComponent.delete(workspaceId);
                        } catch (SearchComponentException e) {
                            log.error(e);
                        }
                    }
                }
            } else {
                return show404Page(model);
            }
        } catch (WorkspaceComponentException e) {
            log.error(e);
        }

        return "redirect:/";
    }

}