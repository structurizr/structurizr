package com.structurizr.server.domain;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class WorkspaceMetaDataTests {

    @Test
    void getName_WhenNull() {
        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        workspace.setName(null);
        assertEquals("Workspace 1", workspace.getName());
    }

    @Test
    public void isPublic() {
        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        assertFalse(workspace.isPublicWorkspace());

        workspace.setPublicWorkspace(true);
        assertTrue(workspace.isPublicWorkspace());
    }

    @Test
    public void isPublicWorkspace() {
        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        assertFalse(workspace.isPublicWorkspace()); // workspaces are private by default

        workspace.setPublicWorkspace(true);
        assertTrue(workspace.isPublicWorkspace());
    }

    @Test
    public void sharingToken() {
        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        assertEquals("", workspace.getSharingToken());
        assertFalse(workspace.isShareable());

        workspace.setSharingToken("12345678901234567890");
        assertEquals("12345678901234567890", workspace.getSharingToken());
        assertEquals("123456...", workspace.getSharingTokenTruncated());
        assertTrue(workspace.isShareable());
    }

    @Test
    public void hasNoUsersConfigured_ReturnsTrue_WhenTheWorkspaceHasNoConfiguredUsers() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        assertTrue(workspace.hasNoUsersConfigured());
    }

    @Test
    public void hasNoUsersConfigured_ReturnsFalse_WhenAuthenticationIsEnabledAndTheWorkspaceHasAReadOnlyUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        workspace.addReadUser("read@example.com");
        assertFalse(workspace.hasNoUsersConfigured());
    }

    @Test
    public void hasNoUsersConfigured_ReturnsFalse_WhenAuthenticationIsEnabledAndTheWorkspaceHasAReadWriteUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        workspace.addWriteUser("write@example.com");
        assertFalse(workspace.hasNoUsersConfigured());
    }

    @Test
    public void isReadUser_ReturnsFalse_WhenAuthenticationIsEnabledAndTheUserIsNotAReadUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        workspace.addReadUser("read@example.com");

        User user = new User("user@example.com");
        assertFalse(workspace.isReadUser(user));
    }

    @Test
    public void isReadUser_ReturnsTrue_WhenTheUserIsAReadUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        workspace.addReadUser("read@example.com");

        User user = new User("read@example.com");
        assertTrue(workspace.isReadUser(user));
    }

    @Test
    public void isReadUser_ReturnsFalse_WhenAuthenticationIsEnabledAndTheUserRoleIsNotAReadUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        workspace.addReadUser("role1");

        User user = new User("user@example.com", Set.of("role2"), AuthenticationMethod.LOCAL);
        assertFalse(workspace.isReadUser(user));
    }

    @Test
    public void isReadUser_ReturnsTrue_WhenAuthenticationIsEnabledAndTheUserRoleIsAReadUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        workspace.addReadUser("role1");

        User user = new User("user@example.com", Set.of("role1", "role2"), AuthenticationMethod.LOCAL);
        assertTrue(workspace.isReadUser(user));
    }

    @Test
    public void isWriteUser_ReturnsFalse_WhenAuthenticationIsEnabledAndTheUserIsNotAWriteUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        workspace.addWriteUser("write@example.com");

        User user = new User("user@example.com");
        assertFalse(workspace.isWriteUser(user));
    }

    @Test
    public void isWriteUser_ReturnsTrue_WhenAuthenticationIsEnabledAndTheUserIsAWriteUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        workspace.addWriteUser("write@example.com");

        User user = new User("write@example.com");
        assertTrue(workspace.isWriteUser(user));
    }

    @Test
    public void test_isWriteUser_ReturnsFalse_WhenAuthenticationIsEnabledAndTheUserRoleIsNotAWriteUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        workspace.addWriteUser("role1");

        User user = new User("user", new HashSet<>(List.of("role2")), AuthenticationMethod.LOCAL);
        assertFalse(workspace.isWriteUser(user));
    }

    @Test
    public void test_isWriteUser_ReturnsTrue_WhenAuthenticationIsEnabledAndTheUserRoleIsAWriteUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        WorkspaceMetaData workspace = new WorkspaceMetaData(1);
        workspace.addWriteUser("role1");

        User user = new User("user", new HashSet<>(List.of("role1", "role2")), AuthenticationMethod.LOCAL);
        assertTrue(workspace.isWriteUser(user));
    }

    @Test
    public void test_isLocked_ReturnsFalse_WhenTheWorkspaceIsNotLocked() {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setLockedUser(null);
        workspaceMetaData.setLockedDate(null);
        assertFalse(workspaceMetaData.isLocked());
    }

    @Test
    public void test_isLocked_ReturnsTrue_WhenTheWorkspaceIsLocked() {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setLockedUser("user");
        workspaceMetaData.setLockedDate(new Date());

        assertTrue(workspaceMetaData.isLocked());
    }

    @Test
    public void test_isLocked_ReturnsFalse_WhenTheWorkspaceWasLockedOverTwoMinutesAgo() {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setLockedUser("user");
        workspaceMetaData.setLockedDate(DateUtils.getXMinutesAgo(3));

        assertFalse(workspaceMetaData.isLocked());
    }

    @Test
    public void test_isLockedBy_ReturnsFalse_WhenTheWorkspaceIsNotLocked() {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setLockedUser(null);
        workspaceMetaData.setLockedDate(null);
        assertFalse(workspaceMetaData.isLockedBy("user", "agent"));
    }

    @Test
    public void test_isLockedBy_ReturnsTrue_WhenTheWorkspaceIsLockedByTheSpecifiedUserAndAgent() {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setLockedUser("user");
        workspaceMetaData.setLockedAgent("agent");
        workspaceMetaData.setLockedDate(new Date());

        assertTrue(workspaceMetaData.isLockedBy("user", "agent"));
    }

    @Test
    public void test_isLockedBy_ReturnsFalse_WhenTheWorkspaceIsNotLockedByTheSpecifiedUserAndAgent() {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setLockedUser("user1");
        workspaceMetaData.setLockedAgent("agent1");
        workspaceMetaData.setLockedDate(new Date());

        assertFalse(workspaceMetaData.isLockedBy("user1", "agent2"));
    }

    @Test
    public void test_isLockedBy_ReturnsFalse_WhenTheWorkspaceIsNotLockedByTheSpecifiedUser() {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.setLockedUser("user1");
        workspaceMetaData.setLockedAgent("agent1");
        workspaceMetaData.setLockedDate(new Date());

        assertFalse(workspaceMetaData.isLockedBy("user2", "agent2"));
    }

    @Test
    public void fromProperties_and_toProperties() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(WorkspaceMetaData.NAME_PROPERTY, "Name");
        properties.setProperty(WorkspaceMetaData.DESCRIPTION_PROPERTY, "Description");
        properties.setProperty(WorkspaceMetaData.VERSION_PROPERTY, "v1.2.3");
        properties.setProperty(WorkspaceMetaData.CLIENT_SIDE_ENCRYPTED_PROPERTY, "false");
        properties.setProperty(WorkspaceMetaData.LAST_MODIFIED_USER_PROPERTY, "user1");
        properties.setProperty(WorkspaceMetaData.LAST_MODIFIED_AGENT_PROPERTY, "structurizr/dsl");
        properties.setProperty(WorkspaceMetaData.LAST_MODIFIED_DATE_PROPERTY, "2021-01-31T14:30:59Z");
        properties.setProperty(WorkspaceMetaData.API_KEY_PROPERTY, "1234567890");
        properties.setProperty(WorkspaceMetaData.API_SECRET_PROPERTY, "0987654321");
        properties.setProperty(WorkspaceMetaData.PUBLIC_PROPERTY, "true");
        properties.setProperty(WorkspaceMetaData.SHARING_TOKEN_PROPERTY, "12345678901234567890");
        properties.setProperty(WorkspaceMetaData.OWNER_PROPERTY, "user@example.com");
        properties.setProperty(WorkspaceMetaData.LOCKED_USER_PROPERTY, "user2@example.com");
        properties.setProperty(WorkspaceMetaData.LOCKED_AGENT_PROPERTY, "structurizr/web");
        properties.setProperty(WorkspaceMetaData.LOCKED_DATE_PROPERTY, "2022-01-31T14:30:59Z");
        properties.setProperty(WorkspaceMetaData.READ_USERS_AND_ROLES_PROPERTY, "user1,user2,user3");
        properties.setProperty(WorkspaceMetaData.WRITE_USERS_AND_ROLES_PROPERTY, "user4,user5,user6");
        properties.setProperty(WorkspaceMetaData.ARCHIVED_PROPERTY, "false");

        WorkspaceMetaData workspace = WorkspaceMetaData.fromProperties(123, properties);

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
        assertEquals("user@example.com", workspace.getOwner());
        assertEquals("user2@example.com", workspace.getLockedUser());
        assertEquals("structurizr/web", workspace.getLockedAgent());
        assertEquals(DateUtils.parseIsoDate("2022-01-31T14:30:59Z"), workspace.getLockedDate());
        assertEquals(Set.of("user1", "user2", "user3"), workspace.getReadUsers());
        assertEquals(Set.of("user4", "user5", "user6"), workspace.getWriteUsers());
        assertFalse(workspace.isArchived());

        properties.clear();
        properties = workspace.toProperties();

        assertEquals("Name", properties.getProperty(WorkspaceMetaData.NAME_PROPERTY));
        assertEquals("Description", properties.getProperty(WorkspaceMetaData.DESCRIPTION_PROPERTY));
        assertEquals("v1.2.3", properties.getProperty(WorkspaceMetaData.VERSION_PROPERTY));
        assertEquals("false", properties.getProperty(WorkspaceMetaData.CLIENT_SIDE_ENCRYPTED_PROPERTY));
        assertEquals("user1", properties.getProperty(WorkspaceMetaData.LAST_MODIFIED_USER_PROPERTY));
        assertEquals("structurizr/dsl", properties.getProperty(WorkspaceMetaData.LAST_MODIFIED_AGENT_PROPERTY));
        assertEquals("2021-01-31T14:30:59Z", properties.getProperty(WorkspaceMetaData.LAST_MODIFIED_DATE_PROPERTY));
        assertEquals("1234567890", properties.getProperty(WorkspaceMetaData.API_KEY_PROPERTY));
        assertEquals("0987654321", properties.getProperty(WorkspaceMetaData.API_SECRET_PROPERTY));
        assertEquals("false", properties.getProperty(WorkspaceMetaData.PUBLIC_PROPERTY));
        assertEquals("12345678901234567890", properties.getProperty(WorkspaceMetaData.SHARING_TOKEN_PROPERTY));
        assertEquals("user@example.com", properties.getProperty(WorkspaceMetaData.OWNER_PROPERTY));
        assertEquals("user2@example.com", properties.getProperty(WorkspaceMetaData.LOCKED_USER_PROPERTY));
        assertEquals("structurizr/web", properties.getProperty(WorkspaceMetaData.LOCKED_AGENT_PROPERTY));
        assertEquals("2022-01-31T14:30:59Z", properties.getProperty(WorkspaceMetaData.LOCKED_DATE_PROPERTY));
        assertEquals("user1,user2,user3", properties.getProperty(WorkspaceMetaData.READ_USERS_AND_ROLES_PROPERTY));
        assertEquals("user4,user5,user6", properties.getProperty(WorkspaceMetaData.WRITE_USERS_AND_ROLES_PROPERTY));
        assertEquals("false", properties.getProperty(WorkspaceMetaData.ARCHIVED_PROPERTY));
    }

    @Test
    public void fromProperties_DefaultsLastModifiedDateWhenNotSet() throws Exception {
        WorkspaceMetaData workspace = WorkspaceMetaData.fromProperties(123, new Properties());
        assertEquals(DateUtils.parseIsoDate("1970-01-01T00:00:00Z"), workspace.getLastModifiedDate());
    }

    @Test
    void visibility() {
        WorkspaceMetaData wmd = new WorkspaceMetaData(1);
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
    void hasAccess_ReturnsTrue_WhenAuthenticationIsDisabled() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_NONE);
        Configuration.init(Profile.Server, properties);

        User user = new User("user@example.com");

        WorkspaceMetaData wmd = new WorkspaceMetaData(1);
        assertTrue(wmd.hasAccess(user));
    }

    @Test
    void hasAccess_ReturnsTrue_WhenAuthenticationIsEnabledAndNoUsersAreConfigured() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        User user = new User("user@example.com");

        WorkspaceMetaData wmd = new WorkspaceMetaData(1);
        assertTrue(wmd.hasAccess(user));
    }

    @Test
    void hasAccess_ReturnsTrue_WhenAuthenticationIsEnabledAndTheUserIsAnAdmin() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        properties.setProperty(StructurizrProperties.ADMIN_USERS_AND_ROLES, "user@example.com");
        Configuration.init(Profile.Server, properties);

        User user = new User("user@example.com");

        WorkspaceMetaData wmd = new WorkspaceMetaData(1);
        wmd.addWriteUser("write@example.com");
        assertTrue(wmd.hasAccess(user));
    }

    @Test
    void hasAccess_ReturnsTrue_WhenAuthenticationIsEnabledAndTheUserIsAWriteUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        User user = new User("write@example.com");

        WorkspaceMetaData wmd = new WorkspaceMetaData(1);
        wmd.addWriteUser("write@example.com");
        assertTrue(wmd.hasAccess(user));
    }

    @Test
    void hasAccess_ReturnsTrue_WhenAuthenticationIsEnabledAndTheUserIsAReadUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        User user = new User("read@example.com");

        WorkspaceMetaData wmd = new WorkspaceMetaData(1);
        wmd.addReadUser("read@example.com");
        assertTrue(wmd.hasAccess(user));
    }

    @Test
    void hasAccess_ReturnsFalse_WhenAuthenticationIsEnabledAndTheUserIsNotAWriteOrReadUser() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.AUTHENTICATION_IMPLEMENTATION, StructurizrProperties.AUTHENTICATION_VARIANT_FILE);
        Configuration.init(Profile.Server, properties);

        User user = new User("user@example.com");

        WorkspaceMetaData wmd = new WorkspaceMetaData(1);
        wmd.addWriteUser("write@example.com");
        wmd.addReadUser("read@example.com");
        assertFalse(wmd.hasAccess(user));
    }

}