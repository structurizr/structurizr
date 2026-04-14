package com.structurizr.dsl;

import java.util.HashMap;
import java.util.Map;

final class DecisionsParser extends AbstractParser {

    private static final Map<String,String> DECISION_IMPORTERS = new HashMap<>();

    private static final String ADRTOOLS_DECISION_IMPORTER = "adrtools";
    private static final String LOG4BRAINS_DECISION_IMPORTER = "log4brains";
    private static final String MADR_DECISION_IMPORTER = "madr";

    static {
        DECISION_IMPORTERS.put(ADRTOOLS_DECISION_IMPORTER, "com.structurizr.importer.documentation.AdrToolsDecisionImporter");
        DECISION_IMPORTERS.put(MADR_DECISION_IMPORTER, "com.structurizr.importer.documentation.MadrDecisionImporter");
        DECISION_IMPORTERS.put(LOG4BRAINS_DECISION_IMPORTER, "com.structurizr.importer.documentation.Log4brainsDecisionImporter");
    }

    private static final String GRAMMAR = "!decisions <path> <type|fqn>";
    private static final int PATH_INDEX = 1;
    private static final int TYPE_OR_FQN_INDEX = 2;

    private static final String EXCLUDE_GRAMMAR = "exclude <regex> [regex]";
    private static final int REGEX_INDEX = 1;

    void parse(DecisionsDslContext context, Tokens tokens) {
        // !adrs <path> <fqn>
        // !decisions <path> <fqn>

        context.setDslPortable(false);

        if (tokens.hasMoreThan(TYPE_OR_FQN_INDEX)) {
            throw new RuntimeException("Too many tokens, expected: " + GRAMMAR);
        }

        if (!tokens.includes(PATH_INDEX)) {
            throw new RuntimeException("Expected: " + GRAMMAR);
        }

        String path = tokens.get(PATH_INDEX);

        String fullyQualifiedClassName = DECISION_IMPORTERS.get(ADRTOOLS_DECISION_IMPORTER);
        if (tokens.includes(TYPE_OR_FQN_INDEX)) {
            String typeOrFullyQualifiedName = tokens.get(TYPE_OR_FQN_INDEX);
            fullyQualifiedClassName = DECISION_IMPORTERS.getOrDefault(typeOrFullyQualifiedName, typeOrFullyQualifiedName);
        }

        context.setPath(path);
        context.setFullyQualifiedClassName(fullyQualifiedClassName);
    }

    void parseExclude(DecisionsDslContext context, Tokens tokens) {
        // exclude <regex> [regex]
        if (!tokens.includes(REGEX_INDEX)) {
            throw new RuntimeException("Expected: " + EXCLUDE_GRAMMAR);
        }

        for (int i = REGEX_INDEX; i < tokens.size(); i++) {
            String regex = tokens.get(i);
            context.exclude(regex);
        }
    }

}