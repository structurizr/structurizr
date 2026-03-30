package com.structurizr.dsl;

import com.structurizr.model.DeploymentNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentGroupParserTests extends AbstractTests {

    private final DeploymentGroupParser parser = new DeploymentGroupParser();

    @Test
    void test_parse_ThrowsAnException_WhenThereAreTooManyTokens() {
        try {
            parser.parse(tokens("deploymentGroup", "name", "extra"));
            fail();
        } catch (Exception e) {
            assertEquals("Too many tokens, expected: deploymentGroup <name>", e.getMessage());
        }
    }

    @Test
    void test_parse_ThrowsAnException_WhenTheNameIsMissing() {
        try {
            parser.parse(tokens("deploymentGroup"));
            fail();
        } catch (Exception e) {
            assertEquals("Expected: deploymentGroup <name>", e.getMessage());
        }
    }

    @Test
    void test_parse() {
        String service1 = parser.parse(tokens("deploymentGroup", "Service 1"));
        assertEquals("Service 1", service1);
    }

    @Test
    void test_parse_ForDeploymentNode_ThrowsAnException_WhenThereAreTooManyTokens() {
        try {
            DeploymentNodeDslContext context = new DeploymentNodeDslContext(null);
            parser.parse(context, tokens("deploymentGroup", "name", "extra"));
            fail();
        } catch (Exception e) {
            assertEquals("Too many tokens, expected: deploymentGroup <deploymentGroups>", e.getMessage());
        }
    }

    @Test
    void test_parse_ForDeploymentNode_ThrowsAnException_WhenTheNameIsMissing() {
        try {
            DeploymentNodeDslContext context = new DeploymentNodeDslContext(null);
            parser.parse(context, tokens("deploymentGroup"));
            fail();
        } catch (Exception e) {
            assertEquals("Expected: deploymentGroup <deploymentGroups>", e.getMessage());
        }
    }

    @Test
    void test_parse_ForDeploymentNode() {
        DeploymentNode deploymentNode = workspace.getModel().addDeploymentNode("Live", "Deployment Node", "Description");
        DeploymentNodeDslContext context = new DeploymentNodeDslContext(deploymentNode);
        IdentifiersRegister identifiersRegister = new IdentifiersRegister();
        identifiersRegister.register("dg1", new DeploymentGroup(new DeploymentEnvironment("Live"), "Deployment Group 1"));
        context.setIdentifierRegister(identifiersRegister);

        parser.parse(context, tokens("deploymentGroup", "dg1"));

        assertTrue(deploymentNode.getDeploymentGroups().contains("Deployment Group 1"));
    }

}