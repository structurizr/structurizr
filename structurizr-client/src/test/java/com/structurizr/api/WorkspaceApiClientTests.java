package com.structurizr.api;

import com.structurizr.Workspace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WorkspaceApiClientTests {

    private WorkspaceApiClient client;

    @Test
    void construction() {
        client = new WorkspaceApiClient("https://localhost", 1234, "key");
        assertEquals("https://localhost", client.getUrl());
    }

    @Test
    void construction_TruncatesTheApiUrl_WhenTheApiUrlHasATrailingSlashCharacter() {
        client = new WorkspaceApiClient("https://localhost/", 1, "key");
        assertEquals("https://localhost", client.getUrl());
    }

    @Test
    void construction_ThrowsAnException_WhenANullApiKeyIsUsed() {
        try {
            client = new WorkspaceApiClient("https://localhost", 1234, null);
            fail();
        } catch (IllegalArgumentException iae) {
            assertEquals("The API key must not be null or empty", iae.getMessage());
        }
    }

    @Test
    void construction_ThrowsAnException_WhenAnEmptyApiKeyIsUsed() {
        try {
            client = new WorkspaceApiClient("https://localhost", 1234, "");
            fail();
        } catch (IllegalArgumentException iae) {
            assertEquals("The API key must not be null or empty", iae.getMessage());
        }
    }

    @Test
    void construction_ThrowsAnException_WhenANullApiUrlIsUsed() {
        try {
            client = new WorkspaceApiClient(null, 1, "key");
            fail();
        } catch (IllegalArgumentException iae) {
            assertEquals("The API URL must not be null or empty.", iae.getMessage());
        }
    }

    @Test
    void construction_ThrowsAnException_WhenAnEmptyApiUrlIsUsed() {
        try {
            client = new WorkspaceApiClient(" ", 1, "key");
            fail();
        } catch (IllegalArgumentException iae) {
            assertEquals("The API URL must not be null or empty.", iae.getMessage());
        }
    }

    @Test
    void construction_ThrowsAnException_WhenAnInvalidWorkspaceIdIsUsed() {
        try {
            client = new WorkspaceApiClient("http://localhost", 0, "key");
            fail();
        } catch (IllegalArgumentException iae) {
            assertEquals("The workspace ID must be a positive integer", iae.getMessage());
        }
    }

    @Test
    void putWorkspace_ThrowsAnException_WhenANullWorkspaceIsSpecified() throws Exception {
        try {
            client = new WorkspaceApiClient("http://localhost", 1234, "key");
            client.putWorkspace(null);
            fail();
        } catch (IllegalArgumentException iae) {
            assertEquals("The workspace must not be null", iae.getMessage());
        }
    }

    @Test
    void getAgent() {
        client = new WorkspaceApiClient("http://localhost", 1234, "key");
        assertTrue(client.getAgent().startsWith("structurizr-java/"));
    }

    @Test
    void setAgent() {
        client = new WorkspaceApiClient("http://localhost", 1234, "key");
        client.setAgent("new_agent");
        assertEquals("new_agent", client.getAgent());
    }

    @Test
    void setAgent_ThrowsAnException_WhenPassedNull() {
        client = new WorkspaceApiClient("http://localhost", 1234, "key");

        try {
            client.setAgent(null);
            fail();
        } catch (Exception e) {
            assertEquals("An agent must be provided.", e.getMessage());
        }
    }

    @Test
    void setAgent_ThrowsAnException_WhenPassedAnEmptyString() {
        client = new WorkspaceApiClient("http://localhost", 1234, "key");

        try {
            client.setAgent(" ");
            fail();
        } catch (Exception e) {
            assertEquals("An agent must be provided.", e.getMessage());
        }
    }

}