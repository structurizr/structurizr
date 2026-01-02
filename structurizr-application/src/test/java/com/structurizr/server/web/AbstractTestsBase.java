package com.structurizr.server.web;

import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.nio.file.Files;

public abstract class AbstractTestsBase {

    protected File createTemporaryDirectory() throws Exception {
        File directory = Files.createTempDirectory(this.getClass().getSimpleName()).toFile();
        directory.mkdirs();
        directory.deleteOnExit();

        return directory;
    }

    protected void deleteDirectory(File directory) {
        FileSystemUtils.deleteRecursively(directory);
    }

}