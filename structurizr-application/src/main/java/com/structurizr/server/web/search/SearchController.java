package com.structurizr.server.web.search;

import com.structurizr.configuration.Features;
import com.structurizr.configuration.Profile;
import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.server.component.search.SearchComponent;
import com.structurizr.server.component.search.SearchComponentException;
import com.structurizr.server.component.search.SearchResult;
import com.structurizr.configuration.Configuration;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.workspace.AbstractWorkspaceController;
import com.structurizr.util.HtmlUtils;
import com.structurizr.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
final class SearchController extends AbstractWorkspaceController {

    private static final Log log = LogFactory.getLog(SearchController.class);

    @Autowired
    private SearchComponent searchComponent;

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    String search(ModelMap model,
                         @RequestParam(required = false) String query,
                         @RequestParam(required = false) Long workspaceId,
                         @RequestParam(required = false) String category) {

        if (Configuration.getInstance().getProperty(StructurizrProperties.SEARCH_IMPLEMENTATION).equals(StructurizrProperties.SEARCH_VARIANT_NONE)) {
            return showFeatureNotAvailablePage(model);
        }

        model.addAttribute("searchBaseUrl", "/");

        List<SearchResult> filteredSearchResults = new ArrayList<>();

        if (query != null) {
            query = HtmlUtils.filterHtml(query);
            query = query.replaceAll("\"", "");
        }

        if (category != null) {
            category = HtmlUtils.filterHtml(category);
            category = category.replaceAll("\"", "");
            category = category.toLowerCase();
        }

        List<WorkspaceMetadata> workspaces;

        if (Configuration.getInstance().getProfile() == Profile.Local) {
            workspaces = workspaceComponent.getWorkspaces();
        } else {
            workspaces = workspaceComponent.getWorkspaces(getUser());
        }

        if (!workspaces.isEmpty()) {
            Map<Long, WorkspaceMetadata> workspacesById = new HashMap<>();

            if (workspaceId == null) {
                for (WorkspaceMetadata workspace : workspaces) {
                    workspacesById.put(workspace.getId(), workspace);
                }
            } else {
                workspaces.stream().filter(w -> w.getId() == workspaceId).findFirst().ifPresent(w -> workspacesById.put(w.getId(), w));
            }

            if (!StringUtils.isNullOrEmpty(query)) {
                List<SearchResult> searchResults = new ArrayList<>();

                try {
                    searchResults = searchComponent.search(query, category, workspacesById.keySet());
                } catch (SearchComponentException e) {
                    log.error(e);
                }

                for (SearchResult searchResult : searchResults) {
                    if (workspacesById.containsKey(searchResult.getWorkspaceId())) {
                        searchResult.
                                setWorkspace(workspacesById.get(searchResult.getWorkspaceId()));
                        filteredSearchResults.add(searchResult);
                    }
                }
            }
        }

        model.addAttribute("query", query);
        model.addAttribute("workspaceId", workspaceId);
        model.addAttribute("category", category);
        model.addAttribute("results", filteredSearchResults);
        addCommonAttributes(model, "Search", true);

        return "search-results";
    }

}