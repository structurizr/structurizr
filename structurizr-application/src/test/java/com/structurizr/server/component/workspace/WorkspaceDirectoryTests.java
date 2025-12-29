package com.structurizr.server.component.workspace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WorkspaceDirectoryTests {

    @Test
    void parseWorkspaceId() {
        assertEquals(0, WorkspaceDirectory.parseWorkspaceId("abc"));
        assertEquals(0, WorkspaceDirectory.parseWorkspaceId("0"));
        assertEquals(0, WorkspaceDirectory.parseWorkspaceId("0-system-landscape"));

        assertEquals(1, WorkspaceDirectory.parseWorkspaceId("1"));
        assertEquals(1, WorkspaceDirectory.parseWorkspaceId("001"));
        assertEquals(1, WorkspaceDirectory.parseWorkspaceId("001-system-landscape"));
    }

}