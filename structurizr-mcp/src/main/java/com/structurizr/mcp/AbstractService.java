package com.structurizr.mcp;

import com.structurizr.dsl.StructurizrDslParser;

class AbstractService {

    protected StructurizrDslParser createStructurizrDslParser() {
        StructurizrDslParser parser = new StructurizrDslParser();

        parser.getFeatures().configure(com.structurizr.dsl.Features.ENVIRONMENT, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.FILE_SYSTEM, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.PLUGINS, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.SCRIPTS, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.COMPONENT_FINDER, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.DOCUMENTATION, false);
        parser.getFeatures().configure(com.structurizr.dsl.Features.DECISIONS, false);

        return parser;
    }

}