package com.structurizr.server.web.home;

import com.structurizr.server.domain.WorkspaceMetadata;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PaginatedWorkspaceListTests {

    private PaginatedWorkspaceList list;

    @Test
    void numberOfWorkspacesIsLessThanPageSize() {
        WorkspaceMetadata wmd1 = new WorkspaceMetadata(1);
        WorkspaceMetadata wmd2 = new WorkspaceMetadata(2);
        WorkspaceMetadata wmd3 = new WorkspaceMetadata(3);

        list = new PaginatedWorkspaceList(List.of(wmd1, wmd2, wmd3), 1, 5);
        assertEquals(3, list.getPageSize()); // page size has been modified
        assertEquals(1, list.getPageNumber());
        assertEquals(1, list.getMaxPage());
        assertFalse(list.hasPreviousPage());
        assertFalse(list.hasNextPage());
        assertEquals(3, list.getWorkspaces().size());
    }

    @Test
    void numberOfWorkspacesIsEqualToPageSize() {
        WorkspaceMetadata wmd1 = new WorkspaceMetadata(1);
        WorkspaceMetadata wmd2 = new WorkspaceMetadata(2);
        WorkspaceMetadata wmd3 = new WorkspaceMetadata(3);

        list = new PaginatedWorkspaceList(List.of(wmd1, wmd2, wmd3), 1, 3);
        assertEquals(3, list.getPageSize());
        assertEquals(1, list.getPageNumber());
        assertEquals(1, list.getMaxPage());
        assertFalse(list.hasPreviousPage());
        assertFalse(list.hasNextPage());
        assertEquals(3, list.getWorkspaces().size());
    }

    @Test
    void numberOfWorkspacesIsGreaterThanPageSize() {
        WorkspaceMetadata wmd1 = new WorkspaceMetadata(1);
        WorkspaceMetadata wmd2 = new WorkspaceMetadata(2);
        WorkspaceMetadata wmd3 = new WorkspaceMetadata(3);

        list = new PaginatedWorkspaceList(List.of(wmd1, wmd2, wmd3), 1, 2);
        assertEquals(2, list.getPageSize());
        assertEquals(1, list.getPageNumber());
        assertEquals(2, list.getMaxPage());
        assertFalse(list.hasPreviousPage());
        assertTrue(list.hasNextPage());
        assertEquals(2, list.getWorkspaces().size());
        assertTrue(list.getWorkspaces().contains(wmd1));
        assertTrue(list.getWorkspaces().contains(wmd2));

        list = new PaginatedWorkspaceList(List.of(wmd1, wmd2, wmd3), 2, 2);
        assertEquals(2, list.getPageSize());
        assertEquals(2, list.getPageNumber());
        assertEquals(2, list.getMaxPage());
        assertTrue(list.hasPreviousPage());
        assertFalse(list.hasNextPage());
        assertEquals(1, list.getWorkspaces().size());
        assertTrue(list.getWorkspaces().contains(wmd3));
    }

    @Test
    void pageNumberLessThanOne() {
        list = new PaginatedWorkspaceList(listOf(3), 0, 3);
        assertEquals(1, list.getPageNumber());
    }

    @Test
    void pageSizeLessThanOne() {
        list = new PaginatedWorkspaceList(listOf(20), 1, 0);
        assertEquals(10, list.getPageSize()); // default page size

        list = new PaginatedWorkspaceList(listOf(5), 1, 0);
        assertEquals(5, list.getPageSize()); // default page size of 10 is less than the number of elements, so default to that instead
    }

    @Test
    void requestedPageNumberIsTooHigh() {
        list = new PaginatedWorkspaceList(listOf(20), 3, 10);
        assertEquals(2, list.getPageNumber());
    }

    private List<WorkspaceMetadata> listOf(int size) {
        List<WorkspaceMetadata> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(new WorkspaceMetadata(i));
        }

        return list;
    }

}