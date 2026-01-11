package com.structurizr.server.component.workspace;

import com.structurizr.server.domain.Image;
import com.structurizr.server.domain.InputStreamAndContentLength;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.AbstractTestsBase;
import com.structurizr.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class AbstractWorkspaceAdapterTests extends AbstractTestsBase {

    protected abstract WorkspaceAdapter getWorkspaceAdapter();

    @Test
    void getWorkspaceIds() {
        WorkspaceAdapter workspaceAdapter = getWorkspaceAdapter();

        WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(3);
        workspaceMetaData.setName("Name");
        workspaceMetaData.setDescription("Description");
        workspaceMetaData.setApiKey("key");
        workspaceMetaData.setApiSecret("secret");
        workspaceAdapter.putWorkspaceMetadata(workspaceMetaData);

        workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setName("Name");
        workspaceMetaData.setDescription("Description");
        workspaceMetaData.setApiKey("key");
        workspaceMetaData.setApiSecret("secret");
        workspaceAdapter.putWorkspaceMetadata(workspaceMetaData);

        List<Long> ids = getWorkspaceAdapter().getWorkspaceIds();
        assertEquals(2, ids.size());
        assertEquals(1L, ids.get(0));
        assertEquals(3L, ids.get(1));
    }

    @Test
    void workspaceMetadata() {
        WorkspaceAdapter workspaceAdapter = getWorkspaceAdapter();

        assertNull(workspaceAdapter.getWorkspaceMetadata(1));

        WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setName("Name");
        workspaceMetaData.setDescription("Description");
        workspaceMetaData.setApiKey("key");
        workspaceMetaData.setApiSecret("secret");

        workspaceAdapter.putWorkspaceMetadata(workspaceMetaData);

        workspaceMetaData = workspaceAdapter.getWorkspaceMetadata(1);
        assertEquals("Name", workspaceMetaData.getName());
        assertEquals("Description", workspaceMetaData.getDescription());
        assertEquals("key", workspaceMetaData.getApiKey());
        assertEquals("secret", workspaceMetaData.getApiSecret());
    }

    @Test
    void workspaceContent() {
        WorkspaceAdapter workspaceAdapter = getWorkspaceAdapter();

        workspaceAdapter.putWorkspace(new WorkspaceMetadata(1), "json", "");

        String json = workspaceAdapter.getWorkspace(1, "", "");
        assertEquals("json", json);
    }

    @Test
    void branches() {
        WorkspaceAdapter workspaceAdapter = getWorkspaceAdapter();

        WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setName("Name");
        workspaceMetaData.setDescription("Description");
        workspaceMetaData.setApiKey("key");
        workspaceMetaData.setApiSecret("secret");

        workspaceAdapter.putWorkspaceMetadata(workspaceMetaData);

        List<WorkspaceBranch> branches = workspaceAdapter.getWorkspaceBranches(1);
        assertEquals(0, branches.size());

        workspaceAdapter.putWorkspace(workspaceMetaData, "json", "");
        workspaceAdapter.putWorkspace(workspaceMetaData, "json-branch1", "branch1");
        workspaceAdapter.putWorkspace(workspaceMetaData, "json-branch2", "branch2");

        branches = workspaceAdapter.getWorkspaceBranches(1);
        assertEquals(2, branches.size());
        assertEquals("branch1", branches.get(0).getName());
        assertEquals("branch2", branches.get(1).getName());

        assertEquals("json", workspaceAdapter.getWorkspace(1, "", ""));
        assertEquals("json-branch1", workspaceAdapter.getWorkspace(1, "branch1", ""));
        assertEquals("json-branch2", workspaceAdapter.getWorkspace(1, "branch2", ""));
        assertNull(workspaceAdapter.getWorkspace(1, "unknown-branch", ""));

        workspaceAdapter.deleteBranch(1, "branch3");
        branches = workspaceAdapter.getWorkspaceBranches(1);
        assertEquals(2, branches.size());
        assertEquals("branch1", branches.get(0).getName());
        assertEquals("branch2", branches.get(1).getName());

        workspaceAdapter.deleteBranch(1, "branch1");
        branches = workspaceAdapter.getWorkspaceBranches(1);
        assertEquals(1, branches.size());
        assertEquals("branch2", branches.get(0).getName());
    }

    @Test
    void versions() {
        WorkspaceAdapter workspaceAdapter = getWorkspaceAdapter();

        WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setName("Name");
        workspaceMetaData.setDescription("Description");
        workspaceMetaData.setApiKey("key");
        workspaceMetaData.setApiSecret("secret");

        workspaceAdapter.putWorkspaceMetadata(workspaceMetaData);

        List<WorkspaceVersion> versions = workspaceAdapter.getWorkspaceVersions(1, "", 10);
        assertEquals(0, versions.size());

        workspaceMetaData.setLastModifiedDate(DateUtils.getEndOfDay(2026, 1, 1));
        workspaceAdapter.putWorkspace(workspaceMetaData, "json-1", "");

        versions = workspaceAdapter.getWorkspaceVersions(1, "", 10);
        assertEquals(1, versions.size());
        assertNull(versions.get(0).getVersionId());
        assertEquals(DateUtils.getEndOfDay(2026, 1, 1), versions.get(0).getLastModifiedDate());
        assertEquals("json-1", workspaceAdapter.getWorkspace(1, "", versions.get(0).getVersionId()));

        workspaceMetaData.setLastModifiedDate(DateUtils.getEndOfDay(2026, 1, 2));
        workspaceAdapter.putWorkspace(workspaceMetaData, "json-2", "");

        versions = workspaceAdapter.getWorkspaceVersions(1, "", 10);
        assertEquals(2, versions.size());
        assertNull(versions.get(0).getVersionId());
        assertEquals(DateUtils.getEndOfDay(2026, 1, 2), versions.get(0).getLastModifiedDate());
        assertEquals(DateUtils.getEndOfDay(2026, 1, 1), versions.get(1).getLastModifiedDate());
        assertEquals("json-2", workspaceAdapter.getWorkspace(1, "", versions.get(0).getVersionId()));
        assertEquals("json-1", workspaceAdapter.getWorkspace(1, "", versions.get(1).getVersionId()));

        workspaceMetaData.setLastModifiedDate(DateUtils.getEndOfDay(2026, 1, 3));
        workspaceAdapter.putWorkspace(workspaceMetaData, "json-3", "");

        versions = workspaceAdapter.getWorkspaceVersions(1, "", 10);
        assertEquals(3, versions.size());
        assertNull(versions.get(0).getVersionId());
        assertEquals(DateUtils.getEndOfDay(2026, 1, 3), versions.get(0).getLastModifiedDate());
        assertEquals(DateUtils.getEndOfDay(2026, 1, 2), versions.get(1).getLastModifiedDate());
        assertEquals(DateUtils.getEndOfDay(2026, 1, 1), versions.get(2).getLastModifiedDate());
        assertEquals("json-3", workspaceAdapter.getWorkspace(1, "", versions.get(0).getVersionId()));
        assertEquals("json-2", workspaceAdapter.getWorkspace(1, "", versions.get(1).getVersionId()));
        assertEquals("json-1", workspaceAdapter.getWorkspace(1, "", versions.get(2).getVersionId()));
    }

    @Test
    void deleteWorkspace() {
        WorkspaceAdapter workspaceAdapter = getWorkspaceAdapter();

        WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setName("Name");
        workspaceMetaData.setDescription("Description");
        workspaceMetaData.setApiKey("key");
        workspaceMetaData.setApiSecret("secret");
        workspaceAdapter.putWorkspaceMetadata(workspaceMetaData);

        workspaceMetaData = new WorkspaceMetadata(2);
        workspaceMetaData.setName("Name");
        workspaceMetaData.setDescription("Description");
        workspaceMetaData.setApiKey("key");
        workspaceMetaData.setApiSecret("secret");
        workspaceAdapter.putWorkspaceMetadata(workspaceMetaData);

        List<Long> ids = getWorkspaceAdapter().getWorkspaceIds();
        assertEquals(2, ids.size());
        assertEquals(1L, ids.get(0));
        assertEquals(2L, ids.get(1));

        assertNotNull(workspaceAdapter.getWorkspaceMetadata(1));
        assertNotNull(workspaceAdapter.getWorkspaceMetadata(2));

        workspaceAdapter.deleteWorkspace(1);

        ids = getWorkspaceAdapter().getWorkspaceIds();
        assertEquals(1, ids.size());
        assertEquals(2L, ids.get(0));

        assertNull(workspaceAdapter.getWorkspaceMetadata(1));
        assertNotNull(workspaceAdapter.getWorkspaceMetadata(2));
    }

    @Test
    void image() throws Exception {
        File tempDirectory = createTemporaryDirectory();
        File image = new File(tempDirectory, "image.png");
        Files.copy(
                new File("src/main/resources/static/static/img/structurizr-logo.png").toPath(),
                image.toPath()
        );
        assertTrue(image.exists());

        boolean result = getWorkspaceAdapter().putImage(1, "", "image.png", image);
        assertTrue(result);

        InputStreamAndContentLength isacl = getWorkspaceAdapter().getImage(1, "", "image.png");
        assertEquals(11871, isacl.getContentLength());

        // try a branch version
        isacl = getWorkspaceAdapter().getImage(1, "branch", "image.png");
        assertNull(isacl);
    }

    @Test
    void image_Branch() throws Exception {
        File tempDirectory = createTemporaryDirectory();
        File image = new File(tempDirectory, "image.png");
        Files.copy(
                new File("src/main/resources/static/static/img/structurizr-logo.png").toPath(),
                image.toPath()
        );
        assertTrue(image.exists());

        boolean result = getWorkspaceAdapter().putImage(1, "branch", "image.png", image);
        assertTrue(result);

        InputStreamAndContentLength isacl = getWorkspaceAdapter().getImage(1, "branch", "image.png");
        assertEquals(11871, isacl.getContentLength());

        // try the main branch version
        isacl = getWorkspaceAdapter().getImage(1, "", "image.png");
        assertNull(isacl);
    }

    @Test
    void images() throws Exception {
        File tempDirectory = createTemporaryDirectory();
        NumberFormat numberFormat = new DecimalFormat("00");

        for (int i = 1; i <= 10; i++) {
            String filename = "image-" + numberFormat.format(i) + ".png";
            File image = new File(tempDirectory, filename);
            Files.copy(
                    new File("src/main/resources/static/static/img/structurizr-logo.png").toPath(),
                    image.toPath()
            );
            boolean result = getWorkspaceAdapter().putImage(1, "", filename, image);
            assertTrue(result);
        }

        List<Image> images = getWorkspaceAdapter().getImages(1);
        assertEquals(10, images.size());
        for (int i = 1; i <= 10; i++) {
            String filename = "image-" + numberFormat.format(i) + ".png";
            Image image = images.get(i-1);
            assertEquals(image.getName(), filename);
            assertEquals(11, image.getSizeInKB());
        }

        // and a different workspace ID
        images = getWorkspaceAdapter().getImages(2);
        assertTrue(images.isEmpty());

        // and delete the images
        boolean result = getWorkspaceAdapter().deleteImages(1);
        assertTrue(result);
        assertTrue(getWorkspaceAdapter().getImages(1).isEmpty());
    }

}