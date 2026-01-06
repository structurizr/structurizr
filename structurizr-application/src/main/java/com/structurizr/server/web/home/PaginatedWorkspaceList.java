package com.structurizr.server.web.home;

import com.structurizr.server.domain.WorkspaceMetadata;

import java.util.List;
final
class PaginatedWorkspaceList {

    static final int DEFAULT_PAGE_SIZE = 10;

    private final List<WorkspaceMetadata> workspaces;
    private final int pageNumber;
    private final int pageSize;
    private int start;
    private int end;

    PaginatedWorkspaceList(List<WorkspaceMetadata> workspaces, int pageNumber, int pageSize) {
        this.workspaces = workspaces;

        if (pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        this.pageSize = Math.min(pageSize, workspaces.size());

        pageNumber = Math.max(1, pageNumber);
        this.pageNumber = Math.min(getMaxPage(), pageNumber);

        start = (pageNumber - 1) * pageSize;
        start = Math.min(start, workspaces.size() - 1);

        end = start + pageSize;
        end = Math.min(end, workspaces.size());
    }

    int getPageNumber() {
        return pageNumber;
    }

    int getPageSize() {
        return pageSize;
    }

    boolean hasPreviousPage() {
        return start > 0;
    }

    boolean hasNextPage() {
        return end < workspaces.size();
    }

    List<WorkspaceMetadata> getWorkspaces() {
        return workspaces.subList(start, end);
    }

    public int getMaxPage() {
        return (int)Math.ceil((double)workspaces.size() / pageSize);
    }

}