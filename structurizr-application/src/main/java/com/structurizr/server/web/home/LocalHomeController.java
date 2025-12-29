package com.structurizr.server.web.home;

import com.structurizr.configuration.Configuration;
import com.structurizr.server.domain.WorkspaceMetaData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@org.springframework.context.annotation.Profile("command-local")
final class LocalHomeController extends AbstractHomeController {

    @RequestMapping(value = { "/" }, method = RequestMethod.GET)
    String showHomePage(
            @RequestParam(required = false, defaultValue = SORT_NAME) String sort,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGE_NUMBER) int pageNumber,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            ModelMap model) {

        if (Configuration.getInstance().isSingleWorkspace()) {
            return "redirect:/workspace/1";
        } else {
            List<WorkspaceMetaData> workspaces = workspaceComponent.getWorkspaces();
            sort = determineSort(sort);
            workspaces = sortAndPaginate(new ArrayList<>(workspaces), sort, pageNumber, pageSize, model);

            model.addAttribute("workspaces", workspaces);
            model.addAttribute("numberOfWorkspaces", workspaces.size());

            model.addAttribute("sort", sort);
            addCommonAttributes(model, "", true);

            return "home";
        }
    }

}