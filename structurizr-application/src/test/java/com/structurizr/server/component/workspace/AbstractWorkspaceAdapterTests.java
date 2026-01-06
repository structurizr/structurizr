package com.structurizr.server.component.workspace;

import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.AbstractTestsBase;
import com.structurizr.util.DateUtils;
import org.junit.jupiter.api.Test;

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

        List<WorkspaceBranch> branches = workspaceAdapter.getWorkspaceBranches(1);
        assertEquals(0, branches.size());

        workspaceAdapter.putWorkspace(new WorkspaceMetadata(1), "json", "");
        workspaceAdapter.putWorkspace(new WorkspaceMetadata(1), "json-branch1", "branch1");
        workspaceAdapter.putWorkspace(new WorkspaceMetadata(1), "json-branch2", "branch2");

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

        List<WorkspaceVersion> versions = workspaceAdapter.getWorkspaceVersions(1, "", 10);
        assertEquals(0, versions.size());

        WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
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
    
}