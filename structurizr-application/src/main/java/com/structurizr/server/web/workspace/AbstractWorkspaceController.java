package com.structurizr.server.web.workspace;

import com.structurizr.configuration.Profile;
import com.structurizr.server.component.search.SearchComponent;
import com.structurizr.server.component.workspace.WorkspaceBranch;
import com.structurizr.server.component.workspace.WorkspaceComponent;
import com.structurizr.server.component.workspace.WorkspaceComponentException;
import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.server.domain.User;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.AbstractController;
import com.structurizr.util.HtmlUtils;
import com.structurizr.util.JsonUtils;
import com.structurizr.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;

import static com.structurizr.configuration.StructurizrProperties.AUTO_REFRESH_INTERVAL_PROPERTY;

/**
 * Base class for all controllers underneath /share and /workspace (i.e. the workspace related controllers).
 */
public abstract class AbstractWorkspaceController extends AbstractController {

    private static final Log log = LogFactory.getLog(AbstractWorkspaceController.class);

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

    protected final String showView(String view, WorkspaceMetaData workspaceMetaData, String branch, String version, ModelMap model, boolean editable, boolean showHeaderAndFooter) {
        try {
            if (editable) {
                workspaceMetaData.setEditable(true);

                if (Configuration.getInstance().getProfile() == Profile.Server) {
                    if (workspaceMetaData.isPublicWorkspace() || workspaceMetaData.hasNoUsersConfigured()) {
                        model.addAttribute("sharingUrlPrefix", "/share/" + workspaceMetaData.getId());
                    }
                }
            } else {
                workspaceMetaData.setEditable(false);
                String json = workspaceComponent.getWorkspace(workspaceMetaData.getId(), branch, version);
                json = json.replaceAll("[\\n\\r\\f]", "");
                model.addAttribute("workspaceAsJson", JsonUtils.base64(json));
            }

            addCommonAttributes(model, workspaceMetaData.getName(), showHeaderAndFooter);

            workspaceMetaData.setBranch(branch);
            workspaceMetaData.setInternalVersion(version);
            model.addAttribute("workspace", workspaceMetaData);
            model.addAttribute("showToolbar", true);
            model.addAttribute("embed", false);

            if (isAuthenticated()) {
                model.addAttribute("user", getUser());
            }

            return view;
        } catch (Exception e) {
            log.error(e);
            return "500";
        }
    }

    protected final void enableLocalRefresh(ModelMap model) {
        model.addAttribute("autoRefreshInterval", Integer.parseInt(Configuration.getInstance().getProperty(AUTO_REFRESH_INTERVAL_PROPERTY)));
        model.addAttribute("autoRefreshLastModifiedDate", workspaceComponent.getLastModifiedDate());
    }

}