package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.server.component.workspace.WorkspaceComponent;
import com.structurizr.server.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("command-local")
public class WorkspaceLastModifiedController extends AbstractController {

    protected WorkspaceComponent workspaceComponent;

    @Autowired
    public void setWorkspaceComponent(WorkspaceComponent workspaceComponent) {
        this.workspaceComponent = workspaceComponent;
    }

    @RequestMapping(value = "/workspace/lastModified", method = RequestMethod.GET, produces = "plain/text; charset=UTF-8")
    @ResponseBody
    public String lastModified() {
        return "" + workspaceComponent.getLastModifiedDate();
    }

}