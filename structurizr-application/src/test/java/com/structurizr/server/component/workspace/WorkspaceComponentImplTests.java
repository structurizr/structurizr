package com.structurizr.server.component.workspace;

import com.structurizr.server.domain.Image;
import com.structurizr.server.domain.InputStreamAndContentLength;
import com.structurizr.server.domain.User;
import com.structurizr.server.domain.WorkspaceMetaData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class WorkspaceComponentImplTests {

    private WorkspaceComponentImpl workspaceComponent;

    private List<WorkspaceMetaData> workspaceMetaDataList;
    private Map<Long,String> workspaces;

    @BeforeEach
    void setUp() {
        workspaceMetaDataList = new ArrayList<>();
        workspaces = new HashMap<>();

        workspaceComponent = new WorkspaceComponentImpl(new WorkspaceDao() {
            @Override
            public List<Long> getWorkspaceIds() {
                return workspaceMetaDataList.stream().map(WorkspaceMetaData::getId).collect(Collectors.toList());
            }

            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaDataList.stream().filter(workspace -> workspace.getId() == workspaceId).findFirst().orElse(null);
            }

            @Override
            public void putWorkspaceMetaData(WorkspaceMetaData workspaceMetaData) {
                workspaceMetaDataList.removeIf(workspace -> workspace.getId() == workspaceMetaData.getId());
                workspaceMetaDataList.add(workspaceMetaData);
            }

            @Override
            public long createWorkspace(User user) throws WorkspaceComponentException {
                return 0;
            }

            @Override
            public boolean deleteWorkspace(long workspaceId) {
                return false;
            }

            @Override
            public String getWorkspace(long workspaceId, String branch, String version) {
                return workspaces.get(workspaceId);
            }

            @Override
            public void putWorkspace(WorkspaceMetaData workspaceMetaData, String json, String branch) {

            }

            @Override
            public boolean putImage(long workspaceId, String branch, String filename, File file) {
                return false;
            }

            @Override
            public List<Image> getImages(long workspaceId) {
                return List.of();
            }

            @Override
            public InputStreamAndContentLength getImage(long workspaceId, String branch, String filename) {
                return null;
            }

            @Override
            public boolean deleteImages(long workspaceId) {
                return false;
            }

            @Override
            public List<WorkspaceVersion> getWorkspaceVersions(long workspaceId, String branch, int maxVersions) {
                return List.of();
            }

            @Override
            public List<WorkspaceBranch> getWorkspaceBranches(long workspaceId) {
                return List.of();
            }

            @Override
            public boolean deleteBranch(long workspaceId, String branch) {
                return false;
            }

            @Override
            public long getLastModifiedDate() {
                return 0;
            }
        }, null);
    }

    @Test
    void getWorkspaces_WhenThereAreNoWorkspaces() {
        assertTrue(workspaceComponent.getWorkspaces().isEmpty());
    }

    @Test
    void getWorkspace_WhenThereAreWorkspaces() {
        WorkspaceMetaData wmd1 = new WorkspaceMetaData(1);
        wmd1.setName("1");

        WorkspaceMetaData wmd2 = new WorkspaceMetaData(2);
        wmd2.setName("2");

        workspaceMetaDataList.add(wmd2);
        workspaceMetaDataList.add(wmd1);

        List<WorkspaceMetaData> workspaces = workspaceComponent.getWorkspaces();
        assertEquals(2, workspaces.size());
        assertSame(wmd1, workspaces.get(0));
        assertSame(wmd2, workspaces.get(1));
    }

    @Test
    void getWorkspace_WhenThereAreArchivedWorkspaces() {
        WorkspaceMetaData wmd1 = new WorkspaceMetaData(1);
        wmd1.setName("1");
        wmd1.setArchived(true);

        WorkspaceMetaData wmd2 = new WorkspaceMetaData(2);
        wmd2.setName("2");

        workspaceMetaDataList.add(wmd2);
        workspaceMetaDataList.add(wmd1);

        List<WorkspaceMetaData> workspaces = workspaceComponent.getWorkspaces();
        assertEquals(1, workspaces.size());
        assertSame(wmd2, workspaces.get(0));
    }

    @Test
    void getWorkspaceMetaData_WhenTheWorkspaceDoesNotExist() {
        assertNull(workspaceComponent.getWorkspaceMetaData(1));
    }

    @Test
    void getWorkspaceMetaData_WhenTheWorkspaceExists() {
        WorkspaceMetaData wmd = new WorkspaceMetaData(1);
        wmd.setName("1");
        workspaceMetaDataList.add(wmd);

        assertSame(wmd, workspaceComponent.getWorkspaceMetaData(1));
    }

    @Test
    void getWorkspaceMetaData_WhenTheWorkspaceIsArchived() {
        WorkspaceMetaData wmd = new WorkspaceMetaData(1);
        wmd.setName("1");
        wmd.setArchived(true);
        workspaceMetaDataList.add(wmd);

        assertNull(workspaceComponent.getWorkspaceMetaData(1));
    }

    @Test
    void putWorkspaceMetaData_ThrowsAnException_WhenPassedNull() {
        try {
            workspaceComponent.putWorkspaceMetaData(null);
            fail();
        } catch (Exception e) {
            assertEquals("Workspace metadata cannot be null", e.getMessage());
        }
    }

    @Test
    void putWorkspaceMetaData() {
        WorkspaceMetaData wmd = new WorkspaceMetaData(1);
        wmd.setName("1");

        workspaceComponent.putWorkspaceMetaData(wmd);
        assertSame(wmd, workspaceComponent.getWorkspaceMetaData(1));
    }

    @Test
    void getWorkspace_WhenTheWorkspaceDoesNotExist() {
        try {
            workspaceComponent.getWorkspace(1, null, null);
            fail();
        } catch (Exception e) {
            assertEquals("Could not get workspace 1", e.getMessage());
        }
    }

    @Test
    void getWorkspace_WhenTheWorkspaceExists() {
        workspaces.put(1L, "json");
        assertEquals("json", workspaceComponent.getWorkspace(1, null, null));
    }

    @Test
    void getWorkspace_WhenTheBranchNameIsInvalid() {
        try {
            workspaceComponent.getWorkspace(1, "!branch", null);
            fail();
        } catch (Exception e) {
            assertEquals("The branch name \"!branch\" is invalid", e.getMessage());
        }
    }

    @Test
    void putImage_ThrowsException_WhenPuttingAFileThatIsNotAnImage() {
        try {
            workspaceComponent.putImage(1, "", "xss.js", new File("xss.js"));
            fail();
        } catch (WorkspaceComponentException e) {
            assertEquals("xss.js is not an image", e.getMessage());
        }
    }

}
