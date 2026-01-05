package com.structurizr.server.component.search;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import com.structurizr.configuration.StructurizrProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.util.Properties;

public class ApacheLuceneSearchAdapterTests extends AbstractSearchAdapterTests {

    private static File dataDirectory;

    private ApacheLuceneSearchAdapter searchAdapter;

    @BeforeEach
    public void setUp() throws Exception {
        dataDirectory = createTemporaryDirectory();

        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.DATA_DIRECTORY, dataDirectory.getAbsolutePath());
        Configuration.init(Profile.Local, properties);

        searchAdapter = new ApacheLuceneSearchAdapter();
        searchAdapter.start();
    }

    @AfterEach
    public void tearDown() {
        searchAdapter.stop();
        deleteDirectory(dataDirectory);
    }

    @Override
    protected SearchAdapter getSearchAdapter() {
        return searchAdapter;
    }

}