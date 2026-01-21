package com.structurizr.dsl;

import java.io.File;

final class WorkspaceDslContext extends DslContext {

    private final File file;

    WorkspaceDslContext() {
        this.file = null;
    }

    WorkspaceDslContext(File file) {
        this.file = file;
    }

    File getFile() {
        return file;
    }

    @Override
    protected String[] getPermittedTokens() {
        return new String[] {
                StructurizrDslTokens.NAME_TOKEN,
                StructurizrDslTokens.DESCRIPTION_TOKEN,
                StructurizrDslTokens.PROPERTIES_TOKEN,
                StructurizrDslTokens.DOCS_TOKEN,
                StructurizrDslTokens.DECISIONS_TOKEN,
                StructurizrDslTokens.IDENTIFIERS_TOKEN,
                StructurizrDslTokens.IMPLIED_RELATIONSHIPS_TOKEN,
                StructurizrDslTokens.MODEL_TOKEN,
                StructurizrDslTokens.VIEWS_TOKEN,
                StructurizrDslTokens.CONFIGURATION_TOKEN
        };
    }

}