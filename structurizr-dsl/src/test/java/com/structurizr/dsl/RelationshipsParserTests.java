package com.structurizr.dsl;

import com.structurizr.model.DeploymentNode;
import com.structurizr.model.Relationship;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.model.SoftwareSystemInstance;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class RelationshipsParserTests extends AbstractTests {

    private final RelationshipsParser parser = new RelationshipsParser();

    @Test
    void test_parseTechnology_ThrowsAnException_WhenNoTechnologyIsSpecified() {
        try {
            parser.parseTechnology(null, tokens("technology"));
            fail();
        } catch (Exception e) {
            assertEquals("Expected: technology <technology>", e.getMessage());
        }
    }

    @Test
    void test_parseTechnology() {
        SoftwareSystem a = model.addSoftwareSystem("A");
        SoftwareSystem b = model.addSoftwareSystem("B");
        Relationship relationship = a.uses(b, "Description", "HTTP");

        DeploymentNode node = model.addDeploymentNode("Node");
        SoftwareSystemInstance aInstance = node.add(a);
        SoftwareSystemInstance bInstance = node.add(b);
        Relationship relationshipInstance = aInstance.getEfferentRelationshipWith(bInstance);

        RelationshipsDslContext context = new RelationshipsDslContext(null, Set.of(relationship, relationshipInstance));

        parser.parseTechnology(context, tokens("technology", "HTTPS"));
        assertEquals("HTTP", relationship.getTechnology());
        assertEquals("HTTPS", relationshipInstance.getTechnology());
    }

}