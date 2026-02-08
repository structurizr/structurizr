package com.structurizr.dsl;

import com.structurizr.PerspectivesHolder;
import com.structurizr.model.Perspective;

final class PerspectiveDslContext extends DslContext {

    private final PerspectivesDslContext perspectivesDslContext;
    private final Perspective perspective;

    PerspectiveDslContext(Perspective perspective, PerspectivesDslContext context) {
        this.perspective = perspective;
        this.perspectivesDslContext = context;
    }

    Perspective getPerspective() {
        return perspective;
    }

    @Override
    void end() {
        for (PerspectivesHolder perspectivesHolder : perspectivesDslContext.getPerspectivesHolders()) {
            perspectivesHolder.addPerspective(perspective);
        }
    }

    @Override
    protected String[] getPermittedTokens() {
        return new String[] {
                StructurizrDslTokens.PERSPECTIVE_DESCRIPTION_TOKEN,
                StructurizrDslTokens.PERSPECTIVE_VALUE_TOKEN
        };
    }

}