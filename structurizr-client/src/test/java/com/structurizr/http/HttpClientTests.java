package com.structurizr.http;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpClientTests {

    @Test
    @Tag("IntegrationTest")
    void get_WhenNoAllowedUrlsAreConfigured() {
        HttpClient httpClient = new HttpClient();

        try {
            httpClient.get("https://static.structurizr.com/themes/microsoft-azure-2024.07.15/icons.json");
        } catch (Exception e) {
            assertEquals("Access to https://static.structurizr.com/themes/microsoft-azure-2024.07.15/icons.json is not permitted", e.getMessage());
        }
    }

    @Test
    @Tag("IntegrationTest")
    void get_WithAllowedUrl() {
        HttpClient httpClient = new HttpClient();
        httpClient.allow("https://playground.structurizr.com/static/themes/amazon-web-services.*");

        httpClient.get("https://playground.structurizr.com/static/themes/amazon-web-services-2023.01/theme.json");

        try {
            httpClient.get("https://playground.structurizr.com/static/themes/microsoft-azure-2024.07/theme.json");
        } catch (Exception e) {
            assertEquals("Access to https://playground.structurizr.com/static/themes/microsoft-azure-2024.07/theme.json is not permitted", e.getMessage());
        }
    }

    @Test
    @Tag("IntegrationTest")
    void get_WithDisallowedUrl() {
        HttpClient httpClient = new HttpClient();
        httpClient.allow("https://static.structurizr.com/.*");

        try {
            httpClient.get("https://example.com");
        } catch (Exception e) {
            assertEquals("Access to https://example.com is not permitted", e.getMessage());
        }
    }

}