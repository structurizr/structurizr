package com.structurizr.server.component.workspace;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import com.structurizr.server.domain.WorkspaceMetaData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class WorkspaceComponentImplTests {

    private WorkspaceComponentImpl workspaceComponent;

    @BeforeEach
    void setUp() {
        Configuration.init(Profile.Local, new Properties());
    }

    @Test
    void getWorkspaces_WhenThereAreNoWorkspaces() {
        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceDao() {
            @Override
            public List<Long> getWorkspaceIds() {
                return new ArrayList<>();
            }
        });

        assertTrue(workspaceComponent.getWorkspaces().isEmpty());
    }

    @Test
    void getWorkspace_WhenThereAreWorkspaces() {
        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceDao() {
            @Override
            public List<Long> getWorkspaceIds() {
                return List.of(1L, 2L);
            }

            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return new WorkspaceMetaData(workspaceId);
            }
        });

        List<WorkspaceMetaData> workspaces = workspaceComponent.getWorkspaces();
        assertEquals(2, workspaces.size());
        assertEquals(1L, workspaces.get(0).getId());
        assertEquals(2L, workspaces.get(1).getId());
    }

    @Test
    void getWorkspaces_WhenThereAreArchivedWorkspaces() {
        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceDao() {
            @Override
            public List<Long> getWorkspaceIds() {
                return List.of(1L, 2L);
            }

            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                WorkspaceMetaData wmd = new WorkspaceMetaData(workspaceId);
                if (workspaceId == 1L) {
                    wmd.setArchived(true);
                }

                return wmd;
            }
        });

        List<WorkspaceMetaData> workspaces = workspaceComponent.getWorkspaces();
        assertEquals(1, workspaces.size());
        assertEquals(2L, workspaces.get(0).getId());
    }

    @Test
    void getWorkspaceMetaData_WhenTheWorkspaceDoesNotExist() {
        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceDao());
        assertNull(workspaceComponent.getWorkspaceMetaData(1));
    }

    @Test
    void getWorkspaceMetaData_WhenTheWorkspaceExists() {
        WorkspaceMetaData wmd = new WorkspaceMetaData(1);

        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceDao() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return wmd;
            }
        });

        assertSame(wmd, workspaceComponent.getWorkspaceMetaData(1));
    }

    @Test
    void getWorkspaceMetaData_WhenTheWorkspaceIsArchived() {
        WorkspaceMetaData wmd = new WorkspaceMetaData(1);
        wmd.setArchived(true);

        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceDao() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return wmd;
            }
        });

        assertNull(workspaceComponent.getWorkspaceMetaData(1));
    }

    @Test
    void putWorkspaceMetaData_ThrowsAnException_WhenPassedNull() {
        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceDao());

        try {
            workspaceComponent.putWorkspaceMetaData(null);
            fail();
        } catch (Exception e) {
            assertEquals("Workspace metadata cannot be null", e.getMessage());
        }
    }

    @Test
    void putWorkspaceMetaData() {
        List<WorkspaceMetaData> workspaces = new ArrayList<>();

        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceDao() {
            @Override
            public void putWorkspaceMetaData(WorkspaceMetaData workspaceMetaData) {
                workspaces.add(workspaceMetaData);
            }
        });

        WorkspaceMetaData wmd = new WorkspaceMetaData(1);
        workspaceComponent.putWorkspaceMetaData(wmd);
        assertSame(wmd, workspaces.get(0));
    }

    @Test
    void getWorkspace_WhenTheWorkspaceDoesNotExist() {
        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceDao());

        try {
            workspaceComponent.getWorkspace(1, null, null);
            fail();
        } catch (Exception e) {
            assertEquals("Could not get workspace 1", e.getMessage());
        }
    }

    @Test
    void getWorkspace_WhenTheWorkspaceExists() {
        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceDao() {
            @Override
            public String getWorkspace(long workspaceId, String branch, String version) {
                return "json";
            }
        });

        assertEquals("json", workspaceComponent.getWorkspace(1, null, null));
    }

    @Test
    void getWorkspace_WhenTheBranchNameIsInvalid() {
        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceDao());

        try {
            workspaceComponent.getWorkspace(1, "!branch", null);
            fail();
        } catch (Exception e) {
            assertEquals("The branch name \"!branch\" is invalid", e.getMessage());
        }
    }

    @Test
    void putImage_ThrowsException_WhenPuttingAFileThatIsNotAnImage() {
        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceDao());

        try {
            workspaceComponent.putImage(1, "", "xss.js", new File("xss.js"));
            fail();
        } catch (WorkspaceComponentException e) {
            assertEquals("xss.js is not an image", e.getMessage());
        }
    }

}
