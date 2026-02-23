package com.structurizr.server.component.workspace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WorkspaceTooLargeExceptionTests {

    @Test
    void message() {
        Exception exception = new WorkspaceTooLargeException(1234, (long)(2.01 * 1024 * 1024), (long)(2.00 * 1024 * 1024));
        assertEquals("Workspace 1234 is 2.010 MB, which exceeds the maximum size of 2.000 MB", exception.getMessage());
    }

}