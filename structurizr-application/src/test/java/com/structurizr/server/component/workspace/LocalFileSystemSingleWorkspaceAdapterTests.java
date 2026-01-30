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
    void getWorkspaceMetadata_WhenJsonFileExists() throws Exception {
        Workspace workspace = new Workspace("Name - JSON", "Description - JSON");
        WorkspaceUtils.saveWorkspaceToJson(workspace, new File(dataDirectory, "workspace.json"));

        workspaceAdapter = new LocalFileSystemSingleWorkspaceAdapter();

        WorkspaceMetadata wmd = workspaceAdapter.getWorkspaceMetadata(1);
        assertEquals("Name - JSON", wmd.getName());
        assertEquals("Description - JSON", wmd.getDescription());
    }

    @Test
    void getWorkspaceMetadata_WhenJsonFileDoesNotExist() {
        workspaceAdapter = new LocalFileSystemSingleWorkspaceAdapter();

        WorkspaceMetadata wmd = workspaceAdapter.getWorkspaceMetadata(1);
        assertEquals("Workspace 1", wmd.getName());
        assertEquals("", wmd.getDescription());
    }

    @Override
    void workspaceMetadata() {
        // see:
        //  - getWorkspaceMetadata_WhenJsonFileExists()
        //  - getWorkspaceMetadata_WhenJsonFileDoesNotExist()
    }

    @Test
    void getWorkspace_WhenDslFileExists() {
        deleteDirectory(dataDirectory);
        dataDirectory.mkdirs();

        String dsl = """
                workspace "DSL" "Description" {
                }""";
        FileUtils.write(new File(dataDirectory, "workspace.dsl"), dsl);

        workspaceAdapter = new LocalFileSystemSingleWorkspaceAdapter();

        String json = workspaceAdapter.getWorkspace(1, "", "");
        assertTrue(json.startsWith("""
                {"configuration":{},"description":"Description","documentation":{},"id":1,"lastModifiedDate":"""));
        assertTrue(json.endsWith("""
                ,"model":{},"name":"DSL","properties":{"structurizr.inspection.error":"3","structurizr.dsl":"d29ya3NwYWNlICJEU0wiICJEZXNjcmlwdGlvbiIgewp9","structurizr.inspection.info":"0","structurizr.inspection.ignore":"0","structurizr.inspection.warning":"0"},"views":{"configuration":{"styles":{},"terminology":{}}}}"""));
    }

    @Test
    void getWorkspace_WhenJsonFileExists() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.setId(1L);

        deleteDirectory(dataDirectory);
        dataDirectory.mkdirs();
        String json = WorkspaceUtils.toJson(workspace, false);
        FileUtils.write(new File(dataDirectory, "workspace.json"), json);

        workspaceAdapter = new LocalFileSystemSingleWorkspaceAdapter();

        json = workspaceAdapter.getWorkspace(1, "", "");
        assertEquals("""
                {"configuration":{},"description":"Description","documentation":{},"id":1,"model":{},"name":"Name","views":{"configuration":{"styles":{},"terminology":{}}}}""", json);
    }

    @Override
    void workspaceContent() throws Exception {
        // see:
        //  - getWorkspace_WhenJsonFileExists()
        //  - getWorkspace_WhenDslFileExists()
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
        // not supported
    }

    @Override
    void image_Branch() throws Exception {
        // not supported
    }

    @Override
    protected WorkspaceAdapter getWorkspaceAdapter() {
        return workspaceAdapter;
    }

    @Test
    void getDataDirectory_ThrowsAnException_WhenTheWorkspaceIdIsNot1() {
        try {
            workspaceAdapter.getDataDirectory(2);
            fail();
        } catch (Exception e) {
            assertEquals("Workspace ID must be 1", e.getMessage());
        }
    }

}