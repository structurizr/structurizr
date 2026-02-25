package com.structurizr.api;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.Properties;

public class WorkspaceApiClientWhenAuthenticationEnabledIntegrationTests extends AbstractWorkspaceApiClientTests {

    private static final String STRUCTURIZR_PROPERTIES = "structurizr.properties";

    @Override
    protected void configureServer(File structurizrDataDirectory) throws Exception {
        // enable authentication
        Properties properties = new Properties();
        properties.setProperty("structurizr.authentication", "fixed");
        properties.setProperty("structurizr.username", "structurizr");
        properties.setProperty("structurizr.password", "{noop}password");
        StringWriter stringWriter = new StringWriter();
        properties.store(stringWriter, null);
        Files.writeString(new File(structurizrDataDirectory, STRUCTURIZR_PROPERTIES).toPath(), stringWriter.toString());
    }

    protected WorkspaceApiClient createWorkspaceApiClient(String apiUrl, long workspaceId) {
        return new WorkspaceApiClient(apiUrl, workspaceId, "1234567890");
    }

}