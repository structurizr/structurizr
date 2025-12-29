package com.structurizr.server.component.workspace;

public class WorkspaceComponentTests {

//    private static final File DATA_DIRECTORY = new File("./build/WorkspaceComponentTests");
//    private WorkspaceComponent workspaceComponent;
//
//    @BeforeEach
//    public void setUp() {
//        deleteDirectory(DATA_DIRECTORY);
//
//        WorkspaceDao dao = new ServerFileSystemWorkspaceDao(DATA_DIRECTORY);
//        workspaceComponent = new WorkspaceComponentImpl(dao, "");
//
//        Configuration.init();
//    }
//
//    @Test
//    public void test() throws Exception {
//        SimpleDateFormat sdf = new SimpleDateFormat(ServerFileSystemWorkspaceDao.VERSION_TIMESTAMP_FORMAT);
//        sdf.setTimeZone(TimeZone.getTimeZone(UTC_TIME_ZONE));
//
//        User user = new User("user@example.com", new HashSet<>(), AuthenticationMethod.LOCAL);
//        long workspaceId = workspaceComponent.createWorkspace(user);
//        assertEquals(1, workspaceId);
//
//        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(1);
//        String jsonV1 = String.format("""
//                {"configuration":{},"description":"Description","documentation":{},"id":1,"lastModifiedDate":"%s","model":{},"name":"Workspace 0001","views":{"configuration":{"branding":{},"styles":{},"terminology":{}}}}""", DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate()));
//        assertEquals(jsonV1, workspaceComponent.getWorkspace(1, "", ""));
//
//        Collection<WorkspaceMetaData> workspaces = workspaceComponent.getWorkspaces();
//        assertEquals(1, workspaces.size());
//        assertEquals(workspaceId, workspaces.iterator().next().getId());
//
//        List<WorkspaceVersion> workspaceVersions = workspaceComponent.getWorkspaceVersions(1, "");
//        assertEquals(1, workspaceVersions.size());
//        WorkspaceVersion version1 = workspaceVersions.get(0); // keep this for later
//        assertNull(workspaceVersions.get(0).getVersionId());
//        assertEquals(DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate()), DateUtils.formatIsoDate(workspaceVersions.get(0).getLastModifiedDate()));
//
//        Thread.sleep(2 * 1000); // sleep for a couple of seconds, otherwise all workspace versions have the same timestamp
//
//        Workspace workspace = new Workspace("Financial Risk System", "...");
//        String json = WorkspaceUtils.toJson(workspace, false);
//        workspaceComponent.putWorkspace(1, "", json);
//
//        workspaceMetaData = workspaceComponent.getWorkspaceMetaData(1);
//        String jsonV2 = String.format("""
//                {"configuration":{},"description":"...","documentation":{},"id":1,"lastModifiedDate":"%s","model":{},"name":"Financial Risk System","views":{"configuration":{"branding":{},"styles":{},"terminology":{}}}}""", DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate()));
//        assertEquals(jsonV2, workspaceComponent.getWorkspace(1, "", ""));
//
//        workspaceVersions = workspaceComponent.getWorkspaceVersions(1, "");
//        assertEquals(2, workspaceVersions.size());
//        assertNull(workspaceVersions.get(0).getVersionId());
//        assertEquals(DateUtils.formatIsoDate(workspaceMetaData.getLastModifiedDate()), DateUtils.formatIsoDate(workspaceVersions.get(0).getLastModifiedDate()));
//
//        assertEquals(sdf.format(version1.getLastModifiedDate()), workspaceVersions.get(1).getVersionId());
//        assertEquals(DateUtils.formatIsoDate(version1.getLastModifiedDate()), DateUtils.formatIsoDate(workspaceVersions.get(1).getLastModifiedDate()));
//
//        json = workspaceComponent.getWorkspace(1, "", sdf.format(version1.getLastModifiedDate()));
//        assertEquals(jsonV1, json);
//
//        try {
//            workspaceComponent.getWorkspace(1, "", "1234567890"); // invalid workspace version
//            fail();
//        } catch (WorkspaceComponentException e) {
//            assertEquals("Could not get workspace 1 with version 1234567890", e.getMessage());
//        }
//
//        boolean result = workspaceComponent.deleteWorkspace(1);
//        assertTrue(result);
//
//        try {
//            assertNull(workspaceComponent.getWorkspaceMetaData(1));
//            workspaceComponent.getWorkspace(1, "", "");
//            fail();
//        } catch (WorkspaceComponentException e) {
//            assertEquals("Could not get workspace 1", e.getMessage());
//        }
//    }
//
//    @Test
//    public void deleteWorkspace() throws Exception {
//        User user = new User("user@example.com", new HashSet<>(), AuthenticationMethod.LOCAL);
//        long workspaceId = workspaceComponent.createWorkspace(user);
//        assertEquals(1, workspaceId);
//
//        Configuration.getInstance().setFeatureDisabled(Features.WORKSPACE_ARCHIVING);
//        assertTrue(workspaceComponent.deleteWorkspace(1));
//        assertFalse(new File(DATA_DIRECTORY, "1").exists());
//
//        // create a new workspace - the ID should be recycled
//        workspaceId = workspaceComponent.createWorkspace(user);
//        assertEquals(1, workspaceId);
//
//        Configuration.getInstance().setFeatureEnabled(Features.WORKSPACE_ARCHIVING);
//        assertTrue(workspaceComponent.deleteWorkspace(1));
//        assertTrue(new File(DATA_DIRECTORY, "1").exists());
//        assertNull(workspaceComponent.getWorkspaceMetaData(1));
//
//        // with workspace archiving enabled, the workspace isn't deleted, so we get a new ID
//        workspaceId = workspaceComponent.createWorkspace(user);
//        assertEquals(2, workspaceId);
//    }
//
//    @AfterEach
//    public void tearDown() {
//        deleteDirectory(DATA_DIRECTORY);
//    }
//

}