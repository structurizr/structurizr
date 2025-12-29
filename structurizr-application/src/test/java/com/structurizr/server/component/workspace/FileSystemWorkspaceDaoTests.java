package com.structurizr.server.component.workspace;

public class FileSystemWorkspaceDaoTests {

//    private static final File DATA_DIRECTORY = new File("./build/FileSystemWorkspaceDaoTests");
//
//    @BeforeEach
//    public void setUp() {
//        deleteDirectory(DATA_DIRECTORY);
//    }
//
//    @Test
//    public void test_removeOldWorkspaceVersions() throws Exception {
//        File workspaceDirectory = new File(DATA_DIRECTORY, "1");
//        workspaceDirectory.mkdirs();
//
//        File workspaceJson = new File(workspaceDirectory, "workspace.json");
//        workspaceJson.createNewFile();
//
//        Calendar cal = DateUtils.getCalendar();
//        SimpleDateFormat sdf = new SimpleDateFormat(FileSystemWorkspaceDao.VERSION_TIMESTAMP_FORMAT);
//        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//
//        List<String> filenames = new ArrayList<>();
//        for (int i = 0; i < 50; i++) {
//            cal.add(Calendar.MINUTE, -i);
//            String filename = "workspace-" + sdf.format(cal.getTime()) + ".json";
//            File version = new File(workspaceDirectory, filename);
//            version.createNewFile();
//            filenames.add(filename);
//        }
//
//        FileSystemWorkspaceDao dao = new FileSystemWorkspaceDao(DATA_DIRECTORY);
//        dao.removeOldWorkspaceVersions(30);
//
//        File[] files = workspaceDirectory.listFiles((dir, name) -> name.matches(FileSystemWorkspaceDao.WORKSPACE_VERSION_JSON_FILENAME_REGEX));
//        assertEquals(30, files.length);
//        assertTrue(workspaceJson.exists());
//        for (int i = 0; i < 30; i++) {
//            String filename = filenames.get(i);
//            assertTrue(new File(workspaceDirectory, filename).exists());
//        }
//
//        dao.removeOldWorkspaceVersions(10);
//        files = workspaceDirectory.listFiles((dir, name) -> name.matches(FileSystemWorkspaceDao.WORKSPACE_VERSION_JSON_FILENAME_REGEX));
//        assertEquals(10, files.length);
//        assertTrue(workspaceJson.exists());
//        for (int i = 0; i < 10; i++) {
//            String filename = filenames.get(i);
//            assertTrue(new File(workspaceDirectory, filename).exists());
//        }
//    }
//
//    @AfterEach
//    public void tearDown() {
//        deleteDirectory(DATA_DIRECTORY);
//    }
//
//    private void deleteDirectory(File directory) {
//        File[] files = directory.listFiles();
//        if (files != null) {
//            for (File file : files) {
//                if (file.isDirectory()) {
//                    deleteDirectory(file);
//                } else {
//                    file.delete();
//                }
//            }
//        }
//    }

}