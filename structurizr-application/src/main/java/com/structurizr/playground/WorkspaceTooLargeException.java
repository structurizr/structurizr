package com.structurizr.playground;

import java.text.DecimalFormat;

public class WorkspaceTooLargeException extends Exception {

    private static final double BYTES_IN_MEGABYTE = 1024 * 1024;

    WorkspaceTooLargeException(long workspaceSizeInBytes, long maxWorkspaceSizeInBytes) {
        super(String.format("Workspace is %s MB, which exceeds the maximum size of %s MB",
                new DecimalFormat("#0.000").format(workspaceSizeInBytes / BYTES_IN_MEGABYTE),
                new DecimalFormat("#0.000").format(maxWorkspaceSizeInBytes / BYTES_IN_MEGABYTE)
        ));
    }

}
