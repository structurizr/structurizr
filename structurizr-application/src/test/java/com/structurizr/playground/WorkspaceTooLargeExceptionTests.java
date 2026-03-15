package com.structurizr.playground;

import org.junit.jupiter.api.Test;

import java.text.DecimalFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WorkspaceTooLargeExceptionTests {

    @Test
    void message() {
        double size = 2.01;
        double max = 2.00;
        Exception exception = new WorkspaceTooLargeException((long)(size * 1024 * 1024), (long)(max * 1024 * 1024));
        assertEquals(
                String.format(
                        "Workspace is %s MB, which exceeds the maximum size of %s MB",
                        new DecimalFormat("#0.000").format(size),
                        new DecimalFormat("#0.000").format(max)
                ),
                exception.getMessage());
    }

}