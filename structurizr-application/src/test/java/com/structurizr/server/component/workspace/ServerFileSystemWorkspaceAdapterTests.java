package com.structurizr.server.component.workspace;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.util.DateUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.structurizr.util.DateUtils.UTC_TIME_ZONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerFileSystemWorkspaceAdapterTests extends AbstractWorkspaceAdapterTests {

    private static File dataDirectory;

    private ServerFileSystemWorkspaceAdapter workspaceAdapter;

    @BeforeEach
    public void setUp() throws Exception {
        dataDirectory = createTemporaryDirectory();

        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.DATA_DIRECTORY, dataDirectory.getAbsolutePath());
        Configuration.init(Profile.Server, properties);

        workspaceAdapter = new ServerFileSystemWorkspaceAdapter();
    }

    @Test
    void test_removeOldWorkspaceVersions() throws Exception {
        File workspaceDirectory = new File(dataDirectory, "123");
        workspaceDirectory.mkdirs();

        File workspaceJson = new File(workspaceDirectory, "workspace.json");
        workspaceJson.createNewFile();

        Calendar cal = DateUtils.getCalendar();
        SimpleDateFormat sdf = new SimpleDateFormat(ServerFileSystemWorkspaceAdapter.VERSION_TIMESTAMP_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(UTC_TIME_ZONE));

        List<String> filenames = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            cal.add(Calendar.MINUTE, -i);
            String filename = "workspace-" + sdf.format(cal.getTime()) + ".json";
            File version = new File(workspaceDirectory, filename);
            version.createNewFile();
            filenames.add(filename);
        }

        Properties properties = Configuration.getInstance().getProperties();
        properties.setProperty(StructurizrProperties.MAX_WORKSPACE_VERSIONS, "30");
        Configuration.init(Profile.Server, properties);

        workspaceAdapter.removeOldWorkspaceVersions();

        File[] files = workspaceDirectory.listFiles((dir, name) -> name.matches(ServerFileSystemWorkspaceAdapter.WORKSPACE_VERSION_JSON_FILENAME_REGEX));
        assertEquals(30, files.length);
        assertTrue(workspaceJson.exists());
        for (int i = 0; i < 30; i++) {
            String filename = filenames.get(i);
            assertTrue(new File(workspaceDirectory, filename).exists());
        }

        properties = Configuration.getInstance().getProperties();
        properties.setProperty(StructurizrProperties.MAX_WORKSPACE_VERSIONS, "10");
        Configuration.init(Profile.Server, properties);

        workspaceAdapter.removeOldWorkspaceVersions();
        files = workspaceDirectory.listFiles((dir, name) -> name.matches(ServerFileSystemWorkspaceAdapter.WORKSPACE_VERSION_JSON_FILENAME_REGEX));
        assertEquals(10, files.length);
        assertTrue(workspaceJson.exists());
        for (int i = 0; i < 10; i++) {
            String filename = filenames.get(i);
            assertTrue(new File(workspaceDirectory, filename).exists());
        }
    }

    @AfterEach
    public void tearDown() {
        deleteDirectory(dataDirectory);
    }

    @Override
    protected WorkspaceAdapter getWorkspaceAdapter() {
        return workspaceAdapter;
    }

}