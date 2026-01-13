package com.structurizr.server.component.workspace;

import com.structurizr.Workspace;
import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Features;
import com.structurizr.validation.WorkspaceScopeValidationException;
import com.structurizr.validation.WorkspaceScopeValidatorFactory;

public class WorkspaceValidationUtils {

    public static void validateWorkspaceScope(Workspace workspace) throws WorkspaceScopeValidationException {
        // if workspace scope validation is enabled, reject workspaces without a defined scope
        if (Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_SCOPE_VALIDATION)) {
            if (workspace.getConfiguration().getScope() == null) {
                throw new WorkspaceScopeValidationException("Strict workspace scope validation has been enabled for this installation. Unscoped workspaces are not permitted - see https://docs.structurizr.com/basics/workspace-scope for more information.");
            }
        }

        // validate workspace scope
        WorkspaceScopeValidatorFactory.getValidator(workspace).validate(workspace);
    }

}
