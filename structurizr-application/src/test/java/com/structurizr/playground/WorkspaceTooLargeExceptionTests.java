package com.structurizr.playground;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WorkspaceTooLargeExceptionTests {

    @Test
    void message() {
        Exception exception = new WorkspaceTooLargeException((long)(2.01 * 1024 * 1024), (long)(2.00 * 1024 * 1024));
        assertEquals("Workspace is 2.010 MB, which exceeds the maximum size of 2.000 MB", exception.getMessage());
    }

}