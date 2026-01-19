package com.structurizr.dsl;

import com.structurizr.model.*;

final class RelationshipsParser extends AbstractParser {

    private final static int TECHNOLOGY_INDEX = 1;

    void parseTechnology(RelationshipsDslContext context, Tokens tokens) {
        // technology <technology>
        if (tokens.hasMoreThan(TECHNOLOGY_INDEX)) {
            throw new RuntimeException("Too many tokens, expected: technology <technology>");
        }

        if (!tokens.includes(TECHNOLOGY_INDEX)) {
            throw new RuntimeException("Expected: technology <technology>");
        }

        String technology = tokens.get(TECHNOLOGY_INDEX);
        for (Relationship relationship : context.getRelationships()) {
            if (relationship.getSource() instanceof StaticStructureElementInstance && relationship.getDestination() instanceof StaticStructureElementInstance) {
                relationship.setTechnology(technology);
            }
        }
    }

}