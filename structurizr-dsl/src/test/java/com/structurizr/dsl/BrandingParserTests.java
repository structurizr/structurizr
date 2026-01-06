package com.structurizr.dsl;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class BrandingParserTests extends AbstractTests {

    private BrandingParser parser = new BrandingParser();

    @Test
    void test_parseLogo_ThrowsAnException_WhenThereAreTooManyTokens() {
        BrandingDslContext context = new BrandingDslContext(null);

        try {
            parser.parseLogo(context, tokens("logo", "path", "extra"));
            fail();
        } catch (Exception e) {
            assertEquals("Too many tokens, expected: logo <path|url>", e.getMessage());
        }
    }

    @Test
    void test_parseLogo_ThrowsAnException_WhenNoPathIsSpecified() {
        BrandingDslContext context = new BrandingDslContext(null);

        try {
            parser.parseLogo(context, tokens("logo"));
            fail();
        } catch (Exception e) {
            assertEquals("Expected: logo <path|url>", e.getMessage());
        }
    }

    @Test
    void test_parseLogo_ThrowsAnException_WhenTheLogoDoesNotExist() {
        BrandingDslContext context = new BrandingDslContext(new File("."));
        context.getFeatures().enable(Features.FILE_SYSTEM);

        try {
            parser.parseLogo(context, tokens("logo", "hello.png"));
            fail();
        } catch (Exception e) {
            assertEquals("hello.png does not exist", e.getMessage());
        }
    }

    @Test
    void test_parseLogo_ThrowsAnException_WhenTheFileIsNotSupported() {
        BrandingDslContext context = new BrandingDslContext(new File("."));
        context.getFeatures().enable(Features.FILE_SYSTEM);

        try {
            parser.parseLogo(context, tokens("logo", "src/test/resources/dsl/getting-started.dsl"));
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage().endsWith("is not a supported image file."));
        }
    }

    @Test
    void test_parseLogo_SetsTheLogo_WhenTheLogoDoesExist() {
        BrandingDslContext context = new BrandingDslContext(new File("."));
        context.setWorkspace(workspace);
        context.getFeatures().enable(Features.FILE_SYSTEM);

        parser.parseLogo(context, tokens("logo", "src/test/resources/dsl/logo.png"));
        assertTrue(workspace.getViews().getConfiguration().getBranding().getLogo().startsWith("data:image/png;base64,"));
    }

    @Test
    void test_parseLogo_SetsTheLogoFromADataUri() {
        BrandingDslContext context = new BrandingDslContext(new File("."));
        context.setWorkspace(workspace);

        parser.parseLogo(context, tokens("logo", "data:image/png;base64,123456789012345678901234567890"));
        assertTrue(workspace.getViews().getConfiguration().getBranding().getLogo().startsWith("data:image/png;base64,123456789012345678901234567890"));
    }

    @Test
    void test_parseLogo_ThrowsAnException_WithAHttpIconAndHttpIsNotEnabled() {
        BrandingDslContext context = new BrandingDslContext(new File("."));
        context.setWorkspace(workspace);
        context.getFeatures().disable(Features.HTTP);

        try {
            parser.parseLogo(context, tokens("logo", "http://structurizr.com/logo.png"));
            fail();
        } catch (Exception e) {
            assertEquals("Icons via HTTP are not permitted (feature structurizr.feature.dsl.http is not enabled)", e.getMessage());
        }
    }

    @Test
    void test_parseLogo_SetsTheLogoFromAHttpUrl() {
        BrandingDslContext context = new BrandingDslContext(new File("."));
        context.setWorkspace(workspace);
        context.getFeatures().enable(Features.HTTP);

        parser.parseLogo(context, tokens("logo", "http://structurizr.com/logo.png"));
        assertEquals("http://structurizr.com/logo.png", workspace.getViews().getConfiguration().getBranding().getLogo());
    }

    @Test
    void test_parseLogo_ThrowsAnException_WithAHttpsIconAndHttpsIsNotEnabled() {
        BrandingDslContext context = new BrandingDslContext(new File("."));
        context.setWorkspace(workspace);
        context.getFeatures().disable(Features.HTTPS);

        try {
            parser.parseLogo(context, tokens("logo", "https://structurizr.com/logo.png"));
            fail();
        } catch (Exception e) {
            assertEquals("Icons via HTTPS are not permitted (feature structurizr.feature.dsl.https is not enabled)", e.getMessage());
        }
    }

    @Test
    void test_parseLogo_SetsTheLogoFromAHttpsUrl() {
        BrandingDslContext context = new BrandingDslContext(new File("."));
        context.setWorkspace(workspace);
        context.getFeatures().enable(Features.HTTPS);

        parser.parseLogo(context, tokens("logo", "https://structurizr.com/logo.png"));
        assertEquals("https://structurizr.com/logo.png", workspace.getViews().getConfiguration().getBranding().getLogo());
    }

}