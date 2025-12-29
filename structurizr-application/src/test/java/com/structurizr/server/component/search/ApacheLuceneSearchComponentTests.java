package com.structurizr.server.component.search;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.nio.file.Files;

public class ApacheLuceneSearchComponentTests extends AbstractSearchComponentTests {

    private static File dataDirectory;

    private ApacheLuceneSearchComponentImpl searchComponent;

    @BeforeEach
    public void setUp() throws Exception {
        dataDirectory = Files.createTempDirectory(this.getClass().getSimpleName()).toFile();
        dataDirectory.mkdirs();

        searchComponent = new ApacheLuceneSearchComponentImpl(dataDirectory);
        searchComponent.start();
    }

    @AfterEach
    public void tearDown() {
        searchComponent.stop();
        FileSystemUtils.deleteRecursively(dataDirectory);
    }

    @Override
    protected SearchComponent getSearchComponent() {
        return searchComponent;
    }
}