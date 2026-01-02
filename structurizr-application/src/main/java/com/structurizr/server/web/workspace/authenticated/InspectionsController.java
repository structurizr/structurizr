package com.structurizr.server.web.workspace.authenticated;

import com.structurizr.Workspace;
import com.structurizr.configuration.Configuration;
import com.structurizr.inspection.DefaultInspector;
import com.structurizr.inspection.Inspector;
import com.structurizr.inspection.Severity;
import com.structurizr.inspection.Violation;
import com.structurizr.server.web.Views;
import com.structurizr.util.WorkspaceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;

@Controller
class InspectionsController extends AbstractWorkspaceController {

    private static final Log log = LogFactory.getLog(InspectionsController.class);

    @RequestMapping(value = "/workspace/{workspaceId}/inspections", method = RequestMethod.GET)
    public String showAuthenticatedInspections(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        if (Configuration.getInstance().getProfile() == com.structurizr.configuration.Profile.Local) {
            enableLocalRefresh(model);
        }

        return showAuthenticatedView(
                Views.INSPECTIONS, workspaceId,
                workspaceMetaData -> {
                    try {
                        String json = workspaceComponent.getWorkspace(workspaceId, branch, version);
                        Workspace workspace = WorkspaceUtils.fromJson(json);
                        Inspector inspector = new DefaultInspector(workspace);

                        List<Violation> violations = inspector.getViolations();
                        violations.sort(Comparator.comparing(Violation::getSeverity));

                        model.addAttribute("violations", violations);
                        model.addAttribute("numberOfInspections", inspector.getNumberOfInspections());
                        model.addAttribute("numberOfViolations", violations.size());
                        model.addAttribute("numberOfErrors", (int)violations.stream().filter(r -> r.getSeverity() == Severity.ERROR).count());
                        model.addAttribute("numberOfWarnings", (int)violations.stream().filter(r -> r.getSeverity() == Severity.WARNING).count());
                        model.addAttribute("numberOfInfos", (int)violations.stream().filter(r -> r.getSeverity() == Severity.INFO).count());
                        model.addAttribute("numberOfIgnores", (int)violations.stream().filter(r -> r.getSeverity() == Severity.IGNORE).count());
                    } catch (Exception e) {
                        log.error(e);
                        throw new RuntimeException(e);
                    }
                },
                branch, version, model, true, false
        );

    }

}