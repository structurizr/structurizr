package com.structurizr.server.web.home;

import com.structurizr.server.component.workspace.WorkspaceComponent;
import com.structurizr.server.domain.WorkspaceMetaData;
import com.structurizr.server.web.AbstractController;
import com.structurizr.util.HtmlUtils;
import com.structurizr.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;

import java.util.Comparator;
import java.util.List;

class AbstractHomeController extends AbstractController {

    protected static final String SORT_DATE = "date";
    protected static final String SORT_NAME = "name";

    protected static final String DEFAULT_PAGE_NUMBER = "1";
    protected static final String DEFAULT_PAGE_SIZE = "" + PaginatedWorkspaceList.DEFAULT_PAGE_SIZE;

    protected WorkspaceComponent workspaceComponent;

    @Autowired
    public void setWorkspaceComponent(WorkspaceComponent workspaceComponent) {
        this.workspaceComponent = workspaceComponent;
    }

    protected List<WorkspaceMetaData> sortAndPaginate(List<WorkspaceMetaData> workspaces, String sort, int pageNumber, int pageSize, ModelMap model) {
        if (SORT_DATE.equals(sort)) {
            workspaces.sort((wmd1, wmd2) -> wmd2.getLastModifiedDate().compareTo(wmd1.getLastModifiedDate()));
        } else {
            workspaces.sort(Comparator.comparing(wmd -> wmd.getName().toLowerCase()));
        }

        if (workspaces.isEmpty() || pageSize >= workspaces.size()) {
            return workspaces;
        } else {
            PaginatedWorkspaceList paginatedWorkspaceList = new PaginatedWorkspaceList(workspaces, pageNumber, pageSize);

            model.addAttribute("pageNumber", paginatedWorkspaceList.getPageNumber());
            if (paginatedWorkspaceList.hasPreviousPage()) {
                model.addAttribute("previousPage", pageNumber - 1);
            }
            if (paginatedWorkspaceList.hasNextPage()) {
                model.addAttribute("nextPage", pageNumber + 1);
            }

            model.addAttribute("maxPage", paginatedWorkspaceList.getMaxPage());
            model.addAttribute("pageSize", paginatedWorkspaceList.getPageSize());

            return paginatedWorkspaceList.getWorkspaces();
        }
    }

    protected String determineSort(String sort) {
        sort = HtmlUtils.filterHtml(sort);

        if (!StringUtils.isNullOrEmpty(sort) && sort.trim().equals(SORT_DATE)) {
            sort = SORT_DATE;
        } else {
            sort = SORT_NAME;
        }

        return sort;
    }

}