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
import java.util.List;
import java.util.Properties;

import static com.structurizr.configuration.StructurizrProperties.DATA_DIRECTORY;
import static org.junit.jupiter.api.Assertions.*;

class LocalFileSystemMultipleWorkspaceAdapterTests extends AbstractWorkspaceAdapterTests {

    private File dataDirectory;
    private LocalFileSystemMultipleWorkspaceAdapter workspaceAdapter;

    @BeforeEach
    void setUp() throws Exception {
        dataDirectory = createTemporaryDirectory();

        Properties properties = new Properties();
        properties.setProperty(DATA_DIRECTORY, dataDirectory.getAbsolutePath());

        Configuration.init(Profile.Local, properties);

        workspaceAdapter = new LocalFileSystemMultipleWorkspaceAdapter();
    }

    @AfterEach
    void tearDown() {
        deleteDirectory(dataDirectory);
    }

    @Test
    void getWorkspaceIds() {
        new File(dataDirectory, "1").mkdir();
        new File(dataDirectory, "02").mkdir();
        new File(dataDirectory, "3-software-system-a").mkdir();
        new File(dataDirectory, "04-software-system-c").mkdir();
        new File(dataDirectory, "005-software-system-d").mkdir();
        FileUtils.write(new File(dataDirectory, "7"), "text");
        FileUtils.write(new File(dataDirectory, "08"), "text");

        List<Long> workspaceIds = workspaceAdapter.getWorkspaceIds();
        assertEquals(5, workspaceIds.size());
        assertEquals(1L, workspaceIds.get(0));
        assertEquals(2L, workspaceIds.get(1));
        assertEquals(3L, workspaceIds.get(2));
        assertEquals(4L, workspaceIds.get(3));
        assertEquals(5L, workspaceIds.get(4));
    }

    @Test
    void getDataDirectory_WhenTheDirectoryNameIsTheId() {
        File workspace1Directory = new File(dataDirectory, "1");
        workspace1Directory.mkdir();

        File workspace2Directory = new File(dataDirectory, "2");
        workspace2Directory.mkdir();

        File workspace3Directory = new File(dataDirectory, "3");
        workspace3Directory.mkdir();

        assertEquals(workspace1Directory.getAbsolutePath(), workspaceAdapter.getDataDirectory(1).getAbsolutePath());
        assertEquals(workspace2Directory.getAbsolutePath(), workspaceAdapter.getDataDirectory(2).getAbsolutePath());
        assertEquals(workspace3Directory.getAbsolutePath(), workspaceAdapter.getDataDirectory(3).getAbsolutePath());
    }

    @Test
    void getDataDirectory_WhenTheDirectoryNameHasAnIdPrefix() {
        FileUtils.write(new File(dataDirectory, "1"), "text");
        File workspace1Directory = new File(dataDirectory, "1-software-system-a");
        workspace1Directory.mkdir();

        FileUtils.write(new File(dataDirectory, "2"), "text");
        File workspace2Directory = new File(dataDirectory, "02-software-system-b");
        workspace2Directory.mkdir();

        FileUtils.write(new File(dataDirectory, "3"), "text");
        File workspace3Directory = new File(dataDirectory, "003-software-system-c");
        workspace3Directory.mkdir();

        assertEquals(workspace1Directory.getAbsolutePath(), workspaceAdapter.getDataDirectory(1).getAbsolutePath());
        assertEquals(workspace2Directory.getAbsolutePath(), workspaceAdapter.getDataDirectory(2).getAbsolutePath());
        assertEquals(workspace3Directory.getAbsolutePath(), workspaceAdapter.getDataDirectory(3).getAbsolutePath());
    }

    @Test
    @Override
    void workspaceMetadata() {
        try {
            Workspace workspace = new Workspace("JSON", "Description");
            File workspaceDirectory = workspaceAdapter.getDataDirectory(1);
            workspaceDirectory.mkdirs();

            WorkspaceUtils.saveWorkspaceToJson(workspace, new File(workspaceDirectory, "workspace.json"));

            workspaceAdapter = new LocalFileSystemMultipleWorkspaceAdapter();

            WorkspaceMetadata wmd = workspaceAdapter.getWorkspaceMetadata(1);
            assertEquals("JSON", wmd.getName());
            assertEquals("Description", wmd.getDescription());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
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
        // no supported
    }

    @Override
    protected WorkspaceAdapter getWorkspaceAdapter() {
        return workspaceAdapter;
    }

}