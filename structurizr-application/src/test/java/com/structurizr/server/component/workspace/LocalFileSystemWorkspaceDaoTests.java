package com.structurizr.server.component.workspace;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import com.structurizr.server.web.AbstractTestsBase;
import com.structurizr.util.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Properties;

import static com.structurizr.configuration.StructurizrProperties.DATA_DIRECTORY;
import static org.junit.jupiter.api.Assertions.*;

public class LocalFileSystemWorkspaceDaoTests extends AbstractTestsBase {

    private File dataDirectory;
    private SingleWorkspaceLocalFileSystemWorkspaceDao dao;

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
    void getWorkspace_WhenAWorkspaceJsonFileDoesNotExist() {
        dao = new SingleWorkspaceLocalFileSystemWorkspaceDao(dataDirectory);
        FileUtils.write(new File(dataDirectory, "workspace.dsl"), """
                workspace {
                }""");

        try {
            dao.getWorkspace(1, "", "");
            fail();
        } catch (Exception e) {
            assertEquals("Workspace 1 does not exist", e.getMessage());
        }
    }

    @Test
    void getWorkspace_WhenAWorkspaceJsonFileDoesExist() {
        dao = new SingleWorkspaceLocalFileSystemWorkspaceDao(dataDirectory);
        FileUtils.write(new File(dataDirectory, "workspace.json"), "{}");

        String json = dao.getWorkspace(1, "", "");
        assertEquals("{}", json);
    }

//    @Test
//    void getImage_ThrowsException_WhenRequestingAFileThatIsNotAnImage() throws Exception {
//        Path tmpdir = Files.createTempDirectory(Paths.get("build"), getClass().getSimpleName());
//        Configuration.init(Profile.Local, tmpdir.toFile());
//        WorkspaceDao workspaceDao = new LocalFileSystemWorkspaceDao(Configuration.getInstance().getDataDirectory());
//
//        try {
//            workspaceDao.getImage(1, "xss.js");
//            fail();
//        } catch (WorkspaceComponentException e) {
//            assertEquals("xss.js is not an image", e.getMessage());
//        }
//    }
//
}