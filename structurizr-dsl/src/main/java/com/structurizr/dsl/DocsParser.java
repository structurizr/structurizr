package com.structurizr.dsl;

final class DocsParser extends AbstractParser {

    private static final String DEFAULT_DOCUMENT_IMPORTER = "com.structurizr.importer.documentation.DefaultDocumentationImporter";

    private static final String GRAMMAR = "!docs <path> <fqn>";
    private static final int PATH_INDEX = 1;
    private static final int FQN_INDEX = 2;

    private static final String EXCLUDE_GRAMMAR = "exclude <filename|regex> [filename|regex]";
    private static final int REGEX_INDEX = 1;

    void parse(DocumentationDslContext context, Tokens tokens) {
        // !docs <path> <fqn>

        context.setDslPortable(false);

        if (tokens.hasMoreThan(FQN_INDEX)) {
            throw new RuntimeException("Too many tokens, expected: " + GRAMMAR);
        }

        if (!tokens.includes(PATH_INDEX)) {
            throw new RuntimeException("Expected: " + GRAMMAR);
        }

        String path = tokens.get(PATH_INDEX);

        String fullyQualifiedClassName = DEFAULT_DOCUMENT_IMPORTER;
        if (tokens.includes(FQN_INDEX)) {
            fullyQualifiedClassName = tokens.get(FQN_INDEX);
        }

        context.setPath(path);
        context.setFullyQualifiedClassName(fullyQualifiedClassName);
    }

    void parseExclude(DocumentationDslContext context, Tokens tokens) {
        // exclude <filename|regex> [filename|regex]
        if (!tokens.includes(REGEX_INDEX)) {
            throw new RuntimeException("Expected: " + EXCLUDE_GRAMMAR);
        }

        for (int i = REGEX_INDEX; i < tokens.size(); i++) {
            String regex = tokens.get(i);
            context.exclude(regex);
        }
    }

}