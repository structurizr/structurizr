package com.structurizr.api;

import com.structurizr.Workspace;
import com.structurizr.encryption.AesEncryptionStrategy;
import com.structurizr.encryption.EncryptedWorkspace;
import com.structurizr.io.json.EncryptedJsonReader;
import com.structurizr.io.json.JsonReader;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class WorkspaceApiClientWhenAuthenticationDisabledIntegrationTests {

    private static final String WORKSPACE_PROPERTIES = "workspace.properties";

    private static StructurizrContainer dockerContainer;
    private static String structurizrApiUrl = "http://localhost:8080/api";
    private static File workspaceArchiveLocation;

    private WorkspaceApiClient client;

    @BeforeAll
    static void startStructurizrServer() throws Exception {
        File structurizrDataDirectory = Files.createTempDirectory(WorkspaceApiClientWhenAuthenticationDisabledIntegrationTests.class.getSimpleName()).toFile();
        structurizrDataDirectory.deleteOnExit();

        workspaceArchiveLocation = new File(structurizrDataDirectory.getAbsolutePath(), "archive");
        workspaceArchiveLocation.mkdirs();

        File workspaceDirectory = new File(structurizrDataDirectory, "1");
        workspaceDirectory.mkdirs();

        Properties properties = new Properties();
        properties.setProperty("name", "Name");
        properties.setProperty("description", "Description");
        properties.setProperty("apiKey", "1234567890");
        StringWriter stringWriter = new StringWriter();
        properties.store(stringWriter, null);
        Files.writeString(new File(workspaceDirectory, WORKSPACE_PROPERTIES).toPath(), stringWriter.toString());

        dockerContainer = new StructurizrContainer(structurizrDataDirectory);
        dockerContainer.start();

        int port = dockerContainer.getMappedPort(8080);
        structurizrApiUrl = "http://localhost:" + port + "/api";

        Logger logger = LoggerFactory.getLogger(StructurizrContainer.class);
        Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(logger);
        dockerContainer.followOutput(logConsumer);
    }

    @AfterAll
    static void stopStructurizrServer() {
        if (dockerContainer != null) {
            dockerContainer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        client = new WorkspaceApiClient(structurizrApiUrl, 1, "");
        client.setMergeFromRemote(false);
        client.setWorkspaceArchiveLocation(workspaceArchiveLocation);
        workspaceArchiveLocation.mkdirs();
        clearWorkspaceArchive();
        assertEquals(0, workspaceArchiveLocation.listFiles().length);
    }

    @AfterEach
    void tearDown() {
        clearWorkspaceArchive();
        workspaceArchiveLocation.delete();
    }

    private void clearWorkspaceArchive() {
        if (workspaceArchiveLocation.listFiles() != null) {
            for (File file : workspaceArchiveLocation.listFiles()) {
                file.delete();
            }
        }
    }

    private File getArchivedWorkspace() {
        return workspaceArchiveLocation.listFiles()[0];
    }

    @Test
    @Tag("IntegrationTest")
    void putAndGetWorkspace_WithoutEncryption() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.getModel().addSoftwareSystem("Name");

        client.putWorkspace(workspace);

        workspace = client.getWorkspace();
        assertNotNull(workspace.getModel().getSoftwareSystemWithName("Name"));

        // and check the archive version is readable
        Workspace archivedWorkspace = new JsonReader().read(new FileReader(getArchivedWorkspace()));
        assertEquals(1, archivedWorkspace.getId());
        assertEquals("Name", archivedWorkspace.getName());
        assertEquals(1, archivedWorkspace.getModel().getSoftwareSystems().size());

        assertEquals(1, workspaceArchiveLocation.listFiles().length);
    }

    @Test
    @Tag("IntegrationTest")
    void putAndGetWorkspace_WithEncryption() throws Exception {
        client.setEncryptionStrategy(new AesEncryptionStrategy("password"));
        Workspace workspace = new Workspace("Name", "Description");
        workspace.getModel().addSoftwareSystem("Name");

        client.putWorkspace(workspace);

        workspace = client.getWorkspace();
        assertNotNull(workspace.getModel().getSoftwareSystemWithName("Name"));

        // and check the archive version is readable
        EncryptedWorkspace archivedWorkspace = new EncryptedJsonReader().read(new FileReader(getArchivedWorkspace()));
        assertEquals(1, archivedWorkspace.getId());
        assertEquals("Name", archivedWorkspace.getName());
        assertInstanceOf(AesEncryptionStrategy.class, archivedWorkspace.getEncryptionStrategy());

        assertEquals(1, workspaceArchiveLocation.listFiles().length);
    }

}

class StructurizrContainer extends GenericContainer<StructurizrContainer> {

    private static final String STRUCTURIZR_DOCKER_IMAGE = "structurizr/structurizr:preview";
    private static final String PATH_IN_CONTAINER = "/usr/local/structurizr";

    public StructurizrContainer(File structurizrDataDirectory) {
        super(DockerImageName.parse(STRUCTURIZR_DOCKER_IMAGE));

        withExposedPorts(8080);
        withFileSystemBind(structurizrDataDirectory.getAbsolutePath(), PATH_IN_CONTAINER, BindMode.READ_WRITE);
        withCommand("server");
        waitingFor(Wait.forHttp("/"));
    }

}