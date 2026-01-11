package com.structurizr.server.domain;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class WorkspaceMetadataTests {

    @Test
    void getName_WhenNull() {
        WorkspaceMetadata workspace = new WorkspaceMetadata(1);
        workspace.setName(null);
        assertEquals("Workspace 1", workspace.getName());
    }

    @Test
    void isPublic() {
        WorkspaceMetadata workspace = new WorkspaceMetadata(1);
        assertFalse(workspace.isPublicWorkspace());

        workspace.setPublicWorkspace(true);
        assertTrue(workspace.isPublicWorkspace());
    }

    @Test
    void isPublicWorkspace() {
        WorkspaceMetadata workspace = new WorkspaceMetadata(1);
        assertFalse(workspace.isPublicWorkspace()); // workspaces are private by default

        workspace.setPublicWorkspace(true);
        assertTrue(workspace.isPublicWorkspace());
    }

    @Test
    void sharingToken() {
        WorkspaceMetadata workspace = new WorkspaceMetadata(1);
        assertEquals("", workspace.getSharingToken());
        assertFalse(workspace.isShareable());

        workspace.setSharingToken("12345678901234567890");
        assertEquals("12345678901234567890", workspace.getSharingToken());
        assertEquals("123456...", workspace.getSharingTokenTruncated());
        assertTrue(workspace.isShareable());
    }

    @Test
    void addReadUser_WhenNull() {
        WorkspaceMetadata workspace = new WorkspaceMetadata(1);
        workspace.addReadUser(null);
        assertEquals(0, workspace.getReadUsers().size());
    }

    @Test
    void addReadUser_WhenEmpty() {
        WorkspaceMetadata workspace = new WorkspaceMetadata(1);
        workspace.addReadUser("");
        assertEquals(0, workspace.getReadUsers().size());
    }

    @Test
    void addReadUser_DoesAddUserMultipleTimes() {
        WorkspaceMetadata workspace = new WorkspaceMetadata(1);
        workspace.addReadUser("read@example.com");
        workspace.addReadUser("read@example.com");
        workspace.addReadUser("READ@example.com");
        assertEquals(1, workspace.getReadUsers().size());
    }

    @Test
    void addWriteUser_WhenNull() {
        WorkspaceMetadata workspace = new WorkspaceMetadata(1);
        workspace.addWriteUser(null);
        assertEquals(0, workspace.getWriteUsers().size());
    }

    @Test
    void addWriteUser_WhenEmpty() {
        WorkspaceMetadata workspace = new WorkspaceMetadata(1);
        workspace.addWriteUser("");
        assertEquals(0, workspace.getWriteUsers().size());
    }

    @Test
    void addWriteUser_DoesAddUserMultipleTimes() {
        WorkspaceMetadata workspace = new WorkspaceMetadata(1);
        workspace.addWriteUser("write@example.com");
        workspace.addWriteUser("write@example.com");
        workspace.addWriteUser("WRITE@example.com");
        assertEquals(1, workspace.getWriteUsers().size());
    }

    @Test
    void hasNoUsersConfigured_ReturnsTrue_WhenTheWorkspaceHasNoConfiguredUsers() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetadata workspace = new WorkspaceMetadata(1);
        assertTrue(workspace.hasNoUsersConfigured());
    }

    @Test
    void hasNoUsersConfigured_ReturnsFalse_WhenAuthenticationIsEnabledAndTheWorkspaceHasAReadOnlyUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetadata workspace = new WorkspaceMetadata(1);
        workspace.addReadUser("read@example.com");
        assertFalse(workspace.hasNoUsersConfigured());
    }

    @Test
    void hasNoUsersConfigured_ReturnsFalse_WhenAuthenticationIsEnabledAndTheWorkspaceHasAReadWriteUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetadata workspace = new WorkspaceMetadata(1);
        workspace.addWriteUser("write@example.com");
        assertFalse(workspace.hasNoUsersConfigured());
    }

    @Test
    void isReadUser_ReturnsFalse_WhenAuthenticationIsEnabledAndTheUserIsNotAReadUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetadata workspace = new WorkspaceMetadata(1);
        workspace.addReadUser("read@example.com");

        User user = new User("user@example.com");
        assertFalse(workspace.isReadUser(user));
    }

    @Test
    void isReadUser_ReturnsTrue_WhenTheUserIsAReadUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetadata workspace = new WorkspaceMetadata(1);
        workspace.addReadUser("read@example.com");

        User user = new User("read@example.com");
        assertTrue(workspace.isReadUser(user));
    }

    @Test
    void isReadUser_ReturnsFalse_WhenAuthenticationIsEnabledAndTheUserRoleIsNotAReadUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetadata workspace = new WorkspaceMetadata(1);
        workspace.addReadUser("role1");

        User user = new User("user@example.com", Set.of("role2"), AuthenticationMethod.LOCAL);
        assertFalse(workspace.isReadUser(user));
    }

    @Test
    void isReadUser_ReturnsTrue_WhenAuthenticationIsEnabledAndTheUserRoleIsAReadUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetadata workspace = new WorkspaceMetadata(1);
        workspace.addReadUser("role1");

        User user = new User("user@example.com", Set.of("role1", "role2"), AuthenticationMethod.LOCAL);
        assertTrue(workspace.isReadUser(user));
    }

    @Test
    void isWriteUser_ReturnsFalse_WhenAuthenticationIsEnabledAndTheUserIsNotAWriteUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetadata workspace = new WorkspaceMetadata(1);
        workspace.addWriteUser("write@example.com");

        User user = new User("user@example.com");
        assertFalse(workspace.isWriteUser(user));
    }

    @Test
    void isWriteUser_ReturnsTrue_WhenAuthenticationIsEnabledAndTheUserIsAWriteUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetadata workspace = new WorkspaceMetadata(1);
        workspace.addWriteUser("write@example.com");

        User user = new User("write@example.com");
        assertTrue(workspace.isWriteUser(user));
    }

    @Test
    void test_isWriteUser_ReturnsFalse_WhenAuthenticationIsEnabledAndTheUserRoleIsNotAWriteUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetadata workspace = new WorkspaceMetadata(1);
        workspace.addWriteUser("role1");

        User user = new User("user", new HashSet<>(List.of("role2")), AuthenticationMethod.LOCAL);
        assertFalse(workspace.isWriteUser(user));
    }

    @Test
    void test_isWriteUser_ReturnsTrue_WhenAuthenticationIsEnabledAndTheUserRoleIsAWriteUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetadata workspace = new WorkspaceMetadata(1);
        workspace.addWriteUser("role1");

        User user = new User("user", new HashSet<>(List.of("role1", "role2")), AuthenticationMethod.LOCAL);
        assertTrue(workspace.isWriteUser(user));
    }

    @Test
    void test_isLocked_ReturnsFalse_WhenTheWorkspaceIsNotLocked() {
        WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setLockedUser(null);
        workspaceMetaData.setLockedDate(null);
        assertFalse(workspaceMetaData.isLocked());
    }

    @Test
    void test_isLocked_ReturnsTrue_WhenTheWorkspaceIsLocked() {
        WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setLockedUser("user");
        workspaceMetaData.setLockedDate(new Date());

        assertTrue(workspaceMetaData.isLocked());
    }

    @Test
    void test_isLocked_ReturnsFalse_WhenTheWorkspaceWasLockedOverTwoMinutesAgo() {
        WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setLockedUser("user");
        workspaceMetaData.setLockedDate(DateUtils.getXMinutesAgo(3));

        assertFalse(workspaceMetaData.isLocked());
    }

    @Test
    void test_isLockedBy_ReturnsFalse_WhenTheWorkspaceIsNotLocked() {
        WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setLockedUser(null);
        workspaceMetaData.setLockedDate(null);
        assertFalse(workspaceMetaData.isLockedBy("user", "agent"));
    }

    @Test
    void test_isLockedBy_ReturnsTrue_WhenTheWorkspaceIsLockedByTheSpecifiedUserAndAgent() {
        WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setLockedUser("user");
        workspaceMetaData.setLockedAgent("agent");
        workspaceMetaData.setLockedDate(new Date());

        assertTrue(workspaceMetaData.isLockedBy("user", "agent"));
    }

    @Test
    void test_isLockedBy_ReturnsFalse_WhenTheWorkspaceIsNotLockedByTheSpecifiedUserAndAgent() {
        WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setLockedUser("user1");
        workspaceMetaData.setLockedAgent("agent1");
        workspaceMetaData.setLockedDate(new Date());

        assertFalse(workspaceMetaData.isLockedBy("user1", "agent2"));
    }

    @Test
    void test_isLockedBy_ReturnsFalse_WhenTheWorkspaceIsNotLockedByTheSpecifiedUser() {
        WorkspaceMetadata workspaceMetaData = new WorkspaceMetadata(1);
        workspaceMetaData.setLockedUser("user1");
        workspaceMetaData.setLockedAgent("agent1");
        workspaceMetaData.setLockedDate(new Date());

        assertFalse(workspaceMetaData.isLockedBy("user2", "agent2"));
    }

    @Test
    void fromProperties_and_toProperties() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(WorkspaceMetadata.NAME_PROPERTY, "Name");
        properties.setProperty(WorkspaceMetadata.DESCRIPTION_PROPERTY, "Description");
        properties.setProperty(WorkspaceMetadata.VERSION_PROPERTY, "v1.2.3");
        properties.setProperty(WorkspaceMetadata.CLIENT_SIDE_ENCRYPTED_PROPERTY, "false");
        properties.setProperty(WorkspaceMetadata.LAST_MODIFIED_USER_PROPERTY, "user1");
        properties.setProperty(WorkspaceMetadata.LAST_MODIFIED_AGENT_PROPERTY, "structurizr/dsl");
        properties.setProperty(WorkspaceMetadata.LAST_MODIFIED_DATE_PROPERTY, "2021-01-31T14:30:59Z");
        properties.setProperty(WorkspaceMetadata.API_KEY_PROPERTY, "1234567890");
        properties.setProperty(WorkspaceMetadata.API_SECRET_PROPERTY, "0987654321");
        properties.setProperty(WorkspaceMetadata.PUBLIC_PROPERTY, "true");
        properties.setProperty(WorkspaceMetadata.SHARING_TOKEN_PROPERTY, "12345678901234567890");
        properties.setProperty(WorkspaceMetadata.LOCKED_USER_PROPERTY, "user@example.com");
        properties.setProperty(WorkspaceMetadata.LOCKED_AGENT_PROPERTY, "structurizr/web");
        properties.setProperty(WorkspaceMetadata.LOCKED_DATE_PROPERTY, "2022-01-31T14:30:59Z");
        properties.setProperty(WorkspaceMetadata.READ_USERS_AND_ROLES_PROPERTY, "user1,user2,user3");
        properties.setProperty(WorkspaceMetadata.WRITE_USERS_AND_ROLES_PROPERTY, "user4,user5,user6");
        properties.setProperty(WorkspaceMetadata.ARCHIVED_PROPERTY, "false");

        WorkspaceMetadata workspace = WorkspaceMetadata.fromProperties(123, properties);

        assertEquals("Name", workspace.getName());
        assertEquals("Description", workspace.getDescription());
        assertEquals("v1.2.3", workspace.getVersion());
        assertFalse(workspace.isClientEncrypted());
        assertEquals("user1", workspace.getLastModifiedUser());
        assertEquals("structurizr/dsl", workspace.getLastModifiedAgent());
        assertEquals(DateUtils.parseIsoDate("2021-01-31T14:30:59Z"), workspace.getLastModifiedDate());
        assertEquals("1234567890", workspace.getApiKey());
        assertEquals("0987654321", workspace.getApiSecret());
        assertFalse(workspace.isPublicWorkspace()); // sharing token is active, and takes priority
        assertEquals("12345678901234567890", workspace.getSharingToken());
        assertEquals("user@example.com", workspace.getLockedUser());
        assertEquals("structurizr/web", workspace.getLockedAgent());
        assertEquals(DateUtils.parseIsoDate("2022-01-31T14:30:59Z"), workspace.getLockedDate());
        assertEquals(Set.of("user1", "user2", "user3"), workspace.getReadUsers());
        assertEquals(Set.of("user4", "user5", "user6"), workspace.getWriteUsers());
        assertFalse(workspace.isArchived());

        properties.clear();
        properties = workspace.toProperties();

        assertEquals("Name", properties.getProperty(WorkspaceMetadata.NAME_PROPERTY));
        assertEquals("Description", properties.getProperty(WorkspaceMetadata.DESCRIPTION_PROPERTY));
        assertEquals("v1.2.3", properties.getProperty(WorkspaceMetadata.VERSION_PROPERTY));
        assertEquals("false", properties.getProperty(WorkspaceMetadata.CLIENT_SIDE_ENCRYPTED_PROPERTY));
        assertEquals("user1", properties.getProperty(WorkspaceMetadata.LAST_MODIFIED_USER_PROPERTY));
        assertEquals("structurizr/dsl", properties.getProperty(WorkspaceMetadata.LAST_MODIFIED_AGENT_PROPERTY));
        assertEquals("2021-01-31T14:30:59Z", properties.getProperty(WorkspaceMetadata.LAST_MODIFIED_DATE_PROPERTY));
        assertEquals("1234567890", properties.getProperty(WorkspaceMetadata.API_KEY_PROPERTY));
        assertEquals("0987654321", properties.getProperty(WorkspaceMetadata.API_SECRET_PROPERTY));
        assertEquals("false", properties.getProperty(WorkspaceMetadata.PUBLIC_PROPERTY));
        assertEquals("12345678901234567890", properties.getProperty(WorkspaceMetadata.SHARING_TOKEN_PROPERTY));
        assertEquals("user@example.com", properties.getProperty(WorkspaceMetadata.LOCKED_USER_PROPERTY));
        assertEquals("structurizr/web", properties.getProperty(WorkspaceMetadata.LOCKED_AGENT_PROPERTY));
        assertEquals("2022-01-31T14:30:59Z", properties.getProperty(WorkspaceMetadata.LOCKED_DATE_PROPERTY));
        assertEquals("user1,user2,user3", properties.getProperty(WorkspaceMetadata.READ_USERS_AND_ROLES_PROPERTY));
        assertEquals("user4,user5,user6", properties.getProperty(WorkspaceMetadata.WRITE_USERS_AND_ROLES_PROPERTY));
        assertEquals("false", properties.getProperty(WorkspaceMetadata.ARCHIVED_PROPERTY));
    }

    @Test
    void fromProperties_DefaultsLastModifiedDateWhenNotSet() throws Exception {
        WorkspaceMetadata workspace = WorkspaceMetadata.fromProperties(123, new Properties());
        assertEquals(DateUtils.parseIsoDate("1970-01-01T00:00:00Z"), workspace.getLastModifiedDate());
    }

    @Test
    void visibility() {
        WorkspaceMetadata wmd = new WorkspaceMetadata(1);
        assertFalse(wmd.isPublicWorkspace());

        wmd.setPublicWorkspace(true);
        assertTrue(wmd.isPublicWorkspace());

        wmd.setSharingToken("token");
        assertFalse(wmd.isPublicWorkspace());
        assertTrue(wmd.isShareable());

        wmd.setPublicWorkspace(true);
        assertTrue(wmd.isPublicWorkspace());
        assertFalse(wmd.isShareable());
    }

    @Test
    void internalVersion() {
        WorkspaceMetadata wmd = new WorkspaceMetadata(1);

        assertNull(wmd.getUserFriendlyInternalVersion());

        wmd.setInternalVersion("20260109162427000"); // parseable
        assertEquals("2026-01-09 16:24:27", wmd.getUserFriendlyInternalVersion());

        wmd.setInternalVersion("20260109162427"); // not parseable
        assertEquals("20260109162427", wmd.getUserFriendlyInternalVersion());
    }

    @Test
    void getPermissions_WhenAuthenticationIsDisabled() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_NONE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetadata wmd = new WorkspaceMetadata(1);
        User user = new User("user@example.com");

        assertTrue(wmd.getPermissions(user).contains(Permission.Admin));
        assertTrue(wmd.getPermissions(user).contains(Permission.Write));
        assertTrue(wmd.getPermissions(user).contains(Permission.Read));
    }

    @Test
    void getPermissions_WhenAuthenticationIsEnabled_AnonymousUser_NoWorkspaceUsers_NoAdminUsers() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "");
        Configuration.init(Profile.Server, properties);

        WorkspaceMetadata wmd = new WorkspaceMetadata(1);

        User user = new User("user@example.com", Set.of(), AuthenticationMethod.NONE); // no access
        assertFalse(wmd.getPermissions(user).contains(Permission.Admin));
        assertFalse(wmd.getPermissions(user).contains(Permission.Write));
        assertFalse(wmd.getPermissions(user).contains(Permission.Read));
    }

    @Test
    void getPermissions_WhenAuthenticationIsEnabled_NoWorkspaceUsers_NoAdminUsers() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "");
        Configuration.init(Profile.Server, properties);

        WorkspaceMetadata wmd = new WorkspaceMetadata(1);

        User user = new User("user@example.com"); // admin, write, read
        assertTrue(wmd.getPermissions(user).contains(Permission.Admin));
        assertTrue(wmd.getPermissions(user).contains(Permission.Write));
        assertTrue(wmd.getPermissions(user).contains(Permission.Read));
    }

    @Test
    void getPermissions_WhenAuthenticationIsEnabled_NoWorkspaceUsers_AdminUsers() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        Configuration.init(Profile.Server, properties);

        WorkspaceMetadata wmd = new WorkspaceMetadata(1);

        User user = new User("user@example.com"); // write, read
        assertFalse(wmd.getPermissions(user).contains(Permission.Admin));
        assertTrue(wmd.getPermissions(user).contains(Permission.Write));
        assertTrue(wmd.getPermissions(user).contains(Permission.Read));

        User admin = new User("admin@example.com"); // admin, write, read
        assertTrue(wmd.getPermissions(admin).contains(Permission.Admin));
        assertTrue(wmd.getPermissions(admin).contains(Permission.Write));
        assertTrue(wmd.getPermissions(admin).contains(Permission.Read));
    }

    @Test
    void getPermissions_WhenAuthenticationIsEnabled_WorkspaceUsers_NoAdminUsers() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "");
        Configuration.init(Profile.Server, properties);

        WorkspaceMetadata wmd = new WorkspaceMetadata(1);
        wmd.addWriteUser("write@example.com");
        wmd.addReadUser("read@example.com");

        User user = new User("user@example.com"); // no permissions
        assertFalse(wmd.getPermissions(user).contains(Permission.Admin));
        assertFalse(wmd.getPermissions(user).contains(Permission.Write));
        assertFalse(wmd.getPermissions(user).contains(Permission.Read));

        User write = new User("write@example.com"); // admin, write, read
        assertTrue(wmd.getPermissions(write).contains(Permission.Admin));
        assertTrue(wmd.getPermissions(write).contains(Permission.Write));
        assertTrue(wmd.getPermissions(write).contains(Permission.Read));

        User read = new User("read@example.com"); // read
        assertFalse(wmd.getPermissions(read).contains(Permission.Admin));
        assertFalse(wmd.getPermissions(read).contains(Permission.Write));
        assertTrue(wmd.getPermissions(read).contains(Permission.Read));
    }

    @Test
    void getPermissions_WhenAuthenticationIsEnabled_WorkspaceUsers_AdminUsers() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "admin@example.com");
        Configuration.init(Profile.Server, properties);

        WorkspaceMetadata wmd = new WorkspaceMetadata(1);
        wmd.addWriteUser("write@example.com");
        wmd.addReadUser("read@example.com");

        User user = new User("user@example.com"); // no permissions
        assertFalse(wmd.getPermissions(user).contains(Permission.Admin));
        assertFalse(wmd.getPermissions(user).contains(Permission.Write));
        assertFalse(wmd.getPermissions(user).contains(Permission.Read));

        User admin = new User("admin@example.com"); // admin, write, read
        assertTrue(wmd.getPermissions(admin).contains(Permission.Admin));
        assertTrue(wmd.getPermissions(admin).contains(Permission.Write));
        assertTrue(wmd.getPermissions(admin).contains(Permission.Read));

        User write = new User("write@example.com"); // write, read
        assertFalse(wmd.getPermissions(write).contains(Permission.Admin));
        assertTrue(wmd.getPermissions(write).contains(Permission.Write));
        assertTrue(wmd.getPermissions(write).contains(Permission.Read));

        User read = new User("read@example.com"); // read
        assertFalse(wmd.getPermissions(read).contains(Permission.Admin));
        assertFalse(wmd.getPermissions(read).contains(Permission.Write));
        assertTrue(wmd.getPermissions(read).contains(Permission.Read));
    }

}