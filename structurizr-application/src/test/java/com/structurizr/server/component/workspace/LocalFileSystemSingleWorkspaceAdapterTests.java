package com.structurizr.server.component.workspace;

import com.structurizr.Workspace;
import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import com.structurizr.server.domain.WorkspaceMetadata;
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

class LocalFileSystemSingleWorkspaceAdapterTests extends AbstractWorkspaceAdapterTests {

    private File dataDirectory;
    private LocalFileSystemSingleWorkspaceAdapter workspaceAdapter;

    @BeforeEach
    void setUp() throws Exception {
        dataDirectory = createTemporaryDirectory();

        Properties properties = new Properties();
        properties.setProperty(DATA_DIRECTORY, dataDirectory.getAbsolutePath());

        Configuration.init(Profile.Local, properties);

        workspaceAdapter = new LocalFileSystemSingleWorkspaceAdapter();
    }

    @AfterEach
    void tearDown() {
        deleteDirectory(dataDirectory);
    }

    @Test
    void constructor_WhenTheDataDirectoryDoesNotExist() {
        workspaceAdapter = new LocalFileSystemSingleWorkspaceAdapter();

        assertTrue(new File(dataDirectory, "workspace.dsl").exists());
        assertFalse(new File(dataDirectory, "workspace.json").exists());
    }

    @Test
    void constructor_WhenAWorkspaceJsonFileExists() {
        deleteDirectory(dataDirectory);
        dataDirectory.mkdirs();
        FileUtils.write(new File(dataDirectory, "workspace.json"), "{}");
        workspaceAdapter = new LocalFileSystemSingleWorkspaceAdapter();

        assertTrue(new File(dataDirectory, "workspace.json").exists());
        assertFalse(new File(dataDirectory, "workspace.dsl").exists()); // this doesn't get created automatically
    }

    @Test
    @Override
    void getWorkspaceIds() {
        workspaceAdapter = new LocalFileSystemSingleWorkspaceAdapter();

        assertEquals(1, workspaceAdapter.getWorkspaceIds().size());
        assertEquals(1, workspaceAdapter.getWorkspaceIds().getFirst());
    }

    @Test
    void getWorkspaceMetadata_WhenJsonFileExists() throws Exception{
        Workspace workspace = new Workspace("JSON", "Description");
        WorkspaceUtils.saveWorkspaceToJson(workspace, new File(dataDirectory, "workspace.json"));

        workspaceAdapter = new LocalFileSystemSingleWorkspaceAdapter();

        WorkspaceMetadata wmd = workspaceAdapter.getWorkspaceMetadata(1);
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

        workspaceAdapter = new LocalFileSystemSingleWorkspaceAdapter();

        WorkspaceMetadata wmd = workspaceAdapter.getWorkspaceMetadata(1);
        assertEquals("DSL", wmd.getName());
        assertEquals("Description", wmd.getDescription());
        assertFalse(StringUtils.isNullOrEmpty(wmd.getApiKey()));
        assertFalse(StringUtils.isNullOrEmpty(wmd.getApiSecret()));
    }

    @Override
    void workspaceMetadata() {
        // see:
        //  - getWorkspaceMetadata_WhenJsonFileExists()
        //  - getWorkspaceMetadata_WhenDslFileExists()
    }

    @Override
    void branches() {
        // not supported
    }

    @Override
    void versions() {
        // not supported
    }

    @Override
    void deleteWorkspace() {
        // no supported
    }

    @Override
    void image_Branch() throws Exception {
        // no supported
    }

    @Override
    protected WorkspaceAdapter getWorkspaceAdapter() {
        return workspaceAdapter;
    }

}