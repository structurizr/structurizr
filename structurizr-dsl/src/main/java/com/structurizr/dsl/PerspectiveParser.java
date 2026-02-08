package com.structurizr.dsl;

import com.structurizr.PerspectivesHolder;
import com.structurizr.model.Perspective;

final class PerspectiveParser extends AbstractParser {

    void parse(PerspectivesDslContext context, Tokens tokens) {
        // <name> <description> [value]
        final int PERSPECTIVE_NAME_INDEX = 0;
        final int PERSPECTIVE_DESCRIPTION_INDEX = 1;
        final int PERSPECTIVE_VALUE_INDEX = 2;

        if (tokens.hasMoreThan(PERSPECTIVE_VALUE_INDEX)) {
            throw new RuntimeException("Too many tokens, expected: <name> <description> [value]");
        }

        if (!tokens.includes(PERSPECTIVE_DESCRIPTION_INDEX)) {
            throw new RuntimeException("Expected: <name> <description> [value]");
        }

        String name = tokens.get(PERSPECTIVE_NAME_INDEX);
        String description = tokens.get(PERSPECTIVE_DESCRIPTION_INDEX);
        String value = "";

        if (tokens.includes(PERSPECTIVE_VALUE_INDEX)) {
            value = tokens.get(PERSPECTIVE_VALUE_INDEX);
        }

        for (PerspectivesHolder perspectivesHolder : context.getPerspectivesHolders()) {
            perspectivesHolder.addPerspective(name, description, value);
        }
    }

    Perspective parse(Tokens tokens) {
        // perspective <name> {
        String name = tokens.get(1);

        return new Perspective(name);
    }

    void parseValue(PerspectiveDslContext context, Tokens tokens) {
        // value <value>
        final int PERSPECTIVE_VALUE_INDEX = 1;

        if (tokens.hasMoreThan(PERSPECTIVE_VALUE_INDEX)) {
            throw new RuntimeException("Too many tokens, expected: value <value>");
        }

        if (!tokens.includes(PERSPECTIVE_VALUE_INDEX)) {
            throw new RuntimeException("Expected: value <value>");
        }

        String value = tokens.get(1);

        context.getPerspective().setValue(value);
    }

    void parseDescription(PerspectiveDslContext context, Tokens tokens) {
        // description <description>
        final int PERSPECTIVE_DESCRIPTION_INDEX = 1;

        if (tokens.hasMoreThan(PERSPECTIVE_DESCRIPTION_INDEX)) {
            throw new RuntimeException("Too many tokens, expected: description <description>");
        }

        if (!tokens.includes(PERSPECTIVE_DESCRIPTION_INDEX)) {
            throw new RuntimeException("Expected: description <description>");
        }

        String description = tokens.get(1);

        context.getPerspective().setDescription(description);
    }

    void parseUrl(PerspectiveDslContext context, Tokens tokens) {
        // url <url>
        final int PERSPECTIVE_URL_INDEX = 1;

        if (tokens.hasMoreThan(PERSPECTIVE_URL_INDEX)) {
            throw new RuntimeException("Too many tokens, expected: url <url>");
        }

        if (!tokens.includes(PERSPECTIVE_URL_INDEX)) {
            throw new RuntimeException("Expected: url <url>");
        }

        String url = tokens.get(1);

        context.getPerspective().setUrl(url);
    }

}