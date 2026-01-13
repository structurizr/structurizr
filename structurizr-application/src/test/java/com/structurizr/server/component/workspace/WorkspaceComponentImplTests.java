package com.structurizr.server.component.workspace;

import com.structurizr.Workspace;
import com.structurizr.configuration.*;
import com.structurizr.encryption.AesEncryptionStrategy;
import com.structurizr.encryption.EncryptedWorkspace;
import com.structurizr.encryption.EncryptionLocation;
import com.structurizr.encryption.EncryptionStrategy;
import com.structurizr.io.json.EncryptedJsonWriter;
import com.structurizr.server.domain.AuthenticationMethod;
import com.structurizr.server.domain.WorkspaceMetadata;
import com.structurizr.server.web.AbstractTestsBase;
import com.structurizr.util.DateUtils;
import com.structurizr.util.WorkspaceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.StringWriter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class WorkspaceComponentImplTests extends AbstractTestsBase {

    private WorkspaceComponentImpl workspaceComponent;

    @BeforeEach
    void setUp() {
        Configuration.init(Profile.Local, new Properties());
    }

    @Test
    void getWorkspaces_WhenThereAreNoWorkspaces() {
        Configuration.init(Profile.Local, new Properties());

        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public List<Long> getWorkspaceIds() {
                return new ArrayList<>();
            }
        });

        assertTrue(workspaceComponent.getWorkspaces().isEmpty());
    }

    @Test
    void getWorkspacesForUser_WhenAuthenticationDisabled() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_NONE);
        Configuration.init(Profile.Server, properties);

        Map<Long, WorkspaceMetadata> workspaceMap = new HashMap<>();

        WorkspaceMetadata workspace1 = new WorkspaceMetadata(1); // private workspace, read/write access
        workspace1.addWriteUser("user1");
        workspaceMap.put(workspace1.getId(), workspace1);

        WorkspaceMetadata workspace2 = new WorkspaceMetadata(2); // private workspace, read-only access
        workspace2.addWriteUser("user2");
        workspace2.addReadUser("user1");
        workspaceMap.put(workspace2.getId(), workspace2);

        WorkspaceMetadata workspace3 = new WorkspaceMetadata(3); // no users defined
        workspaceMap.put(workspace3.getId(), workspace3);

        WorkspaceMetadata workspace4 = new WorkspaceMetadata(4); // private workspace, no access
        workspace4.addWriteUser("user4");
        workspaceMap.put(workspace4.getId(), workspace4);

        WorkspaceMetadata workspace5 = new WorkspaceMetadata(5); // public workspace
        workspace5.addWriteUser("user5");
        workspace5.setPublicWorkspace(true);
        workspaceMap.put(workspace5.getId(), workspace5);

        WorkspaceComponentImpl workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public List<Long> getWorkspaceIds() {
                return new ArrayList<>(workspaceMap.keySet());
            }

            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMap.get(workspaceId);
            }
        });

        com.structurizr.server.domain.User user = new com.structurizr.server.domain.User("user1", new HashSet<>(), AuthenticationMethod.LOCAL);
        Collection<WorkspaceMetadata> workspaces = workspaceComponent.getWorkspaces(user);

        assertEquals(5, workspaces.size());

        assertTrue(workspaces.stream().anyMatch(w -> w.getId() == 1)); // private workspace, read/write access
        assertEquals("/workspace", workspaces.stream().filter(w -> w.getId() == 1).findFirst().get().getUrlPrefix());

        assertTrue(workspaces.stream().anyMatch(w -> w.getId() == 2)); // private workspace, read-only access
        assertEquals("/workspace", workspaces.stream().filter(w -> w.getId() == 2).findFirst().get().getUrlPrefix());

        assertTrue(workspaces.stream().anyMatch(w -> w.getId() == 3)); // no users defined
        assertEquals("/workspace", workspaces.stream().filter(w -> w.getId() == 3).findFirst().get().getUrlPrefix());

        assertTrue(workspaces.stream().anyMatch(w -> w.getId() == 4)); // private workspace, no access
        assertEquals("/workspace", workspaces.stream().filter(w -> w.getId() == 4).findFirst().get().getUrlPrefix());

        assertTrue(workspaces.stream().anyMatch(w -> w.getId() == 5)); // public workspace, no role-based access
        assertEquals("/workspace", workspaces.stream().filter(w -> w.getId() == 5).findFirst().get().getUrlPrefix());
    }

    @Test
    void getWorkspacesForUser_WhenAuthenticationEnabledAndUnauthenticated() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        Map<Long, WorkspaceMetadata> workspaceMap = new HashMap<>();

        WorkspaceMetadata workspace1 = new WorkspaceMetadata(1); // private workspace
        workspace1.addWriteUser("user1");
        workspaceMap.put(workspace1.getId(), workspace1);

        WorkspaceMetadata workspace2 = new WorkspaceMetadata(2); // public workspace
        workspace2.addWriteUser("user2");
        workspace2.setPublicWorkspace(true);
        workspaceMap.put(workspace2.getId(), workspace2);

        WorkspaceComponentImpl workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public List<Long> getWorkspaceIds() {
                return new ArrayList<>(workspaceMap.keySet());
            }

            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMap.get(workspaceId);
            }
        });

        Collection<WorkspaceMetadata> workspaces = workspaceComponent.getWorkspaces(null);

        assertEquals(1, workspaces.size());

        assertFalse(workspaces.stream().anyMatch(w -> w.getId() == 1)); // private workspace

        assertTrue(workspaces.stream().anyMatch(w -> w.getId() == 2)); // public workspace
        assertEquals("/share", workspaces.stream().filter(w -> w.getId() == 2).findFirst().get().getUrlPrefix());
    }

    @Test
    void getWorkspacesForUser_WhenAuthenticationEnabledAndAuthenticated() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        Map<Long, WorkspaceMetadata> workspaceMap = new HashMap<>();

        WorkspaceMetadata workspace1 = new WorkspaceMetadata(1); // private workspace, read/write access
        workspace1.addWriteUser("user1");
        workspaceMap.put(workspace1.getId(), workspace1);

        WorkspaceMetadata workspace2 = new WorkspaceMetadata(2); // private workspace, read-only access
        workspace2.addWriteUser("user2");
        workspace2.addReadUser("user1");
        workspaceMap.put(workspace2.getId(), workspace2);

        WorkspaceMetadata workspace3 = new WorkspaceMetadata(3); // no users defined
        workspaceMap.put(workspace3.getId(), workspace3);

        WorkspaceMetadata workspace4 = new WorkspaceMetadata(4); // private workspace, no access
        workspace4.addWriteUser("user4");
        workspaceMap.put(workspace4.getId(), workspace4);

        WorkspaceMetadata workspace5 = new WorkspaceMetadata(5); // public workspace
        workspace5.addWriteUser("user5");
        workspace5.setPublicWorkspace(true);
        workspaceMap.put(workspace5.getId(), workspace5);

        WorkspaceComponentImpl workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public List<Long> getWorkspaceIds() {
                return new ArrayList<>(workspaceMap.keySet());
            }

            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMap.get(workspaceId);
            }
        });

        com.structurizr.server.domain.User user = new com.structurizr.server.domain.User("user1", new HashSet<>(), AuthenticationMethod.LOCAL);
        Collection<WorkspaceMetadata> workspaces = workspaceComponent.getWorkspaces(user);

        assertEquals(4, workspaces.size());

        assertTrue(workspaces.stream().anyMatch(w -> w.getId() == 1)); // private workspace, read/write access
        assertEquals("/workspace", workspaces.stream().filter(w -> w.getId() == 1).findFirst().get().getUrlPrefix());

        assertTrue(workspaces.stream().anyMatch(w -> w.getId() == 2)); // private workspace, read-only access
        assertEquals("/workspace", workspaces.stream().filter(w -> w.getId() == 2).findFirst().get().getUrlPrefix());

        assertTrue(workspaces.stream().anyMatch(w -> w.getId() == 3)); // no users defined
        assertEquals("/workspace", workspaces.stream().filter(w -> w.getId() == 3).findFirst().get().getUrlPrefix());

        assertFalse(workspaces.stream().anyMatch(w -> w.getId() == 4)); // private workspace, no access

        assertTrue(workspaces.stream().anyMatch(w -> w.getId() == 5)); // public workspace, no role-based access
        assertEquals("/share", workspaces.stream().filter(w -> w.getId() == 5).findFirst().get().getUrlPrefix());
    }

    @Test
    void getWorkspacesForUser_WhenAuthenticationEnabledAndTheUserIsAnAdmin() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        Configuration.init(Profile.Server, properties);

        Map<Long, WorkspaceMetadata> workspaceMap = new HashMap<>();

        WorkspaceMetadata workspace1 = new WorkspaceMetadata(1); // private workspace, read/write access
        workspace1.addWriteUser("user1");
        workspaceMap.put(workspace1.getId(), workspace1);

        WorkspaceMetadata workspace2 = new WorkspaceMetadata(2); // private workspace, read-only access
        workspace2.addWriteUser("user2");
        workspace2.addReadUser("user1");
        workspaceMap.put(workspace2.getId(), workspace2);

        WorkspaceMetadata workspace3 = new WorkspaceMetadata(3); // no users defined
        workspaceMap.put(workspace3.getId(), workspace3);

        WorkspaceMetadata workspace4 = new WorkspaceMetadata(4); // private workspace, no access
        workspace4.addWriteUser("user4");
        workspaceMap.put(workspace4.getId(), workspace4);

        WorkspaceMetadata workspace5 = new WorkspaceMetadata(5); // public workspace
        workspace5.addWriteUser("user5");
        workspace5.setPublicWorkspace(true);
        workspaceMap.put(workspace5.getId(), workspace5);

        WorkspaceComponentImpl workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public List<Long> getWorkspaceIds() {
                return new ArrayList<>(workspaceMap.keySet());
            }

            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMap.get(workspaceId);
            }
        });

        com.structurizr.server.domain.User user = new com.structurizr.server.domain.User("admin@example.com", new HashSet<>(), AuthenticationMethod.LOCAL);
        Collection<WorkspaceMetadata> workspaces = workspaceComponent.getWorkspaces(user);

        // workspaces are visible
        assertEquals(5, workspaces.size());
        for (WorkspaceMetadata workspace : workspaces) {
            assertEquals("/workspace", workspace.getUrlPrefix());
        }
    }

    @Test
    void getWorkspace_WhenThereAreWorkspaces() {
        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public List<Long> getWorkspaceIds() {
                return List.of(1L, 2L);
            }

            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return new WorkspaceMetadata(workspaceId);
            }
        });

        List<WorkspaceMetadata> workspaces = workspaceComponent.getWorkspaces();
        assertEquals(2, workspaces.size());
        assertEquals(1L, workspaces.get(0).getId());
        assertEquals(2L, workspaces.get(1).getId());
    }

    @Test
    void getWorkspaces_WhenThereAreArchivedWorkspaces() {
        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public List<Long> getWorkspaceIds() {
                return List.of(1L, 2L);
            }

            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(workspaceId);
                if (workspaceId == 1L) {
                    wmd.setArchived(true);
                }

                return wmd;
            }
        });

        List<WorkspaceMetadata> workspaces = workspaceComponent.getWorkspaces();
        assertEquals(1, workspaces.size());
        assertEquals(2L, workspaces.get(0).getId());
    }

    @Test
    void getWorkspaceMetaData_WhenTheWorkspaceDoesNotExist() {
        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter());
        assertNull(workspaceComponent.getWorkspaceMetadata(1));
    }

    @Test
    void getWorkspaceMetaData_WhenTheWorkspaceExists() {
        WorkspaceMetadata wmd = new WorkspaceMetadata(1);

        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return wmd;
            }
        });

        assertSame(wmd, workspaceComponent.getWorkspaceMetadata(1));
    }

    @Test
    void getWorkspaceMetaData_WhenTheWorkspaceIsArchived() {
        WorkspaceMetadata wmd = new WorkspaceMetadata(1);
        wmd.setArchived(true);

        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return wmd;
            }
        });

        assertNull(workspaceComponent.getWorkspaceMetadata(1));
    }

    @Test
    void putWorkspaceMetadata_ThrowsAnException_WhenPassedNull() {
        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter());

        try {
            workspaceComponent.putWorkspaceMetadata(null);
            fail();
        } catch (Exception e) {
            assertEquals("Workspace metadata cannot be null", e.getMessage());
        }
    }

    @Test
    void putWorkspaceMetadata() {
        List<WorkspaceMetadata> workspaces = new ArrayList<>();

        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public void putWorkspaceMetadata(WorkspaceMetadata workspaceMetaData) {
                workspaces.add(workspaceMetaData);
            }
        });

        WorkspaceMetadata wmd = new WorkspaceMetadata(1);
        workspaceComponent.putWorkspaceMetadata(wmd);
        assertSame(wmd, workspaces.get(0));
    }

    @Test
    void getWorkspace_WhenTheWorkspaceDoesNotExist() {
        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter());

        try {
            workspaceComponent.getWorkspace(1, null, null);
            fail();
        } catch (Exception e) {
            assertEquals("Could not get workspace 1", e.getMessage());
        }
    }

    @Test
    void getWorkspace_WhenTheBranchNameIsInvalid() {
        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter());

        try {
            workspaceComponent.getWorkspace(1, "!branch", null);
            fail();
        } catch (Exception e) {
            assertEquals("The branch name \"!branch\" is invalid", e.getMessage());
        }
    }

    @Test
    public void getWorkspace_WhenServerSideEncryptionIsNotEnabled() {
        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public String getWorkspace(long workspaceId, String branch, String version) {
                return "json";
            }
        });

        String json = workspaceComponent.getWorkspace(1, "", "");
        assertEquals("json", json);
    }

    @Test
    void getWorkspace_WhenServerSideEncryptionIsEnabled() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ENCRYPTION_PASSPHRASE, "password");
        Configuration.init(Profile.Server, properties);

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public String getWorkspace(long workspaceId, String branch, String version) {
                String json = "";

                try {
                    Workspace workspace = new Workspace("Name", "Description");
                    workspace.setId(1);
                    EncryptionStrategy encryptionStrategy = new AesEncryptionStrategy("password");
                    encryptionStrategy.setLocation(EncryptionLocation.Server);
                    EncryptedWorkspace encryptedWorkspace = new EncryptedWorkspace(workspace, encryptionStrategy);
                    EncryptedJsonWriter encryptedJsonWriter = new EncryptedJsonWriter(false);
                    StringWriter stringWriter = new StringWriter();
                    encryptedJsonWriter.write(encryptedWorkspace, stringWriter);
                    json = stringWriter.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return json;
            }
        });

        String json = workspaceComponent.getWorkspace(1, "", "");
        assertEquals("""
                {"configuration":{},"description":"Description","documentation":{},"id":1,"model":{},"name":"Name","views":{"configuration":{"branding":{},"styles":{},"terminology":{}}}}""", json);
    }

    @Test
    void getWorkspace_WhenClientSideEncryptionIsEnabled() {
        try {
            Workspace workspace = new Workspace("Name", "Description");
            workspace.setId(1);
            EncryptionStrategy encryptionStrategy = new AesEncryptionStrategy("password");
            encryptionStrategy.setLocation(EncryptionLocation.Client);
            EncryptedWorkspace encryptedWorkspace = new EncryptedWorkspace(workspace, encryptionStrategy);
            EncryptedJsonWriter encryptedJsonWriter = new EncryptedJsonWriter(false);
            StringWriter stringWriter = new StringWriter();
            encryptedJsonWriter.write(encryptedWorkspace, stringWriter);
            final String json = stringWriter.toString();

            WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
                @Override
                public String getWorkspace(long workspaceId, String branch, String version) {
                    return json;
                }
            });
            assertEquals(json, workspaceComponent.getWorkspace(1, "", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void putWorkspace_WhenServerSideEncryptionIsNotEnabled() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        String json = WorkspaceUtils.toJson(workspace, false);

        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        final StringBuffer jsonBuffer = new StringBuffer();

        String expectedJson = """
                {"configuration":{},"description":"Description","documentation":{},"id":1,"lastModifiedDate":"%s","model":{},"name":"Name","views":{"configuration":{"branding":{},"styles":{},"terminology":{}}}}""";

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public void putWorkspaceMetadata(WorkspaceMetadata wmd) {
                workspaceMetaData.setLastModifiedDate(wmd.getLastModifiedDate());
            }

            @Override
            public void putWorkspace(WorkspaceMetadata workspaceMetaData, String json, String branch) {
                jsonBuffer.append(json);
            }
        });

        workspaceComponent.putWorkspace(1, "", json);
        assertEquals(String.format(expectedJson, DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate()), "1"), jsonBuffer.toString());

        // and again, to increment the revision
        json = jsonBuffer.toString();
        jsonBuffer.setLength(0);
        workspaceComponent.putWorkspace(1, "", json);
        assertEquals(String.format(expectedJson, DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate()), "2"), jsonBuffer.toString());
    }

    @Test
    void putWorkspace_WhenServerSideEncryptionIsEnabled() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ENCRYPTION_PASSPHRASE, "password");
        Configuration.init(Profile.Server, properties);

        Workspace workspace = new Workspace("Name", "Description");
        String json = WorkspaceUtils.toJson(workspace, false);

        final StringBuffer jsonBuffer = new StringBuffer();
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public void putWorkspaceMetadata(WorkspaceMetadata wmd) {
                workspaceMetaData.setLastModifiedDate(wmd.getLastModifiedDate());
            }

            @Override
            public void putWorkspace(WorkspaceMetadata workspaceMetaData, String json, String branch) {
                jsonBuffer.append(json);
            }
        });

        workspaceComponent.putWorkspace(1, "", json);
        String pattern = """
                "id":1,"lastModifiedDate":"%s","name":"Name"}""";
        assertTrue(jsonBuffer.toString().startsWith("{\"ciphertext\":\""));
        assertTrue(jsonBuffer.toString().endsWith(String.format(pattern, DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate()))));

        // and again, to increment the revision
        json = jsonBuffer.toString();
        jsonBuffer.setLength(0);
        workspaceComponent.putWorkspace(1, "", json);
        assertTrue(jsonBuffer.toString().startsWith("{\"ciphertext\":\""));
        assertTrue(jsonBuffer.toString().endsWith(String.format(pattern, DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate()))));
    }

    @Test
    void putWorkspace_WhenClientSideEncryptionIsEnabled() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        EncryptionStrategy encryptionStrategy = new AesEncryptionStrategy("passphrase");
        EncryptedWorkspace encryptedWorkspace = new EncryptedWorkspace(workspace, encryptionStrategy);
        EncryptedJsonWriter encryptedJsonWriter = new EncryptedJsonWriter(false);
        StringWriter stringWriter = new StringWriter();
        encryptedJsonWriter.write(encryptedWorkspace, stringWriter);
        String json = stringWriter.toString();

        final StringBuffer jsonBuffer = new StringBuffer();
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public void putWorkspaceMetadata(WorkspaceMetadata wmd) {
                workspaceMetaData.setLastModifiedDate(wmd.getLastModifiedDate());
            }

            @Override
            public void putWorkspace(WorkspaceMetadata workspaceMetaData, String json, String branch) {
                jsonBuffer.append(json);
            }
        });

        workspaceComponent.putWorkspace(1, "", json);
        String pattern = """
                "id":1,"lastModifiedDate":"%s","name":"Name"}""";
        assertTrue(jsonBuffer.toString().startsWith("{\"ciphertext\":\""));
        assertTrue(jsonBuffer.toString().endsWith(String.format(pattern, DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate()))));

        // and again, to increment the revision
        json = jsonBuffer.toString();
        jsonBuffer.setLength(0);
        workspaceComponent.putWorkspace(1, "", json);
        assertTrue(jsonBuffer.toString().startsWith("{\"ciphertext\":\""));
        assertTrue(jsonBuffer.toString().endsWith(String.format(pattern, DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate()))));
    }

    @Test
    void test_putWorkspace_UpdatesTheVisibility_WhenTheVisibilityIsSpecified() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        Workspace workspace = new Workspace("Name", "Description");
        workspace.getConfiguration().setVisibility(Visibility.Public);

        String json = WorkspaceUtils.toJson(workspace, false);

        final WorkspaceMetadata workspaceMetadata = new WorkspaceMetadata(1);
        workspaceMetadata.setPublicWorkspace(false);

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }
        });

        workspaceComponent.putWorkspace(1, "", json);

        assertTrue(workspaceMetadata.isPublicWorkspace());
    }

    @Test
    void test_putWorkspace_DoesNotUpdateTheVisibility_WhenAuthenticationIsEnabledAndAdminUsersAreDefined() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        Configuration.init(Profile.Server, properties);

        Workspace workspace = new Workspace("Name", "Description");
        workspace.getConfiguration().setVisibility(Visibility.Public);

        String json = WorkspaceUtils.toJson(workspace, false);

        final WorkspaceMetadata workspaceMetadata = new WorkspaceMetadata(1);
        workspaceMetadata.setPublicWorkspace(false);

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }
        });

        workspaceComponent.putWorkspace(1, "", json);

        assertFalse(workspaceMetadata.isPublicWorkspace());
    }

    @Test
    void test_putWorkspace_DoesNotUpdateTheVisibility_WhenTheVisibilityIsNotSpecified() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");

        String json = WorkspaceUtils.toJson(workspace, false);

        final WorkspaceMetadata wmd = new WorkspaceMetadata(1);
        wmd.setPublicWorkspace(false);

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public void putWorkspaceMetadata(WorkspaceMetadata workspaceMetaData) {
                wmd.setPublicWorkspace(workspaceMetaData.isPublicWorkspace());
            }
        });

        workspaceComponent.putWorkspace(1, "", json);

        assertFalse(wmd.isPublicWorkspace());
    }

    @Test
    void test_putWorkspace_UpdatesTheRoleBasedSecurity_WhenUsersAreDefined() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        Workspace workspace = new Workspace("Name", "Description");
        workspace.getConfiguration().addUser("user1@example.com", Role.ReadWrite);
        workspace.getConfiguration().addUser("user2@example.com", Role.ReadWrite);
        workspace.getConfiguration().addUser("user3@example.com", Role.ReadOnly);
        workspace.getConfiguration().addUser("user4@example.com", Role.ReadOnly);
        String json = WorkspaceUtils.toJson(workspace, false);

        WorkspaceMetadata workspaceMetadata = new WorkspaceMetadata(1);
        workspaceMetadata.addWriteUser("write@example.com");
        workspaceMetadata.addReadUser("read@example.com");

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }
        });

        workspaceComponent.putWorkspace(1, "", json);

        assertEquals(2, workspaceMetadata.getWriteUsers().size());
        assertTrue(workspaceMetadata.getWriteUsers().contains("user1@example.com"));
        assertTrue(workspaceMetadata.getWriteUsers().contains("user2@example.com"));
        assertEquals(2, workspaceMetadata.getReadUsers().size());
        assertTrue(workspaceMetadata.getReadUsers().contains("user3@example.com"));
        assertTrue(workspaceMetadata.getReadUsers().contains("user4@example.com"));
    }

    @Test
    void putWorkspace_DoesNotUpdateTheRoleBasedSecurity_WhenUsersAreNotDefined() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        String json = WorkspaceUtils.toJson(workspace, false);

        WorkspaceMetadata workspaceMetadata = new WorkspaceMetadata(1);
        workspaceMetadata.addWriteUser("write@example.com");
        workspaceMetadata.addReadUser("read@example.com");

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }
        });

        workspaceComponent.putWorkspace(1, "", json);

        assertEquals(1, workspaceMetadata.getWriteUsers().size());
        assertTrue(workspaceMetadata.getWriteUsers().contains("write@example.com"));
        assertEquals(1, workspaceMetadata.getReadUsers().size());
        assertTrue(workspaceMetadata.getReadUsers().contains("read@example.com"));
    }

    @Test
    void putWorkspace_DoesNotUpdateTheRoleBasedSecurity_WhenAuthenticationIsEnabledAndAdminUsersAreDefined() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        Configuration.init(Profile.Server, properties);

        Workspace workspace = new Workspace("Name", "Description");
        workspace.getConfiguration().addUser("user1@example.com", Role.ReadWrite);
        workspace.getConfiguration().addUser("user2@example.com", Role.ReadWrite);
        workspace.getConfiguration().addUser("user3@example.com", Role.ReadOnly);
        workspace.getConfiguration().addUser("user4@example.com", Role.ReadOnly);
        String json = WorkspaceUtils.toJson(workspace, false);

        WorkspaceMetadata workspaceMetadata = new WorkspaceMetadata(1);
        workspaceMetadata.addWriteUser("write@example.com");
        workspaceMetadata.addReadUser("read@example.com");

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetadata;
            }
        });

        workspaceComponent.putWorkspace(1, "", json);

        assertEquals(1, workspaceMetadata.getWriteUsers().size());
        assertTrue(workspaceMetadata.getWriteUsers().contains("write@example.com"));
        assertEquals(1, workspaceMetadata.getReadUsers().size());
        assertTrue(workspaceMetadata.getReadUsers().contains("read@example.com"));
    }

    @Test
    void test_putWorkspace_ThrowsAnException_WhenWorkspaceScopeValidationIsStrictAndTheWorkspaceIsUnscoped() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.getConfiguration().setScope(null);

        String json = WorkspaceUtils.toJson(workspace, false);

        final WorkspaceMetadata wmd = new WorkspaceMetadata(1);
        wmd.setPublicWorkspace(false);

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter());
        try {
            Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_SCOPE_VALIDATION);
            workspaceComponent.putWorkspace(1, "", json);
            fail();
        } catch (WorkspaceComponentException e) {
            assertEquals("Strict workspace scope validation has been enabled for this installation. Unscoped workspaces are not permitted - see https://docs.structurizr.com/basics/workspace-scope for more information.", e.getMessage());
        }
    }

    @Test
    void test_putWorkspace_ThrowsAnException_WhenWorkspaceScopeValidationFails() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.getConfiguration().setScope(WorkspaceScope.Landscape);
        workspace.getModel().addSoftwareSystem("A").addContainer("AA");

        String json = WorkspaceUtils.toJson(workspace, false);

        final WorkspaceMetadata wmd = new WorkspaceMetadata(1);
        wmd.setPublicWorkspace(false);

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter());
        try {
            workspaceComponent.putWorkspace(1, "", json);
            fail();
        } catch (WorkspaceComponentException e) {
            assertEquals("Workspace is landscape scoped, but the software system named A has containers.", e.getMessage());
        }
    }

    @Test
    void putImage_ThrowsException_WhenPuttingAFileThatIsNotAnImage() {
        workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter());

        try {
            workspaceComponent.putImage(1, "", "xss.js", new File("xss.js"));
            fail();
        } catch (WorkspaceComponentException e) {
            assertEquals("xss.js is not an image", e.getMessage());
        }
    }

    @Test
    void createWorkspace() throws Exception {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        final StringBuffer jsonBuffer = new StringBuffer();

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public void putWorkspaceMetadata(WorkspaceMetadata wmd) {
                workspaceMetaData.setLastModifiedDate(wmd.getLastModifiedDate());
            }

            @Override
            public void putWorkspace(WorkspaceMetadata workspaceMetaData, String json, String branch) {
                jsonBuffer.append(json);
            }
        });

        long workspaceId = workspaceComponent.createWorkspace(null);

        assertEquals(1, workspaceId);
        assertEquals(String.format("""
                {"configuration":{},"description":"Description","documentation":{},"id":1,"lastModifiedDate":"%s","model":{},"name":"Workspace 0001","views":{"configuration":{"branding":{},"styles":{},"terminology":{}}}}""", DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate())), jsonBuffer.toString());
    }

    @Test
    void deleteWorkspace() {
        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public boolean deleteWorkspace(long workspaceId) {
                return true;
            }
        });

        assertTrue(workspaceComponent.deleteWorkspace(1));
    }

    @Test
    void deleteWorkspace_WhenArchivingIsEnabled() {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);

        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_ARCHIVING);

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void putWorkspaceMetadata(WorkspaceMetadata wmd) {
                workspaceMetaData.setArchived(wmd.isArchived());
            }
        });

        assertTrue(workspaceComponent.deleteWorkspace(1));
        assertTrue(workspaceMetaData.isArchived());
    }

    @Test
    public void shareWorkspace() {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(workspaceId);
                wmd.setSharingToken("");

                return wmd;
            }

            @Override
            public void putWorkspaceMetadata(WorkspaceMetadata wmd) {
                workspaceMetaData.setSharingToken(wmd.getSharingToken());
            }
        });

        workspaceComponent.shareWorkspace(1);
        assertEquals(36, workspaceMetaData.getSharingToken().length());
    }

    @Test
    void unshareWorkspace() {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(workspaceId);
                wmd.setSharingToken("1234567890");

                return wmd;
            }

            @Override
            public void putWorkspaceMetadata(WorkspaceMetadata wmd) {
                workspaceMetaData.setSharingToken(wmd.getSharingToken());
            }
        });

        workspaceComponent.unshareWorkspace(1);
        assertEquals("", workspaceMetaData.getSharingToken());
    }

    @Test
    void lockWorkspace_LocksTheWorkspace_WhenItIsAlreadyLockedByTheSameUser() {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                WorkspaceMetadata wmd = new WorkspaceMetadata(1);
                wmd.setLockedUser("user1");
                wmd.setLockedAgent("agent");
                wmd.setLockedDate(new Date());

                return wmd;
            }

            @Override
            public void putWorkspaceMetadata(WorkspaceMetadata wmd) {
                workspaceMetaData.setLockedUser(wmd.getLockedUser());
                workspaceMetaData.setLockedAgent(wmd.getLockedAgent());
                workspaceMetaData.setLockedDate(wmd.getLockedDate());
            }
        });

        boolean locked = workspaceComponent.lockWorkspace(1, "user1", "agent");
        assertTrue(locked);

        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user1", workspaceMetaData.getLockedUser());
        assertEquals("agent", workspaceMetaData.getLockedAgent());
        assertFalse(DateUtils.isOlderThanXMinutes(workspaceMetaData.getLockedDate(), 1));
    }

    @Test
    void lockWorkspace_DoesNotLockTheWorkspace_WhenItIsAlreadyLocked() {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setLockedUser("user1");
        workspaceMetaData.setLockedAgent("agent");
        workspaceMetaData.setLockedDate(new Date());

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void putWorkspaceMetadata(WorkspaceMetadata wmd) {
                workspaceMetaData.setLockedUser(wmd.getLockedUser());
                workspaceMetaData.setLockedAgent(wmd.getLockedAgent());
                workspaceMetaData.setLockedDate(wmd.getLockedDate());
            }
        });

        boolean locked = workspaceComponent.lockWorkspace(1, "user2", "agent");
        assertFalse(locked);

        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user1", workspaceMetaData.getLockedUser());
        assertEquals("agent", workspaceMetaData.getLockedAgent());
        assertFalse(DateUtils.isOlderThanXMinutes(workspaceMetaData.getLockedDate(), 1));
    }

    @Test
    void lockWorkspace_LocksTheWorkspace_WhenThePreviousLockHasExpired() {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setLockedUser("user1");
        workspaceMetaData.setLockedAgent("agent");
        workspaceMetaData.setLockedDate(DateUtils.getXMinutesAgo(10));

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void putWorkspaceMetadata(WorkspaceMetadata wmd) {
                workspaceMetaData.setLockedUser(wmd.getLockedUser());
                workspaceMetaData.setLockedAgent(wmd.getLockedAgent());
                workspaceMetaData.setLockedDate(wmd.getLockedDate());
            }
        });

        boolean locked = workspaceComponent.lockWorkspace(1, "user2", "agent");
        assertTrue(locked);

        assertTrue(workspaceMetaData.isLocked());
        assertEquals("user2", workspaceMetaData.getLockedUser());
        assertEquals("agent", workspaceMetaData.getLockedAgent());
        assertFalse(DateUtils.isOlderThanXMinutes(workspaceMetaData.getLockedDate(), 1));
    }

    @Test
    void unlockWorkspace_UnlocksTheWorkspace_WhenItIsAlreadyLocked() {
        final WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setLockedUser("user1");
        workspaceMetaData.setLockedAgent("agent");
        workspaceMetaData.setLockedDate(DateUtils.getXMinutesAgo(10));

        WorkspaceComponent workspaceComponent = new WorkspaceComponentImpl(new MockWorkspaceAdapter() {
            @Override
            public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) {
                return workspaceMetaData;
            }

            @Override
            public void putWorkspaceMetadata(WorkspaceMetadata wmd) {
                workspaceMetaData.setLockedUser(wmd.getLockedUser());
                workspaceMetaData.setLockedAgent(wmd.getLockedAgent());
                workspaceMetaData.setLockedDate(wmd.getLockedDate());
            }
        });

        boolean unlocked = workspaceComponent.unlockWorkspace(1);
        assertTrue(unlocked);

        assertFalse(workspaceMetaData.isLocked());
        assertNull(workspaceMetaData.getLockedUser());
        assertNull(workspaceMetaData.getLockedAgent());
        assertNull(workspaceMetaData.getLockedDate());
    }

}
