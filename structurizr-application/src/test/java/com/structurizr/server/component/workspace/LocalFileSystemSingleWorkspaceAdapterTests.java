package com.structurizr.server.component.workspace;

import com.structurizr.Workspace;
import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.AbstractTestsBase;
import com.structurizr.util.FileUtils;
import com.structurizr.util.StringUtils;
import com.structurizr.util.WorkspaceUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Properties;

import static com.structurizr.configuration.StructurizrProperties.DATA_DIRECTORY;
import static org.junit.jupiter.api.Assertions.*;

public class LocalFileSystemSingleWorkspaceAdapterTests extends AbstractTestsBase {

    private File dataDirectory;
    private LocalFileSystemSingleWorkspaceAdapter adapter;

    @BeforeEach
    void setUp() throws Exception {
        dataDirectory = createTemporaryDirectory();

        Properties properties = new Properties();
        properties.setProperty(DATA_DIRECTORY, dataDirectory.getAbsolutePath());

        Configuration.init(Profile.Local, properties);
    }

    @AfterEach
    void tearDown() {
        deleteDirectory(dataDirectory);
    }

    @Test
    void constructor_WhenTheDataDirectoryDoesNotExist() {
        adapter = new LocalFileSystemSingleWorkspaceAdapter(dataDirectory);

        assertTrue(new File(dataDirectory, "workspace.dsl").exists());
        assertFalse(new File(dataDirectory, "workspace.json").exists());
    }

    @Test
    void constructor_WhenAWorkspaceJsonFileExists() {
        FileUtils.write(new File(dataDirectory, "workspace.json"), "{}");
        adapter = new LocalFileSystemSingleWorkspaceAdapter(dataDirectory);

        assertTrue(new File(dataDirectory, "workspace.json").exists());
        assertFalse(new File(dataDirectory, "workspace.dsl").exists()); // this doesn't get created automatically
    }

    @Test
    void getWorkspaceIds() {
        adapter = new LocalFileSystemSingleWorkspaceAdapter(dataDirectory);

        assertEquals(1, adapter.getWorkspaceIds().size());
        assertEquals(1, adapter.getWorkspaceIds().getFirst());
    }

    @Test
    void getWorkspaceMetadata_WhenJsonFileExists() throws Exception{
        Workspace workspace = new Workspace("JSON", "Description");
        WorkspaceUtils.saveWorkspaceToJson(workspace, new File(dataDirectory, "workspace.json"));

        adapter = new LocalFileSystemSingleWorkspaceAdapter(dataDirectory);

        WorkspaceMetadata wmd = adapter.getWorkspaceMetadata(1);
        assertEquals("JSON", wmd.getName());
        assertEquals("Description", wmd.getDescription());
        assertFalse(StringUtils.isNullOrEmpty(wmd.getApiKey()));
        assertFalse(StringUtils.isNullOrEmpty(wmd.getApiSecret()));
    }

    @Test
    void getWorkspaceMetadata_WhenDslFileExists() {
        String dsl = """
                workspace "DSL" "Description" {
                }""";
        FileUtils.write(new File(dataDirectory, "workspace.dsl"), dsl);

        adapter = new LocalFileSystemSingleWorkspaceAdapter(dataDirectory);

        WorkspaceMetadata wmd = adapter.getWorkspaceMetadata(1);
        assertEquals("DSL", wmd.getName());
        assertEquals("Description", wmd.getDescription());
        assertFalse(StringUtils.isNullOrEmpty(wmd.getApiKey()));
        assertFalse(StringUtils.isNullOrEmpty(wmd.getApiSecret()));
    }

}